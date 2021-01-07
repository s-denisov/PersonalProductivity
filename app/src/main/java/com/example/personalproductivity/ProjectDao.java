package com.example.personalproductivity;

import androidx.lifecycle.LiveData;
import androidx.room.*;

import java.util.List;

@Dao
public interface ProjectDao {

    @Transaction @Query("SELECT * FROM Project ORDER BY name ASC")
    LiveData<List<ProjectWithTaskGroup>> getProjects();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertProject(Project project);
}