package com.example.personalproductivity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Task {

    @PrimaryKey(autoGenerate = true)
    public int id;
    public int parentTaskGroupId;

    public String name;
    public boolean succeeded;
    public boolean failed;
    public long timeThinking;
    public long timeReading;
    public long timeWriting;
}
