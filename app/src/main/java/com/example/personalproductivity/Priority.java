package com.example.personalproductivity;

import androidx.annotation.NonNull;
import androidx.room.TypeConverter;

public enum Priority {
    GUARANTEED(100, "Guaranteed"), RECOMMENDED(80, "Recommended"),
    EXPECTED(60, "Expected"), TARGET(40, "Target"), UNLIKELY(20, "Unlikely");

    private final int number;
    private final String string;
    Priority(int number, String string) {
        this.number = number;
        this.string = string;
    }

    public int getNumber() {
        return number;
    }

    @TypeConverter
    public static int toNumber(Priority priority) {
        return priority.number;
    }

    @TypeConverter
    public static Priority fromNumber(int number) {
        for (Priority priority : values()) {
            if (priority.number == number) {
                return priority;
            }
        }
        return EXPECTED;
    }

    @NonNull
    @Override
    public String toString() {
        return string;
    }
}
