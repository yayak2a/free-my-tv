package io.freemytv;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The iCloud album full screen, opened from the drawer's photo card: one photo every 8 seconds with a crossfade,
 * ← → to go back and forth, Back to leave. Each photo is fitted whole on a softly blurred copy of itself.
 */
public class PhotosActivity extends Activity {
    static final String START = "start";

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);   // a slideshow, not an idle screen
        setContentView(new Show(this, getIntent().getIntExtra(START, 0)));
    }

    private static final class Show extends View {
        private static final long EVERY = 8000, FADE = 900;
        private final Album album;
        private final List<String> photos;
        private final Handler handler = new Handler(Looper.getMainLooper());
        private final ExecutorService loader = Executors.newSingleThreadExecutor();
        private final Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
        private final Paint dim = new Paint();
        private final RectF rect = new RectF();
        private Bitmap now, before, nowBlur, beforeBlur;
        private long fadeAt;
        private int index, loading = -1;

        private final Runnable advance = () -> go(1);

        Show(Context c, int start) {
            super(c);
            album = Album.get(c);
            photos = album.ready();
            index = photos.isEmpty() ? 0 : Math.floorMod(start, photos.size());
            dim.setColor(0x8C000000);
            setFocusable(true);
            setBackgroundColor(0xFF000000);
        }

        @Override protected void onSizeChanged(int w, int h, int ow, int oh) {
            if (now == null) load(index);
        }

        @Override protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            handler.removeCallbacksAndMessages(null);
            loader.shutdownNow();
        }

        private void go(int step) {
            if (photos.isEmpty()) return;
            index = Math.floorMod(index + step, photos.size());
            load(index);
        }

        /** Decodes the photo (and its blurred backdrop) in the background, then fades it in. */
        private void load(int at) {
            if (photos.isEmpty() || getWidth() == 0) return;
            loading = at;
            handler.removeCallbacks(advance);
            int w = getWidth(), h = getHeight();
            String guid = photos.get(at);
            loader.execute(() -> {
                Bitmap b = album.fit(guid, w, h);
                Bitmap blur = null;
                if (b != null) {
                    // Tiny, then drawn full screen with filtering: a soft blur for nothing.
                    int bw = 32, bh = Math.max(1, Math.round(32f * b.getHeight() / b.getWidth()));
                    Bitmap tiny = Bitmap.createScaledBitmap(b, bw, bh, true);
                    blur = Bitmap.createScaledBitmap(tiny, bw * 2, bh * 2, true);
                    tiny.recycle();
                }
                final Bitmap photo = b, soft = blur;
                handler.post(() -> {
                    if (loading != at) {   // moved on meanwhile
                        if (photo != null) photo.recycle();
                        if (soft != null) soft.recycle();
                        return;
                    }
                    if (photo != null) {
                        if (before != null) before.recycle();
                        if (beforeBlur != null) beforeBlur.recycle();
                        before = now;
                        beforeBlur = nowBlur;
                        now = photo;
                        nowBlur = soft;
                        fadeAt = SystemClock.uptimeMillis();
                        invalidate();
                    }
                    handler.postDelayed(advance, EVERY);
                });
            });
        }

        @Override protected void onDraw(Canvas c) {
            float t = now == null ? 0 : Math.min(1, (SystemClock.uptimeMillis() - fadeAt) / (float) FADE);
            t = t * t * (3 - 2 * t);
            if (before != null && t < 1) draw(c, before, beforeBlur, 255);
            if (now != null) draw(c, now, nowBlur, Math.round(255 * t));
            if (t < 1) postInvalidateOnAnimation();
        }

        private void draw(Canvas c, Bitmap photo, Bitmap blur, int alpha) {
            int w = getWidth(), h = getHeight();
            paint.setAlpha(alpha);
            if (blur != null) {
                float s = Math.max((float) w / blur.getWidth(), (float) h / blur.getHeight());
                float bw = blur.getWidth() * s, bh = blur.getHeight() * s;
                rect.set((w - bw) / 2, (h - bh) / 2, (w + bw) / 2, (h + bh) / 2);
                c.drawBitmap(blur, null, rect, paint);
                dim.setAlpha(Math.round(0x8C * alpha / 255f));
                c.drawRect(0, 0, w, h, dim);
            }
            float s = Math.min((float) w / photo.getWidth(), (float) h / photo.getHeight());
            float pw = photo.getWidth() * s, ph = photo.getHeight() * s;
            rect.set((w - pw) / 2, (h - ph) / 2, (w + pw) / 2, (h + ph) / 2);
            c.drawBitmap(photo, null, rect, paint);
        }

        @Override public boolean onKeyDown(int code, KeyEvent e) {
            if (code == KeyEvent.KEYCODE_DPAD_RIGHT || code == KeyEvent.KEYCODE_MEDIA_NEXT) {
                go(1);
                return true;
            }
            if (code == KeyEvent.KEYCODE_DPAD_LEFT || code == KeyEvent.KEYCODE_MEDIA_PREVIOUS) {
                go(-1);
                return true;
            }
            return super.onKeyDown(code, e);
        }
    }
}
