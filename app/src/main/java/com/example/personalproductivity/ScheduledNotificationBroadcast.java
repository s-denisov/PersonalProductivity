package com.example.personalproductivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class ScheduledNotificationBroadcast extends BroadcastReceiver {
    public static void createNotification(Context context, String text) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MainActivity.WORK_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(text);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(1, builder.build());
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        createNotification(context, "Break almost over!");
    }
}
