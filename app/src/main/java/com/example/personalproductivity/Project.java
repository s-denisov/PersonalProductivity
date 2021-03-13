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
public class Project implements TaskOrParent {

    @PrimaryKey(autoGenerate = true) @Getter
    public int id;
    @Getter @Setter
    public String name;
    @Getter @Setter
    public CompletionStatus completionStatus = CompletionStatus.IN_PROGRESS;
//    public boolean countAsWork = true;

    @Ignore
    private List<TaskGroup> taskGroupList;

    public List<TaskGroup> getTaskGroupList() {
        return taskGroupList;
    }

    public void setTaskGroupList(List<TaskGroup> taskGroupList) {
        this.taskGroupList = taskGroupList;
    }

    @Override
    public LiveData<List<TaskGroup>> getChildren(ProjectDao source) {
        return source.getTaskGroups(id);
    }

    @Override
    public void setParentId(int parentId) {}

    @Override
    public void updateInDb(ProjectViewModel viewModel) {
        viewModel.doAction(dao -> dao.updateProject(this));
    }

    @Override
    public void deleteInDb(ProjectViewModel viewModel) {
        viewModel.doAction(dao -> dao.deleteProject(this));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Project project = (Project) o;
        return name.equals(project.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
