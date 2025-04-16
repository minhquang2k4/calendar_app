package com.example.calendar_app;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class App extends Application {
    private static App instance;
    private static final String TAG = "CalendarApp";

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        String userId = getSharedPreferences("MyPrefs", MODE_PRIVATE).getString("USER_ID", null);
        if (userId != null) {
            Log.d(TAG, "User ID found, scheduling reminder checks: " + userId);
            scheduleReminderChecks();
        } else {
            Log.d(TAG, "No user ID found, skipping reminder scheduling");
        }
    }

    /**
     * Public method to schedule reminder checks - can be called after login
     */
    public void scheduleReminderChecks() {
        Log.d(TAG, "Scheduling reminder checks");
        scheduleRepeatingCheck(this);
    }

    private void scheduleRepeatingCheck(Context context) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        // Add action to make the intent more explicit
        intent.setAction("com.example.calendar_app.CHECK_REMINDERS");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 1000, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            long interval = 60 * 1000; // 1 minute
            long startTime = System.currentTimeMillis() + 5000;

            Log.d(TAG, "Setting up repeating alarm for reminder checks");

            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    startTime,
                    interval,
                    pendingIntent);
            Log.d(TAG, "Scheduled repeating alarm");
        }
    }
    
    public static App getInstance() {
        return instance;
    }
}