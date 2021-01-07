package com.example.personalproductivity;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class ProjectWithTaskGroup {
    @Embedded public Project project;
    @Relation(parentColumn = "name", entityColumn = "parentProjectName")
    public List<TaskGroup> taskGroups;
}
