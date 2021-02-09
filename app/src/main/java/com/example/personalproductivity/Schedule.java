package com.example.personalproductivity;

import lombok.Data;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class Schedule {

    @Data
    public static class ScheduledItem {
        private final long start; private final long length; private final boolean isTask;
    }

    public static List<ScheduledItem> schedule(long start, long duration) {

        List<ScheduledItem> result = new ArrayList<>();
        SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.getDefault());
        format.setTimeZone(TimeZone.getDefault());

        for (int i = 1; i % 4 != 1 || duration >= 4 * 3600_000; i++) {
            result.add(new ScheduledItem(start, 1800_000, true));
//            System.out.println(format.format(start) + " - " + format.format(start + 1800_000) + " " + i);
            long breakTime = i % 4 == 0 ? 1800_000 : 300_000;
//            if (i % 4 == 0) System.out.println();
            start += 1800_000 + breakTime;
            duration -= 1800_000;
        }

        for (int i = 0; i < 2; i++) {
            long duration2 = duration / 2;
            while (true) {
                if (duration2 <= 3600_000) {
//                    System.out.println(format.format(start) + " - " + format.format(start + duration2 / 2));
                    result.add(new ScheduledItem(start, duration2 / 2, true));
                    start += duration2 / 2 + 300_000;
//                    System.out.println(format.format(start) + " - " + format.format(start + duration2 / 2));
                    result.add(new ScheduledItem(start, duration2 / 2, true));
                    start += duration2 / 2 + 1800_000;
                    break;
                }
//                System.out.println(format.format(start) + " - " + format.format(start + 1800_000));
                start += 1800_000 + 300_000;
                duration2 -= 1800_000;
            }
        }
        return result;
    }
}
