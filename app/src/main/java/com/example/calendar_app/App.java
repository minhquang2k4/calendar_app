package com.example.calendar_app;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class App extends Application {
    private static App instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;


        int userId = getSharedPreferences("MyPrefs", MODE_PRIVATE).getInt("USER_ID", -1);
        if (userId != -1) {
            scheduleRepeatingCheck(this);
        }


    }


    private void scheduleRepeatingCheck(Context context) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            long interval = 60 * 1000;
            long startTime = System.currentTimeMillis() + 5000;

            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    startTime,
                    interval,
                    pendingIntent
            );
        }}


    public static App getInstance() {
        return instance;
    }
}