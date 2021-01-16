package com.example.personalproductivity;

import androidx.lifecycle.LiveData;
import androidx.room.*;

import java.util.List;

@Dao
public interface ProjectDao {

    @Transaction @Query("SELECT * FROM Project ORDER BY name ASC")
    LiveData<List<Project>> getProjects();

    @Query("SELECT * FROM Project WHERE name=:name")
    Project getProject(String name);

    @Query("SELECT * FROM TaskGroup WHERE parentProjectName=:projectName")
    LiveData<List<TaskGroup>> getTaskGroups(String projectName);

    @Query("SELECT * FROM Task WHERE parentTaskGroupId=:taskGroupId")
    LiveData<List<Task>> getTasks(int taskGroupId);

    @Query("SELECT * FROM Task WHERE completionStatus='IN_PROGRESS' ORDER BY lastUsed DESC")
    LiveData<List<Task>> getTasksLastUsed();


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertProject(Project project);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTaskGroup(TaskGroup taskGroup);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTask(Task task);


    @Update
    void updateProject(Project project);

    @Update
    void updateTaskGroup(TaskGroup taskGroup);

    @Update
    void updateTask(Task task);




    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertTaskRecord(TaskTimeRecord record);

    @Update(onConflict = OnConflictStrategy.IGNORE)
    void updateTaskRecord(TaskTimeRecord record);
    
    @Query("SELECT * FROM TaskTimeRecord WHERE daysSinceEpoch=:daysSinceEpoch")
    LiveData<List<TaskTimeRecord>> getTaskRecordsByDay(long daysSinceEpoch);
}