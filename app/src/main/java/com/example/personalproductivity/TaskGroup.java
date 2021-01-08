package com.example.personalproductivity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.List;

@Entity
public class TaskGroup {

    @PrimaryKey(autoGenerate = true)
    public int id;
    public String parentProjectName;

    public String name;

    @Ignore
    private List<Task> taskList;

    public List<Task> getTaskList() {
        return taskList;
    }

    public void setTaskList(List<Task> taskList) {
        this.taskList = taskList;
    }
}
