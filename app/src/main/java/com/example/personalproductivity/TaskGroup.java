package com.example.personalproductivity;

import androidx.lifecycle.LiveData;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Objects;

@Entity
@TypeConverters({CompletionStatus.class})
public class TaskGroup implements TaskOrParent{

    @PrimaryKey(autoGenerate = true) @Getter
    public int id;
    public int parentProjectId;

    @Getter @Setter public String name;
    @Getter @Setter public CompletionStatus completionStatus = CompletionStatus.IN_PROGRESS;

    @Ignore
    private List<Task> taskList;

    public List<Task> getTaskList() {
        return taskList;
    }

    public void setTaskList(List<Task> taskList) {
        this.taskList = taskList;
    }

    @Override
    public LiveData<List<TaskView>> getChildren(ProjectDao source) {
        return source.getTaskViewsByTaskGroup(id, WorkTimerFragment.findDaysSinceEpoch());
    }

    @Override
    public void setParentId(int parentId) {
        parentProjectId = parentId;
    }

    @Override
    public void updateInDb(ProjectViewModel viewModel) {
        viewModel.doAction(dao -> dao.updateTaskGroup(this));
    }

    @Override
    public void deleteInDb(ProjectViewModel viewModel) {
        viewModel.doAction(dao -> dao.deleteTaskGroup(this));
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
