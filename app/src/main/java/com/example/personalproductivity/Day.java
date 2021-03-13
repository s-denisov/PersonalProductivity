package com.example.personalproductivity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import lombok.Data;
import lombok.NonNull;

@Data
@Entity
public class Day {
    @PrimaryKey @NonNull private long daysSinceEpoch;
    @NonNull private long targetWorkTime;
    @NonNull private long schoolTime; // unadjusted. should be e.g multiplied by 0.8
    @NonNull private long missedSleep;

    public long subtractPrivateStudy(long millisWorked) {
        long workedModifier = schoolTime == 0 ? 0 : schoolTime - 5 * 3600_000;
        return millisWorked + workedModifier;
    }

    public long findTargetWorkTime() {
        return targetWorkTime + missedSleep;
    }
}
