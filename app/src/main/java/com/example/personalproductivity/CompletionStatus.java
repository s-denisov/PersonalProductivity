package com.example.personalproductivity;

import androidx.room.TypeConverter;

public enum CompletionStatus {
    IN_PROGRESS, COMPLETE, FAILED;

    @TypeConverter
    public static String fromCompletionStatus(CompletionStatus status) {
        return status.toString();
    }

    @TypeConverter
    public static CompletionStatus toCompletionStatus(String str) {
        return CompletionStatus.valueOf(str);
    }
}
