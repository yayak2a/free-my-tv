package io.freemytv;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

/**
 * The sleep timer (the moon button): the TV goes to standby in 30, 60 or 90 minutes, whatever app is on screen.
 * An alarm wakes HomeReceiver in the main process, which presses the remote's Sleep key through the TV's own ADB
 * (the same connection the mode switch uses), and Power if the TV is still awake two seconds later.
 */
final class Sleep {
    private Sleep() {}

    static final String ACTION = "io.freemytv.SLEEP";
    /** Each press of the button: off → 30 → 60 → 90 → off. */
    static final int[] STEPS = {30, 60, 90};

    private static SharedPreferences sp(Context c) {
        return c.getApplicationContext().getSharedPreferences("sleep", Context.MODE_PRIVATE);
    }

    /** When the TV goes to sleep (epoch ms), or 0 when the timer is off. */
    static long at(Context c) {
        long at = sp(c).getLong("at", 0);
        return at > System.currentTimeMillis() ? at : 0;
    }

    /** Minutes left, rounded up; 0 when off. */
    static int minutesLeft(Context c) {
        long at = at(c);
        return at == 0 ? 0 : (int) Math.ceil((at - System.currentTimeMillis()) / 60_000.0);
    }

    /** The next step after the current one; returns the minutes set (0 = off). */
    static int cycle(Context c) {
        int left = minutesLeft(c), next = 0;
        if (left == 0) next = STEPS[0];
        else for (int step : STEPS) if (step > left + 1) { next = step; break; }
        set(c, next);
        return next;
    }

    static void set(Context c, int minutes) {
        AlarmManager alarms = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pi = intent(c);
        alarms.cancel(pi);
        if (minutes <= 0) {
            sp(c).edit().remove("at").apply();
            return;
        }
        long at = System.currentTimeMillis() + minutes * 60_000L;
        sp(c).edit().putLong("at", at).apply();
        boolean exact = Build.VERSION.SDK_INT < 31 || alarms.canScheduleExactAlarms();
        if (exact) alarms.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi);
        else alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi);
    }

    private static PendingIntent intent(Context c) {
        Intent i = new Intent(c, HomeReceiver.class).setAction(ACTION);
        return PendingIntent.getBroadcast(c, 7, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    /** The alarm went off (main process, off the main thread): standby. */
    static void now(Context c) {
        sp(c).edit().remove("at").apply();
        try (Adb adb = Adb.connect(c, 0, null)) {
            adb.shell("input keyevent KEYCODE_SLEEP");
            Thread.sleep(2000);
            String power = adb.shell("dumpsys power | grep -m1 mWakefulness=");
            if (power.contains("Awake")) adb.shell("input keyevent KEYCODE_POWER");
            AppLog.w("sleep timer: the TV went to standby");
        } catch (Exception e) {
            AppLog.w("sleep timer: " + e.getMessage());
        }
    }
}
