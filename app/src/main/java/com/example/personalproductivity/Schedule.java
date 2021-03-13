package com.example.personalproductivity;

import android.content.SharedPreferences;
import android.util.Log;
import androidx.annotation.NonNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Schedule {

    @Data
    public static class ScheduledItem {
        private final long start; private final long length; private final boolean isTask;

        private String formatTimeSinceEpoch(long time) {
            LocalDateTime dateTime = LocalDateTime.ofEpochSecond(time / 1000, 0, OffsetDateTime.now().getOffset());
            return dateTime.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        }

        @NonNull
        @Override
        public String toString() {
            return formatTimeSinceEpoch(start) + " - " + formatTimeSinceEpoch(start + length);
        }
    }

    public long sessionLength;
    public long shortBreakLength;
    public long longBreakLength;
    public long maxSessionLength;
    public long maxBlockLength;


    public Schedule(SharedPreferences settings) {
        sessionLength = keyToMillis(settings, SettingsFragment.SESSION_LENGTH_KEY);
        shortBreakLength = keyToMillis(settings, SettingsFragment.SHORT_BREAK_LENGTH_KEY);
        longBreakLength = keyToMillis(settings, SettingsFragment.LONG_BREAK_LENGTH_KEY);
        maxSessionLength = keyToMillis(settings, SettingsFragment.MAX_SESSION_LENGTH_KEY);
        maxBlockLength = keyToMillis(settings, SettingsFragment.MAX_BLOCK_LENGTH_KEY);
    }

    private static int keyToMillis(SharedPreferences settings, String key) {
        return Integer.parseInt(settings.getString(key, "0")) * 60_000;
    }

    public List<ScheduledItem> schedule(long start, long duration /* without breaks */, List<Event> events) {
        events.sort(Comparator.comparingLong(Event::getStartTimeStamp));
        List<ScheduledItem> result = new ArrayList<>();
        for (Event event : events) {
            long durationBefore = event.getStartTimeStamp() - start; // TODO: use duration if duration is lower (but including breaks)
            Log.d("project", "Before: " + durationBefore / 60_000);
            if (durationBefore > 600_000) {
                List<ScheduledItem> beforeEvent = schedule(start, durationBefore, true);
                result.addAll(beforeEvent);
                for (ScheduledItem item : beforeEvent) {
                    duration -= item.length;
                }
            }
            start = Math.max(start, event.getStartTimeStamp() + event.getLength());
        }
        if (duration > 0) result.addAll(schedule(start, duration, false));
        return result;
    }

    private List<ScheduledItem> schedule(long start, long duration, boolean breaksIncludedInDuration) {
        LocalDateTime dateTime = LocalDateTime.ofEpochSecond(start / 1000, 0, OffsetDateTime.now().getOffset());
        Log.d("project", dateTime.toLocalTime() + " ---------------------");

        List<ScheduledItem> result = new ArrayList<>();

        for (int i = 1; i % 4 != 1 || duration >= 8 * sessionLength; i++) {
            result.add(new ScheduledItem(start, sessionLength, true));
            long breakTime = i % 4 == 0 ? longBreakLength : shortBreakLength;
            start += sessionLength + breakTime;
            duration -= sessionLength;
            if (breaksIncludedInDuration) duration -= breakTime;
        }

        for (int i = 0; i < 2; i++) {
            long duration2;
            //if ((!breaksIncludedInDuration && duration > 2.5 * 3600_000) || duration > 3 * 3600_000) {
            if (duration > maxBlockLength) {
                Log.d("project", String.valueOf(duration / 60_000));
                duration2 = duration / 2;
                if (breaksIncludedInDuration) duration2 -= longBreakLength / 2;
            } else {
                duration2 = duration;
                i++;
            }
            Log.d("project", String.valueOf(duration2 / 60_000));
            while (duration2 > 2 * maxSessionLength) {
                Log.d("project", String.valueOf(duration2 / 60_000));
                result.add(new ScheduledItem(start, sessionLength, true));
                start += sessionLength + shortBreakLength;
                duration2 -= sessionLength;
                if (breaksIncludedInDuration) duration2 -= shortBreakLength;
            }
//            if (duration2 <= 2 * MAX_SESSION_LENGTH && (!breaksIncludedInDuration || duration2 > SHORT_BREAK_LENGTH)) {

            Log.d("project", String.valueOf(duration2 / 60_000));

            if (duration2 <= maxSessionLength) {
                long breakLength = i == 0 ? longBreakLength : shortBreakLength;
                duration2 -= breakLength;
                if (duration2 > 0) {
                    result.add(new ScheduledItem(start, duration2, true));
                    start += duration2 + breakLength;
                }
            } else {
                if (breaksIncludedInDuration) duration2 -= shortBreakLength;
                if (duration2 > 600_000) {
                    result.add(new ScheduledItem(start, duration2 / 2, true));
                    start += duration2 / 2 + shortBreakLength;
                    result.add(new ScheduledItem(start, duration2 / 2, true));
                    start += duration2 / 2 + longBreakLength;
                }
            }
//            }
        }
        return result;
    }

}
