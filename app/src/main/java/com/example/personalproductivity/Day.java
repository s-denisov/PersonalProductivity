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
}
