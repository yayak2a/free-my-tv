package io.freemytv;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Screensaver photos of landscapes and animals from Unsplash (unsplash.com), with a free Access Key of your own
 * (config.properties "unsplash_key"). A stock of photos stays on disk, so the screensaver has them right away and
 * without internet; a few new ones come in every few hours, and each screensaver shows them in a new order.
 *
 * Unsplash's rules: the photographer is credited on screen, and each photo the TV keeps is reported to Unsplash's
 * download endpoint once (as for a wallpaper). The images come from Unsplash's own servers, at the screen's size.
 */
final class Nature {
    private static final String API = "https://api.unsplash.com";
    /** One request per theme, each bringing this many photos. */
    private static final String[] QUERIES = {"landscape", "wildlife"};
    private static final int PER_QUERY = 8, KEEP = 32;
    private static final long FETCH_EVERY = 6 * 3600_000L, RETRY_AFTER = 30 * 60_000L;
    /** The screensaver's photo size (DreamView.PW × PH): 10% larger than the screen for the slow zoom. */
    private static final int W = 2112, H = 1188;
    static final String KIND = "photo";

    private static Nature instance;

    static synchronized Nature get(Context c) {
        if (instance == null) instance = new Nature(c.getApplicationContext());
        return instance;
    }

    /** One photo: what the screensaver shows, plus where it comes from. */
    private static final class Photo {
        String id, place, author, image, report;
        boolean reported;
    }

    private final Context context;
    private final SharedPreferences store;
    private final java.util.concurrent.ExecutorService io = java.util.concurrent.Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());
    private final List<Recs.Listener> listeners = new ArrayList<>();
    private volatile List<Photo> photos = new ArrayList<>();
    private volatile List<String> order = new ArrayList<>();
    private volatile boolean fetching;
    private volatile long fetchedAt, failedAt;
    private volatile String error = "";

    private Nature(Context c) {
        context = c;
        store = c.getSharedPreferences("nature", Context.MODE_PRIVATE);
        fetchedAt = store.getLong("fetchedAt", 0);
        try {
            photos = parse(new JSONArray(store.getString("photos", "[]")));
        } catch (Exception ignored) {
        }
    }

    static boolean hasKey() {
        return !Config.UNSPLASH_KEY.trim().isEmpty();
    }

    void addListener(Recs.Listener l) { if (!listeners.contains(l)) listeners.add(l); }
    void removeListener(Recs.Listener l) { listeners.remove(l); }

    String error() { return error; }

    // MARK: - What the screensaver shows

    /** A new screensaver: the photos in a new random order, never starting with the one that started the last one. */
    void newSession() {
        List<String> ids = new ArrayList<>();
        for (Photo p : photos) if (imageFile(p).exists()) ids.add(p.id);
        Collections.shuffle(ids);
        String last = store.getString("first", "");
        if (ids.size() > 1 && ids.get(0).equals(last)) ids.add(ids.remove(0));
        if (!ids.isEmpty()) store.edit().putString("first", ids.get(0)).apply();
        order = ids;
    }

    /** The photos on disk, in this screensaver's order (new arrivals at the end). */
    List<Recs.Pick> showcase() {
        List<Recs.Pick> out = new ArrayList<>();
        if (!hasKey()) return out;
        List<Photo> all = photos;
        Set<String> placed = new HashSet<>();
        for (String id : order) {
            for (Photo p : all) {
                if (p.id.equals(id) && imageFile(p).exists() && placed.add(id)) out.add(pick(p));
            }
        }
        // Built here, not kept: the credit follows the language picked on the TV.
        for (Photo p : all) if (imageFile(p).exists() && placed.add(p.id)) out.add(pick(p));
        return out;
    }

    /** The photo's image on disk, or null. */
    File readyImage(Recs.Pick pick) {
        for (Photo p : photos) {
            if (p.id.equals(pick.backdrop)) return imageFile(p).exists() ? imageFile(p) : null;
        }
        return null;
    }

    // MARK: - Fetching

    /** New photos when the stock is old or small; after a failure, waits 30 minutes before trying again. */
    void refreshIfStale() {
        if (!hasKey() || fetching) return;
        long now = System.currentTimeMillis();
        long sinceFail = now - failedAt;
        if (failedAt != 0 && sinceFail >= 0 && sinceFail < RETRY_AFTER) return;
        int ready = 0;
        for (Photo p : photos) if (imageFile(p).exists()) ready++;
        boolean stale = now - fetchedAt > FETCH_EVERY || now < fetchedAt || ready < PER_QUERY;
        boolean missing = ready < photos.size();
        if (!stale && !missing) return;
        fetching = true;
        io.execute(() -> {
            try {
                List<Photo> list = new ArrayList<>(photos);
                if (stale) {
                    Set<String> have = new HashSet<>();
                    for (Photo p : list) have.add(p.id);
                    for (String q : QUERIES) {
                        for (Photo p : fetch(q)) if (have.add(p.id)) list.add(p);
                    }
                    fetchedAt = System.currentTimeMillis();
                }
                int failures = 0, tried = 0;
                String last = "";
                for (Photo p : list) {
                    if (imageFile(p).exists() && p.reported) continue;
                    tried++;
                    try {
                        download(p);
                        if (!p.reported) {
                            report(p);
                            p.reported = true;
                        }
                    } catch (IOException e) {
                        failures++;
                        last = e.getMessage() == null ? e.toString() : e.getMessage();
                    }
                }
                photos = prune(list);
                save();
                if (tried > 0 && failures == tried) throw new IOException(last);
                error = "";
                failedAt = 0;
            } catch (Exception e) {
                error = e.getMessage() == null ? e.toString() : e.getMessage();
                failedAt = System.currentTimeMillis();
                AppLog.w("unsplash: " + error);
            } finally {
                fetching = false;
                main.post(() -> { for (Recs.Listener l : new ArrayList<>(listeners)) l.onRecs(); });
            }
        });
    }

    private List<Photo> fetch(String query) throws Exception {
        String body = get(API + "/photos/random?count=" + PER_QUERY + "&orientation=landscape&content_filter=high&query=" + query);
        JSONArray a = new JSONArray(body);
        List<Photo> out = new ArrayList<>();
        for (int i = 0; i < a.length(); i++) {
            JSONObject o = a.optJSONObject(i);
            if (o == null) continue;
            JSONObject urls = o.optJSONObject("urls"), links = o.optJSONObject("links"), user = o.optJSONObject("user");
            String raw = urls == null ? "" : urls.optString("raw", "");
            if (o.optString("id", "").isEmpty() || raw.isEmpty()) continue;
            Photo p = new Photo();
            p.id = o.optString("id");
            p.image = raw + (raw.contains("?") ? "&" : "?") + "w=" + W + "&h=" + H + "&fit=crop&crop=entropy&q=82&fm=jpg";
            p.report = links == null ? "" : links.optString("download_location", "");
            p.author = user == null ? "" : user.optString("name", "").trim();
            p.place = place(o.optJSONObject("location"));
            out.add(p);
        }
        return out;
    }

    /** "Lofoten, Norway": the location's name, or its city and country, or nothing. */
    private static String place(JSONObject loc) {
        if (loc == null) return "";
        String name = loc.isNull("name") ? "" : loc.optString("name", "").trim();
        if (!name.isEmpty()) return name;
        String city = loc.isNull("city") ? "" : loc.optString("city", "").trim();
        String country = loc.isNull("country") ? "" : loc.optString("country", "").trim();
        return city.isEmpty() ? country : country.isEmpty() ? city : city + ", " + country;
    }

    private void download(Photo p) throws IOException {
        File f = imageFile(p);
        if (f.exists()) return;
        //noinspection ResultOfMethodCallIgnored
        f.getParentFile().mkdirs();
        File part = new File(f.getPath() + ".part");
        HttpURLConnection c = (HttpURLConnection) new URL(p.image).openConnection();
        c.setConnectTimeout(8000);
        c.setReadTimeout(20000);
        try (InputStream in = c.getInputStream(); FileOutputStream out = new FileOutputStream(part)) {
            byte[] buffer = new byte[16384];
            int n;
            while ((n = in.read(buffer)) > 0) out.write(buffer, 0, n);
        } catch (IOException e) {
            //noinspection ResultOfMethodCallIgnored
            part.delete();
            throw e;
        } finally {
            c.disconnect();
        }
        if (!part.renameTo(f)) throw new IOException("couldn't save the photo");
    }

    /** Unsplash counts the photos apps keep: one call to its download endpoint per photo. */
    private void report(Photo p) {
        if (p.report == null || p.report.isEmpty()) return;
        try {
            get(p.report);
        } catch (IOException e) {
            AppLog.w("unsplash: download report: " + e.getMessage());
        }
    }

    private String get(String url) throws IOException {
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setConnectTimeout(8000);
        c.setReadTimeout(10000);
        c.setRequestProperty("Authorization", "Client-ID " + Config.UNSPLASH_KEY.trim());
        c.setRequestProperty("Accept-Version", "v1");
        try {
            int code = c.getResponseCode();
            if (code == 401) throw new IOException("Unsplash rejected the key");
            if (code == 403) throw new IOException("Unsplash: too many requests this hour");
            if (code != 200) throw new IOException("Unsplash returned HTTP " + code);
            try (InputStream in = c.getInputStream()) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buffer = new byte[8192];
                int n;
                while ((n = in.read(buffer)) > 0) out.write(buffer, 0, n);
                return new String(out.toByteArray(), StandardCharsets.UTF_8);
            }
        } finally {
            c.disconnect();
        }
    }

    // MARK: - Stock on disk

    private File imageFile(Photo p) {
        return new File(new File(context.getCacheDir(), "nature"), p.id.replaceAll("[^A-Za-z0-9_-]", "") + ".jpg");
    }

    /** Keeps the newest photos (up to KEEP) and deletes the others' files. */
    private List<Photo> prune(List<Photo> list) {
        List<Photo> keep = new ArrayList<>(list.subList(Math.max(0, list.size() - KEEP), list.size()));
        Set<String> names = new HashSet<>();
        for (Photo p : keep) names.add(imageFile(p).getName());
        File[] files = new File(context.getCacheDir(), "nature").listFiles();
        if (files != null) {
            for (File f : files) {
                //noinspection ResultOfMethodCallIgnored
                if (!names.contains(f.getName())) f.delete();
            }
        }
        return keep;
    }

    private void save() {
        JSONArray a = new JSONArray();
        try {
            for (Photo p : photos) {
                a.put(new JSONObject().put("id", p.id).put("place", p.place).put("author", p.author)
                        .put("image", p.image).put("report", p.report).put("reported", p.reported));
            }
        } catch (org.json.JSONException ignored) {
        }
        store.edit().putString("photos", a.toString()).putLong("fetchedAt", fetchedAt).apply();
    }

    private static List<Photo> parse(JSONArray a) {
        List<Photo> out = new ArrayList<>();
        for (int i = 0; i < a.length(); i++) {
            JSONObject o = a.optJSONObject(i);
            if (o == null || o.optString("id", "").isEmpty()) continue;
            Photo p = new Photo();
            p.id = o.optString("id");
            p.place = o.optString("place", "");
            p.author = o.optString("author", "");
            p.image = o.optString("image", "");
            p.report = o.optString("report", "");
            p.reported = o.optBoolean("reported", false);
            out.add(p);
        }
        return out;
    }

    /** As the screensaver's caption: the place (or nothing) and the photographer's credit. */
    private static Recs.Pick pick(Photo p) {
        Recs.Pick k = new Recs.Pick();
        k.kind = KIND;
        k.id = p.id.hashCode();
        k.backdrop = p.id;
        k.title = p.place;
        k.label = "UNSPLASH";
        k.meta = p.author.isEmpty() ? "Unsplash" : Ui.tr("Foto: ", "Photo: ", "Foto: ") + p.author;
        k.detailed = true;
        return k;
    }
}
