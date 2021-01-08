package com.example.personalproductivity;

import android.content.Context;
import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class ProjectRepository {

    private final ProjectDao projectDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public ProjectRepository(Context context) {
        projectDao = ProjectDatabase.getDatabase(context).projectDao();
    }

    public LiveData<List<Project>> getProjects() {
        return projectDao.getProjects();
    }

    public void doAction(Consumer<ProjectDao> f) {
        executor.execute(() -> f.accept(projectDao));
    }
}
