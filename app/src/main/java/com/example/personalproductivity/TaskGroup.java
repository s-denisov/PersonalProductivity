package com.example.personalproductivity;

import androidx.lifecycle.LiveData;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.List;
import java.util.Objects;

@Entity
public class TaskGroup implements TaskOrParent{

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

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public LiveData<List<Task>> getChildren(ProjectDao source) {
        return source.getTasks(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskGroup taskGroup = (TaskGroup) o;
        return id == taskGroup.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
