package com.example.personalproductivity;

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

    public static final long SESSION_LENGTH = 1800_000;
//    public static final long SESSION_LENGTH = 10_000; // For testing
    public static final long SHORT_BREAK_LENGTH = 300_000;
//    public static final long SHORT_BREAK_LENGTH = 70_000; // For testing
    public static final long LONG_BREAK_LENGTH = 1800_000;
//    public static final long LONG_BREAK_LENGTH = 70_000; // For testing
    public static final long MAX_SESSION_LENGTH = SESSION_LENGTH * 4 / 3;
    public static final long MAX_BLOCK_LENGTH = 5 * SESSION_LENGTH;


    public static List<ScheduledItem> schedule(long start, long duration /* without breaks */, List<Event> events) {
        events.sort(Comparator.comparingLong(Event::getStartTimeStamp));
        List<ScheduledItem> result = new ArrayList<>();
        for (Event event : events) {
            long durationBefore = Math.min(duration, event.getStartTimeStamp() - start);
            Log.d("project", "Before: " + durationBefore / 60_000);
            if (durationBefore > 600_000) {
                List<ScheduledItem> beforeEvent = schedule(start, durationBefore, durationBefore != duration);
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

    private static List<ScheduledItem> schedule(long start, long duration, boolean breaksIncludedInDuration) {
        List<ScheduledItem> result = new ArrayList<>();

        for (int i = 1; i % 4 != 1 || duration >= 8 * SESSION_LENGTH; i++) {
            result.add(new ScheduledItem(start, SESSION_LENGTH, true));
            long breakTime = i % 4 == 0 ? LONG_BREAK_LENGTH : SHORT_BREAK_LENGTH;
            start += SESSION_LENGTH + breakTime;
            duration -= SESSION_LENGTH;
            if (breaksIncludedInDuration) duration -= breakTime;
        }

        for (int i = 0; i < 2; i++) {
            long duration2;
            //if ((!breaksIncludedInDuration && duration > 2.5 * 3600_000) || duration > 3 * 3600_000) {
            if (duration > MAX_BLOCK_LENGTH) {
                Log.d("project", String.valueOf(duration / 60_000));
                duration2 = duration / 2;
                if (breaksIncludedInDuration) duration2 -= LONG_BREAK_LENGTH / 2;
            } else {
                duration2 = duration;
                i++;
            }
            Log.d("project", String.valueOf(duration2 / 60_000));
            while (duration2 > 2 * MAX_SESSION_LENGTH) {
                Log.d("project", String.valueOf(duration2 / 60_000));
                result.add(new ScheduledItem(start, SESSION_LENGTH, true));
                start += SESSION_LENGTH + SHORT_BREAK_LENGTH;
                duration2 -= SESSION_LENGTH;
                if (breaksIncludedInDuration) duration2 -= SHORT_BREAK_LENGTH;
            }
//            if (duration2 <= 2 * MAX_SESSION_LENGTH && (!breaksIncludedInDuration || duration2 > SHORT_BREAK_LENGTH)) {

            Log.d("project", String.valueOf(duration2 / 60_000));

            if (duration2 <= MAX_SESSION_LENGTH) {
                long breakLength = i == 0 ? LONG_BREAK_LENGTH : SHORT_BREAK_LENGTH;
                duration2 -= breakLength;
                if (duration2 > 0) {
                    result.add(new ScheduledItem(start, duration2, true));
                    start += duration2 + breakLength;
                }
            } else {
                if (breaksIncludedInDuration) duration2 -= SHORT_BREAK_LENGTH;
                if (duration2 > 600_000) {
                    result.add(new ScheduledItem(start, duration2 / 2, true));
                    start += duration2 / 2 + SHORT_BREAK_LENGTH;
                    result.add(new ScheduledItem(start, duration2 / 2, true));
                    start += duration2 / 2 + LONG_BREAK_LENGTH;
                }
            }
//            }
        }
        return result;
    }

}
