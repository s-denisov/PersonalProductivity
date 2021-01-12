package com.example.personalproductivity;

import androidx.lifecycle.LiveData;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.List;
import java.util.Objects;

@Entity
public class Task implements TaskOrParent {

    @PrimaryKey(autoGenerate = true)
    public int id;
    public int parentTaskGroupId;

    public String name;
    public boolean succeeded;
    public boolean failed;
    public long timeThinking;
    public long timeReading;
    public long timeWriting;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public LiveData<? extends List<? extends TaskOrParent>> getChildren(ProjectDao source) {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
