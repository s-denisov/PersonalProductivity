package com.example.personalproductivity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class TaskGroup {

    @PrimaryKey(autoGenerate = true)
    public int taskGroupId;
    public String parentProjectName;

    public String name;
}
