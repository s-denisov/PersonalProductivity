package com.example.personalproductivity;

import android.annotation.SuppressLint;
import android.os.CountDownTimer;
import lombok.Getter;
import lombok.Setter;

import java.util.function.Consumer;

public class WorkOrBreakTimer {

    private CountDownTimer timer;
    @Getter private long timeLeft;
    @Setter private Consumer<Long> onTick;
    @Setter private Runnable onFinish;
    private final long initialTime;

    @SuppressLint("DefaultLocale")
    public static String toHoursMinutes(long millis) {
        long seconds = millis / 1000;
        return String.format("%d:%02d", seconds / 3600, seconds % 3600 / 60);
    }

    @SuppressLint("DefaultLocale")
    public static String formatMilliseconds(long millis) {
        if (millis < 0) return "-" + formatMilliseconds(-millis);
        long seconds = millis / 1000;
        long hours = seconds / 3600;
        String hoursString = hours == 0 ? "" : hours + ":";
        return String.format("%s%02d:%02d", hoursString, seconds % 3600 / 60, seconds % 60);
    }

    private void createCountDownTimer() {
        timer = new CountDownTimer(timeLeft, 1000) {

            @SuppressLint("SetTextI18n")
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeft = millisUntilFinished;
                onTick.accept(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                onFinish.run();
            }
        };
        timer.start();
    }

    public WorkOrBreakTimer(long initialTime) {
        timeLeft = initialTime;
        this.initialTime = initialTime;
    }

    public void start() {
        if (timer == null) createCountDownTimer();
    }

    public void pause() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public long findTimeSpent() {
        return initialTime - timeLeft;
    }
}
