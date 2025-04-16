package com.example.calendar_app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.calendar_app.Entities.EventEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReminderReceiver extends BroadcastReceiver {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("ReminderReceiver", "Receiver triggered");

        executor.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(context);

            String userId = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                    .getString("USER_ID", null);
            
            if (userId == null) {
                Log.e("ReminderReceiver", "User ID not found");
                return;
            }

            try {
                String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                
                List<EventEntity> events = db.eventDao().getUpcomingEventsWithNotifications(userId, today);

                for (EventEntity event : events) {
                    Log.d("ReminderReceiver", "- Tiêu đề: " + event.title);
                    
                    scheduleNotification(context, event);
                }
            } catch (Exception e) {
                Log.e("ReminderReceiver", "Lỗi khi xử lý sự kiện: ", e);
            }
        });
    }

    private void scheduleNotification(Context context, EventEntity event) {
        LocalDateTime eventDateTime = LocalDateTime.parse(event.startDate + "T" + event.startTime);
        LocalDateTime reminderTime = eventDateTime.minusMinutes(event.reminderOffset);

        long triggerMillis = reminderTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        if (System.currentTimeMillis() > triggerMillis) {
            Log.d("ReminderReceiver", "Thời gian nhắc đã qua, không tạo notification cho sự kiện ID: " + event.id);
            return;
        }

        Intent notifIntent = new Intent(context, NotificationPublisher.class);
        notifIntent.putExtra("title", event.title);
        notifIntent.putExtra("description", event.description);
        notifIntent.putExtra("eventId", event.id);

        // Use a hashcode of the UUID string as the request code
        int requestCode = event.id.hashCode();
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                notifIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent);
        }
    }
}

