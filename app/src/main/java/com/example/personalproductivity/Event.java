package com.example.personalproductivity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import java.io.Serializable;

@Entity
@Data
public class Event implements TimeRangeItem, Serializable {
    @EqualsAndHashCode.Include @PrimaryKey(autoGenerate = true) private int id;
    @NonNull private String name;
    @NonNull private long daysSinceEpoch;
    @NonNull private long startTimeStamp;
    @NonNull private long length;
}

