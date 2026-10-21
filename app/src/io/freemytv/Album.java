package io.freemytv;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Your own photos on the drawer's square card, from an iCloud shared album anyone with the link can open
 * (config.properties "icloud_album"). Two kinds of links:
 *  - iOS 26 and later: https://photos.icloud.com/shared/album/<key> ("Copy Link to Album", sharing option "Anyone
 *    with the link"). Read through CloudKit as photos.icloud.com does: the key resolves to a public access token and
 *    the album's server, whose zone lists every photo with a small JPEG (resJPEGThumbRes, ~340 × 510).
 *  - Older: https://www.icloud.com/sharedalbum/#B0… ("Public Website"), read through the sharedstreams service.
 * Neither is documented by Apple, so either may change one day. Anyone with the link can see the album: make one
 * just for the TV.
 *
 * Up to 60 photos stay on disk, small (the card is 300 px); the album is checked every 30 minutes.
 */
final class Album {
    private static final long CHECK_EVERY = 30 * 60_000L, RETRY_AFTER = 10 * 60_000L;
    private static final int KEEP = 60, NEW_PER_CHECK = 20;
    /** The smallest version at least this wide is downloaded (the card is 300 px; a little more stays sharp). */
    private static final int WANT = 640;
    private static final String CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    interface Listener { void onAlbum(); }

    private static Album instance;

    static synchronized Album get(Context c) {
        if (instance == null) instance = new Album(c.getApplicationContext());
        return instance;
    }

    private final Context context;
    private final SharedPreferences store;
    private final java.util.concurrent.ExecutorService io = java.util.concurrent.Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());
    private final List<Listener> listeners = new ArrayList<>();
    private volatile List<String> guids = new ArrayList<>();   // newest first, as in the album
    private volatile boolean fetching;
    private volatile long checkedAt, failedAt;
    private int next;

    private Album(Context c) {
        context = c;
        store = c.getSharedPreferences("album", Context.MODE_PRIVATE);
        checkedAt = store.getLong("checkedAt", 0);
        String saved = store.getString("guids", "");
        if (!saved.isEmpty() && token().equals(store.getString("token", ""))) {
            List<String> list = new ArrayList<>();
            Collections.addAll(list, saved.split(","));
            guids = list;
        }
    }

    /** The album's key: the last part of a photos.icloud.com link, or what follows "#" in an older one. */
    static String token() {
        String link = Config.ICLOUD_ALBUM.trim();
        if (cloudKit()) {
            String t = link.replaceAll("[?#].*$", "").replaceAll("/+$", "");
            t = t.substring(t.lastIndexOf('/') + 1);
            return t.matches("[0-9A-Za-z_-]{10,}") ? t : "";
        }
        int hash = link.lastIndexOf('#');
        String t = hash >= 0 ? link.substring(hash + 1) : link;
        int end = t.indexOf(';');
        if (end >= 0) t = t.substring(0, end);
        return t.matches("[0-9A-Za-z]{10,}") ? t : "";
    }

    /** A photos.icloud.com link (iOS 26 and later). */
    private static boolean cloudKit() {
        return Config.ICLOUD_ALBUM.contains("photos.icloud.com/shared/");
    }

    static boolean configured() {
        return !token().isEmpty();
    }

    void addListener(Listener l) { if (!listeners.contains(l)) listeners.add(l); }
    void removeListener(Listener l) { listeners.remove(l); }

    // MARK: - Photos for the card

    /** How many photos are on disk, ready to show. */
    int count() {
        int n = 0;
        for (String g : guids) if (file(g).exists()) n++;
        return n;
    }

    /**
     * The next photo, decoded and cropped to a square of `size` px (center), or null. Call it off the main thread.
     * The order is the album's, newest first, starting over at the end.
     */
    Bitmap nextSquare(int size) {
        List<String> list = new ArrayList<>();
        for (String g : guids) if (file(g).exists()) list.add(g);
        if (list.isEmpty() || size <= 0) return null;
        for (int tries = 0; tries < list.size(); tries++) {
            String g = list.get(Math.floorMod(next++, list.size()));
            try {
                Bitmap b = square(file(g), size);
                if (b != null) return b;
            } catch (RuntimeException | OutOfMemoryError e) {
                AppLog.w("album: photo: " + e.getMessage());
            }
        }
        return null;
    }

    private static Bitmap square(File f, int size) {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(f.getPath(), bounds);
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null;
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = 1;
        while (Math.min(bounds.outWidth, bounds.outHeight) / (opts.inSampleSize * 2) >= size) opts.inSampleSize *= 2;
        Bitmap b = BitmapFactory.decodeFile(f.getPath(), opts);
        if (b == null) return null;
        int side = Math.min(b.getWidth(), b.getHeight());
        Bitmap crop = Bitmap.createBitmap(b, (b.getWidth() - side) / 2, (b.getHeight() - side) / 2, side, side);
        Bitmap out = Bitmap.createScaledBitmap(crop, size, size, true);
        if (crop != b) b.recycle();
        if (out != crop) crop.recycle();
        return out;
    }

    // MARK: - Reading the shared album

    void refreshIfStale() {
        if (!configured() || fetching) return;
        long now = System.currentTimeMillis();
        long sinceFail = now - failedAt;
        if (failedAt != 0 && sinceFail >= 0 && sinceFail < RETRY_AFTER) return;
        boolean stale = now - checkedAt > CHECK_EVERY || now < checkedAt || guids.isEmpty();
        if (!stale) return;
        fetching = true;
        io.execute(() -> {
            try {
                check();
                failedAt = 0;
            } catch (Exception e) {
                failedAt = System.currentTimeMillis();
                AppLog.w("album: " + e.getMessage());
            } finally {
                fetching = false;
                main.post(() -> { for (Listener l : new ArrayList<>(listeners)) l.onAlbum(); });
            }
        });
    }

    private void check() throws Exception {
        if (cloudKit()) {
            checkCloudKit();
            return;
        }
        String token = token();
        String base = "https://" + host(token) + "/" + token + "/sharedstreams/";
        JSONObject stream;
        try {
            stream = post(base + "webstream", "{\"streamCtag\":null}");
        } catch (java.net.UnknownHostException e) {
            // Any server answers with the right one (330 below): ask a known one.
            base = "https://p23-sharedstreams.icloud.com/" + token + "/sharedstreams/";
            stream = post(base + "webstream", "{\"streamCtag\":null}");
        }
        String redirect = stream.optString("X-Apple-MMe-Host", "");
        if (!redirect.isEmpty()) {   // the album lives on another server: Apple says which
            base = "https://" + redirect + "/" + token + "/sharedstreams/";
            stream = post(base + "webstream", "{\"streamCtag\":null}");
        }
        JSONArray photos = stream.optJSONArray("photos");
        if (photos == null) throw new IOException("no photos in the album (is its public link on?)");

        // Newest first; videos are left out (their poster frames aren't worth a slot).
        List<String> order = new ArrayList<>();
        java.util.Map<String, String> checksumOf = new java.util.HashMap<>();
        List<JSONObject> list = new ArrayList<>();
        for (int i = 0; i < photos.length(); i++) {
            JSONObject p = photos.optJSONObject(i);
            if (p != null && !"video".equals(p.optString("mediaAssetType"))) list.add(p);
        }
        Collections.sort(list, (a, b) -> b.optString("dateCreated").compareTo(a.optString("dateCreated")));
        for (JSONObject p : list) {
            String guid = p.optString("photoGuid", "");
            String sum = pickSize(p.optJSONObject("derivatives"));
            if (guid.isEmpty() || sum == null || !guid.matches("[0-9A-Za-z-]+")) continue;
            order.add(guid);
            checksumOf.put(guid, sum);
            if (order.size() >= KEEP) break;
        }

        // Download what's new (a few per check), with the short-lived links Apple hands out for them.
        List<String> missing = new ArrayList<>();
        for (String g : order) if (!file(g).exists() && missing.size() < NEW_PER_CHECK) missing.add(g);
        if (!missing.isEmpty()) {
            JSONObject body = new JSONObject().put("photoGuids", new JSONArray(missing));
            JSONObject items = post(base + "webasseturls", body.toString()).optJSONObject("items");
            for (String g : missing) {
                JSONObject loc = items == null ? null : items.optJSONObject(checksumOf.get(g));
                if (loc == null) continue;
                String url = "https://" + loc.optString("url_location") + loc.optString("url_path");
                try {
                    download(url, file(g));
                } catch (IOException e) {
                    AppLog.w("album: download: " + e.getMessage());
                }
            }
        }

        done(order, token);
    }

    private void done(List<String> order, String token) {
        guids = order;
        prune(order);
        checkedAt = System.currentTimeMillis();
        store.edit().putString("guids", android.text.TextUtils.join(",", order)).putString("token", token)
                .putLong("checkedAt", checkedAt).apply();
    }

    // MARK: - photos.icloud.com (CloudKit)

    private static final String CK = "/database/1/com.apple.photos.cloud/production/";

    private void checkCloudKit() throws Exception {
        String key = token();
        // 1. The link's key → the album's zone, a public access token (20 minutes) and the server that holds it.
        JSONObject body = new JSONObject().put("shortGUIDs", new JSONArray().put(new JSONObject().put("value", key)));
        JSONObject resolved = post("https://ckdatabasews.icloud.com" + CK + "public/records/resolve?remapEnums=true&sharing_url_key=" + key,
                body.toString());
        JSONArray results = resolved.optJSONArray("results");
        JSONObject r = results == null ? null : results.optJSONObject(0);
        JSONObject access = r == null ? null : r.optJSONObject("anonymousPublicAccess");
        if (r == null || access == null || r.optJSONObject("zoneID") == null) {
            throw new IOException("album not found (is it shared with \"Anyone with the link\"?)");
        }
        String server = access.optString("databasePartition", "https://ckdatabasews.icloud.com").replace(":443", "");
        String url = server + CK + "shared/changes/zone?remapEnums=true&sharing_url_key=" + key
                + "&publicAccessAuthToken=" + java.net.URLEncoder.encode(access.optString("token"), "UTF-8");

        // 2. Every record in the album's zone, a page at a time: photos (CPLMaster) and their entries (CPLAsset).
        List<JSONObject> masters = new ArrayList<>();
        Set<String> shown = new HashSet<>();
        boolean anyAsset = false;
        String sync = null;
        for (int page = 0; page < 20; page++) {
            JSONObject zone = new JSONObject().put("zoneID", r.getJSONObject("zoneID")).put("resultsLimit", 200);
            if (sync != null) zone.put("syncToken", sync);
            JSONObject reply = post(url, new JSONObject().put("zones", new JSONArray().put(zone)).toString());
            JSONObject z = reply.optJSONArray("zones") == null ? null : reply.getJSONArray("zones").optJSONObject(0);
            if (z == null) break;
            JSONArray records = z.optJSONArray("records");
            for (int i = 0; records != null && i < records.length(); i++) {
                JSONObject rec = records.optJSONObject(i);
                if (rec == null || rec.optBoolean("deleted")) continue;
                String type = rec.optString("recordType");
                JSONObject f = rec.optJSONObject("fields");
                if (f == null) continue;
                if ("CPLMaster".equals(type)) {
                    masters.add(rec);
                } else if ("CPLAsset".equals(type)) {
                    anyAsset = true;
                    boolean hidden = value(f, "isHidden") == 1 || value(f, "trashReason") > 0;
                    JSONObject ref = f.optJSONObject("masterRef");
                    String master = ref == null || ref.optJSONObject("value") == null ? "" : ref.getJSONObject("value").optString("recordName");
                    if (!hidden && !master.isEmpty()) shown.add(master);
                }
            }
            sync = z.optString("syncToken", null);
            if (!z.optBoolean("moreComing")) break;
        }

        // Newest first; videos and photos taken out of the album are left out.
        List<JSONObject> photos = new ArrayList<>();
        for (JSONObject m : masters) {
            JSONObject f = m.getJSONObject("fields");
            String item = f.optJSONObject("itemType") == null ? "" : f.getJSONObject("itemType").optString("value");
            if (item.contains("movie") || item.contains("video") || item.contains("mpeg")) continue;
            if (anyAsset && !shown.contains(m.optString("recordName"))) continue;
            if (download(f) == null) continue;
            photos.add(m);
        }
        Collections.sort(photos, (a, b) -> Double.compare(value(b.optJSONObject("fields"), "originalCreationDate"),
                value(a.optJSONObject("fields"), "originalCreationDate")));
        List<String> order = new ArrayList<>();
        int fetched = 0;
        for (JSONObject m : photos) {
            if (order.size() >= KEEP) break;
            String guid = m.optString("recordName").replaceAll("[^A-Za-z0-9_-]", "_");
            if (guid.isEmpty()) continue;
            order.add(guid);
            if (file(guid).exists() || fetched >= NEW_PER_CHECK) continue;
            fetched++;
            try {
                download(download(m.getJSONObject("fields")), file(guid));
            } catch (IOException e) {
                AppLog.w("album: download: " + e.getMessage());
            }
        }
        done(order, key);
    }

    /** The small JPEG's link (the medium one if the small one is missing), or null. */
    private static String download(JSONObject fields) {
        for (String res : new String[] {"resJPEGThumbRes", "resJPEGMedRes"}) {
            JSONObject o = fields.optJSONObject(res);
            JSONObject v = o == null ? null : o.optJSONObject("value");
            String url = v == null ? "" : v.optString("downloadURL", "");
            if (!url.isEmpty()) return url.replace("${f}", "photo.jpg");
        }
        return null;
    }

    private static double value(JSONObject fields, String name) {
        JSONObject o = fields == null ? null : fields.optJSONObject(name);
        return o == null ? 0 : o.optDouble("value", 0);
    }

    /** The checksum of the smallest version at least WANT px wide (else the largest). */
    private static String pickSize(JSONObject derivatives) {
        if (derivatives == null) return null;
        String best = null, largest = null;
        int bestW = Integer.MAX_VALUE, largestW = 0;
        for (Iterator<String> it = derivatives.keys(); it.hasNext(); ) {
            JSONObject d = derivatives.optJSONObject(it.next());
            if (d == null || d.optString("checksum", "").isEmpty()) continue;
            int w = Math.max(d.optInt("width", 0), d.optInt("height", 0));
            if (w >= WANT && w < bestW) { bestW = w; best = d.optString("checksum"); }
            if (w > largestW) { largestW = w; largest = d.optString("checksum"); }
        }
        return best != null ? best : largest;
    }

    /** The server for this album: "pNN-sharedstreams.icloud.com", NN from the token's first characters (base 62). */
    static String host(String token) {
        int partition = token.charAt(0) == 'A' ? CHARS.indexOf(token.charAt(1))
                : CHARS.indexOf(token.charAt(1)) * 62 + CHARS.indexOf(token.charAt(2));
        return "p" + Math.max(1, partition) + "-sharedstreams.icloud.com";
    }

    private static JSONObject post(String url, String json) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setConnectTimeout(8000);
        c.setReadTimeout(12000);
        c.setRequestMethod("POST");
        c.setDoOutput(true);
        c.setRequestProperty("Content-Type", "text/plain");
        // The page each service expects to be called from.
        c.setRequestProperty("Origin", url.contains("ckdatabasews") ? "https://photos.icloud.com" : "https://www.icloud.com");
        try {
            try (OutputStream out = c.getOutputStream()) {
                out.write(json.getBytes(StandardCharsets.UTF_8));
            }
            int code = c.getResponseCode();
            if (code == 404) throw new IOException("album not found (is its public link on?)");
            // 330: the album is on another server, named in the reply.
            InputStream in = code == 200 ? c.getInputStream() : code == 330 ? c.getErrorStream() : null;
            if (in == null) throw new IOException("iCloud returned HTTP " + code);
            try (InputStream body = in) {
                return new JSONObject(read(body));
            }
        } finally {
            c.disconnect();
        }
    }

    private static String read(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int n;
        while ((n = in.read(buffer)) > 0) out.write(buffer, 0, n);
        return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }

    private static void download(String url, File f) throws IOException {
        //noinspection ResultOfMethodCallIgnored
        f.getParentFile().mkdirs();
        File part = new File(f.getPath() + ".part");
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
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

    private File file(String guid) {
        return new File(new File(context.getCacheDir(), "album"), guid + ".jpg");
    }

    /** Photos removed from the album (or past the 60 newest) leave the TV too. */
    private void prune(List<String> keep) {
        Set<String> names = new HashSet<>();
        for (String g : keep) names.add(g + ".jpg");
        File[] files = new File(context.getCacheDir(), "album").listFiles();
        if (files == null) return;
        for (File f : files) {
            //noinspection ResultOfMethodCallIgnored
            if (!names.contains(f.getName())) f.delete();
        }
    }
}
