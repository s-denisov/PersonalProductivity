package com.example.personalproductivity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

@Entity
@Data
public class TaskTimeRecord {
    @EqualsAndHashCode.Include @PrimaryKey @NonNull private long startTimeStamp;
    @NonNull private long daysSinceEpoch;
    private long length = 0;
    @NonNull private int taskId;

    public void addMilliseconds(long milliseconds) {
        length += milliseconds;
    }
}
