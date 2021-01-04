package com.example.personalproductivity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    public static final String WORK_NOTIFICATION_CHANNEL_ID = "com.example.personalproductivity.MainActivity work notification";
    boolean timerOn = false;
    private WorkOrBreakTimer workTimer;
    private WorkOrBreakTimer breakTimer;

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
        setContentView(R.layout.activity_main);
        createTimers(3);
    }

    private void createTimers(long workHours) {
        workTimer = new WorkOrBreakTimer(findViewById(R.id.button_work),
                findViewById(R.id.progress_work), this, workHours * 3600 * 1000, true);
        breakTimer = new WorkOrBreakTimer(findViewById(R.id.button_break),
                findViewById(R.id.progress_break), this,  3600 * 1000, false);
    }

    public void startStop(View view) {
        if (timerOn) {
            workTimer.pause();
            breakTimer.start();
        } else {
            workTimer.start();
            breakTimer.pause();
        }
        timerOn = !timerOn;
    }

    public void breakTimerFinished() {
        startStop(null);
        workTimer.disable();
        breakTimer.disable();
    }
}