package com.example.personalproductivity;

import androidx.lifecycle.LiveData;
import androidx.room.*;

import java.util.List;

@Dao
public abstract class ProjectDao {

    @Transaction @Query("SELECT * FROM Project ORDER BY name ASC")
    public abstract LiveData<List<Project>> getProjects();

    @Query("SELECT * FROM Project WHERE name=:name")
    abstract Project getProject(String name);

    @Query("SELECT * FROM TaskGroup WHERE parentProjectName=:projectName")
    public abstract List<TaskGroup> getTaskGroups(String projectName);

    @Query("SELECT * FROM Task WHERE parentTaskGroupId=:taskGroupId")
    public abstract List<Task> getTasks(int taskGroupId);


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertProject(Project project);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertTaskGroup(TaskGroup taskGroup);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertTask(Task task);
}