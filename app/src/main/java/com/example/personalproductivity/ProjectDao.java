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

    @Query("SELECT * FROM TaskGroup WHERE parentProjectId=:projectId ORDER BY id DESC")
    LiveData<List<TaskGroup>> getTaskGroups(int projectId);

    @Query("SELECT * FROM Task WHERE parentTaskGroupId=:taskGroupId ORDER BY lastUsed DESC")
    LiveData<List<Task>> getTasks(int taskGroupId);

    @Query("SELECT * FROM Task WHERE completionStatus='IN_PROGRESS' ORDER BY lastUsed DESC")
    LiveData<List<Task>> getTasksLastUsed();

    @Query("SELECT * FROM Task WHERE completionStatus='TODO_LATER' OR completionStatus='IN_PROGRESS' ORDER BY priority DESC, lastUsed DESC")
    LiveData<List<Task>> getTasksByPriority();


    @Query("SELECT Task.*, " +
            "Task.priority AS priority, SUM(TaskTimeRecord.length) AS totalLength, :daysSinceEpoch AS daysSinceEpoch, " +
            "SUM(CASE WHEN TaskTimeRecord.daysSinceEpoch=:daysSinceEpoch THEN TaskTimeRecord.length ELSE 0 END) AS lengthToday" +
            " FROM Task LEFT JOIN TaskTimeRecord ON TaskTimeRecord.taskId=Task.id WHERE Task.parentTaskGroupId=:taskGroupId " +
            "GROUP BY Task.id ORDER BY lastUsed DESC")
    LiveData<List<TaskView>> getTaskViewsByTaskGroup(int taskGroupId, long daysSinceEpoch);

    @Query("SELECT Task.*, " +
            "Task.priority AS priority, SUM(TaskTimeRecord.length) AS totalLength, :daysSinceEpoch AS daysSinceEpoch, " +
            "SUM(CASE WHEN TaskTimeRecord.daysSinceEpoch=:daysSinceEpoch THEN TaskTimeRecord.length ELSE 0 END) AS lengthToday" +
            " FROM Task LEFT JOIN TaskTimeRecord ON TaskTimeRecord.taskId=Task.id WHERE Task.completionStatus='IN_PROGRESS' " +
            "OR Task.completionStatus='TODO_LATER' GROUP BY Task.id ORDER BY Task.priority DESC")
    LiveData<List<TaskView>> getTaskViews(long daysSinceEpoch);

    @Query("SELECT * FROM Project WHERE id=" +
            "(SELECT parentProjectId FROM TaskGroup WHERE id=(SELECT parentTaskGroupId FROM Task WHERE id=:taskId))")
    LiveData<Project> getProjectFromTask(int taskId);

    @Query("SELECT * FROM Task WHERE id=:id")
    LiveData<Task> getTask(int id);

    @Query("SELECT SUM(length) FROM TaskTimeRecord WHERE taskId=:taskId")
    LiveData<Long> findTimeSpent(int taskId);

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


    @Delete
    void deleteProject(Project project);

    @Delete
    void deleteTaskGroup(TaskGroup taskGroup);

    @Delete
    void deleteTask(Task task);




    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertTaskRecord(TaskTimeRecord record);

    @Update(onConflict = OnConflictStrategy.IGNORE)
    void updateTaskRecord(TaskTimeRecord record);

    @Query("SELECT * FROM TaskTimeRecord WHERE startTimeStamp=:millisSinceEpoch")
    LiveData<TaskTimeRecord> getTaskRecordByTime(long millisSinceEpoch);

    @Query("SELECT * FROM TaskTimeRecord WHERE daysSinceEpoch=:daysSinceEpoch")
    LiveData<List<TaskTimeRecord>> getTaskRecordsByDay(long daysSinceEpoch);

    @Query("SELECT * FROM TaskTimeRecord WHERE taskId=:taskId")
    LiveData<List<TaskTimeRecord>> getTaskRecordsByTaskId(int taskId);


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertDay(Day day);

    @Update
    void updateDay(Day day);

    @Query("SELECT * FROM Day WHERE daysSinceEpoch=:daysSinceEpoch")
    LiveData<Day> getDay(long daysSinceEpoch);

    @Query("SELECT Day.*, SUM(Event.schoolLessons) AS totalSchoolLessons, SUM(Event.choreLength) AS totalChoreLength, " +
            "SUM(Event.funLength) AS totalFunLength " +
            "FROM Day LEFT JOIN Event ON Day.daysSinceEpoch=Event.daysSinceEpoch " +
            "WHERE Day.daysSinceEpoch=:daysSinceEpoch GROUP BY Day.daysSinceEpoch")
    LiveData<DayView> getDayView(long daysSinceEpoch);


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertEvent(Event event);

    @Update
    void updateEvent(Event event);

    @Query("SELECT * FROM Event WHERE daysSinceEpoch=:daysSinceEpoch")
    LiveData<List<Event>> getEventsByDay(long daysSinceEpoch);
}