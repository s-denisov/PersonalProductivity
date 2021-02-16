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

    @Query("SELECT * FROM Project WHERE name=" +
            "(SELECT parentProjectName FROM TaskGroup WHERE id=(SELECT parentTaskGroupId FROM Task WHERE id=:taskId))")
    LiveData<Project> getProjectFromTask(int taskId);

    @Query("SELECT * FROM Task WHERE id=:id")
    LiveData<Task> getTask(int id);


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertProject(Project project);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertTaskGroup(TaskGroup taskGroup);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
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

    @Delete
    void deleteTaskRecord(TaskTimeRecord record);

    @Query("SELECT * FROM TaskTimeRecord WHERE daysSinceEpoch=:daysSinceEpoch")
    LiveData<List<TaskTimeRecord>> getTaskRecordsByDay(long daysSinceEpoch);


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertDay(Day day);

    @Update
    void updateDay(Day day);

    @Query("SELECT * FROM Day where daysSinceEpoch=:daysSinceEpoch")
    LiveData<Day> getDay(long daysSinceEpoch);



    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertEvent(Event event);

    @Update
    void updateEvent(Event event);

    @Query("SELECT * FROM Event WHERE daysSinceEpoch=:daysSinceEpoch")
    LiveData<List<Event>> getEventsByDay(long daysSinceEpoch);
}