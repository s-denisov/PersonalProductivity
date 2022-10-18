package com.example.personalproductivity.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.personalproductivity.db.Project;
import com.example.personalproductivity.db.ProjectDao;
import com.example.personalproductivity.db.ProjectDatabase;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class ProjectViewModel extends AndroidViewModel {

    private final ProjectDao projectDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public ProjectViewModel(@NonNull Application application) {
        super(application);
        projectDao = ProjectDatabase.getDatabase(application).projectDao();
    }

    public LiveData<List<Project>> getProjects() {
        return projectDao.getProjects();
    }

    public void doAction(Consumer<ProjectDao> fn) {
        executor.execute(() -> fn.accept(projectDao));
    }

    public ProjectDao getProjectDao() {
        return projectDao;
    }
}
