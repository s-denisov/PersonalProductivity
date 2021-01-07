package com.example.personalproductivity;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class TaskGroupWithTask {
    @Embedded public TaskGroup taskGroup;
    @Relation(parentColumn = "taskGroupId", entityColumn = "parentTaskGroupId")
    public List<Task> tasks;
}
