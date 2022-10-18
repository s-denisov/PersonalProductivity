package com.example.personalproductivity.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.example.personalproductivity.db.types.TimeRangeItem;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

@Entity
@Data
public class TaskTimeRecord implements TimeRangeItem {
    @EqualsAndHashCode.Include @PrimaryKey @NonNull private long startTimeStamp;
    @NonNull private long daysSinceEpoch;
    private long length = 0;
    @NonNull private int taskId;
    @NonNull private boolean privateStudy;

    public void addMilliseconds(long milliseconds) {
        length += milliseconds;
    }
}
