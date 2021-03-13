package com.example.personalproductivity;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Objects;

@Entity
@TypeConverters({CompletionStatus.class, Priority.class})
public class Task implements TaskOrParent { // TODO: for some reason, crashes on start if TaskOrParent not implemented. Seems to be due to navigation.

    @PrimaryKey(autoGenerate = true) @Getter
    public int id;
    public int parentTaskGroupId;

    public String name;
    public long lastUsed = 0;
    @Getter @Setter public CompletionStatus completionStatus = CompletionStatus.IN_PROGRESS;
    @Getter @Setter @NonNull public Priority priority = Priority.EXPECTED;
    @Getter @Setter @NonNull public long deadlineDate;
    @Getter @Setter @NonNull public long expectedTime;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LiveData<? extends List<? extends TaskOrParent>> getChildren(ProjectDao source) {
        return null;
    }

    public void setParentId(int parentId) {
        parentTaskGroupId = parentId;
    }

    public void updateInDb(ProjectViewModel viewModel) {
        viewModel.doAction(dao -> dao.updateTask(this));
    }

    public void deleteInDb(ProjectViewModel viewModel) {
        viewModel.doAction(dao -> dao.deleteTask(this));
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

    @NonNull
    @Override
    public String toString() {
        return name;
    }
}
