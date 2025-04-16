package com.example.calendar_app;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.calendar_app.Activities.ReminderDetailActivity;

public class NotificationPublisher extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("title");
        String description = intent.getStringExtra("description");
        String eventId = intent.getStringExtra("eventId");
        Log.d("Notisss", "id " + eventId);
        
        if (eventId == null) {
            Log.e("NotificationPublisher", "Event ID not found in intent");
            return;
        }
        
        Intent detailIntent = new Intent(context, ReminderDetailActivity.class);
        detailIntent.putExtra("EVENT_ID", eventId);
        detailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // Use a hashcode of the UUID string as the request code
        int requestCode = eventId.hashCode();
        
        PendingIntent contentIntent = PendingIntent.getActivity(
                context,
                requestCode,
                detailIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        String channelId = "event_channel";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId, "Event Reminders", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setContentTitle(title)
                .setContentText(description)
                .setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.ic_notification)
                .setAutoCancel(true);

        // Generate a unique notification ID from the event ID
        int notificationId = requestCode;
        notificationManager.notify(notificationId, builder.build());
    }
}
