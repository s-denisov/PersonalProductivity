package com.example.personalproductivity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    public static final String WORK_NOTIFICATION_CHANNEL_ID = "com.example.personalproductivity.MainActivity work notification";
    boolean workTimerOn = false;
    private WorkOrBreakTimer workTimer;
    private WorkOrBreakTimer breakTimer;
    private long workedToday = 0;
    private ProjectRepository projectRepository;

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(WORK_NOTIFICATION_CHANNEL_ID, "Work Notification", importance);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createNotificationChannel();
    }
}