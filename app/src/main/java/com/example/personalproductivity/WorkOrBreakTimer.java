package com.example.personalproductivity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.ProgressBar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class WorkOrBreakTimer {

    private final Button representation;
    private final ProgressBar remainingPercentage;
    private final MainActivity context;
    private CountDownTimer timer;
    private final boolean isWorkTimer;
    private boolean breakAlmostOverNotificationSent = false;
    private long timeLeft;
    private final long initialTime;

    @SuppressLint("DefaultLocale")
    private String formatMilliseconds(long millis) {
        long seconds = millis / 1000;
        return String.format("%d:%02d:%02d", seconds / 3600, seconds % 3600 / 60, seconds % 60);
    }

    private void createNotification(String text) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MainActivity.WORK_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(text);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(1, builder.build());
    }

    private void createCountDownTimer() {
        timer = new CountDownTimer(timeLeft, 1000) {

            @SuppressLint("SetTextI18n")
            @Override
            public void onTick(long millisUntilFinished) {
                representation.setText(formatMilliseconds(millisUntilFinished));
                timeLeft = millisUntilFinished;
                remainingPercentage.setProgress((int) (millisUntilFinished * 100 / initialTime));
                if (!isWorkTimer && millisUntilFinished < 5 * 60 * 1000 && !breakAlmostOverNotificationSent) {
                    createNotification("Break almost over");
                    breakAlmostOverNotificationSent = true;
                }
            }

            @Override
            public void onFinish() {
                representation.setTextColor(isWorkTimer ? Color.GREEN : Color.RED);
                representation.setText("Done!");
                remainingPercentage.setProgress(0);
                createNotification((isWorkTimer ? "Work" : "Break") + " complete");
                if (!isWorkTimer) context.breakTimerFinished();
            }
        };
        timer.start();
    }

    public WorkOrBreakTimer(Button representation, ProgressBar remainingPercentage,
                            MainActivity context, long initialTime, boolean isWorkTimer) {
        this.representation = representation;
        this.context = context;
        timeLeft = initialTime;
        this.initialTime = initialTime;
        this.isWorkTimer = isWorkTimer;
        this.remainingPercentage = remainingPercentage;
        representation.setText(formatMilliseconds(initialTime));
    }

    public void start() {
        createCountDownTimer();
        representation.setEnabled(true);
    }

    public void pause() {
        if (timer != null) timer.cancel();
        disable();
    }

    public void disable() {
        representation.setEnabled(false);
    }
}
