package com.example.personalproductivity;

import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Objects;

public class ProjectListFragment extends Fragment {

    private View view;
    private ProjectRepository repository;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("project", "onCreateView");
        view = inflater.inflate(R.layout.fragment_project_list, container, false);
        repository = new ProjectRepository(getActivity());
        LiveData<List<Project>> projects = repository.getProjects();
        ProjectRecyclerViewAdapter adapter = new ProjectRecyclerViewAdapter(new ProjectRecyclerViewAdapter.ProjectDiff());
        RecyclerView recyclerView = view.findViewById(R.id.recyclerview_projects);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        projects.observe(Objects.requireNonNull(getActivity()), adapter::submitList);
        view.findViewById(R.id.fab_create_project).setOnClickListener(this::createProject);
        return view;
    }

    public void createProject(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
        builder.setTitle("New project");

        FrameLayout container = new FrameLayout(getActivity());
        container.setPadding(50, 5, 40, 5);
        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Project name");
        container.addView(input);
        builder.setView(container);

        builder.setPositiveButton("Submit", (dialog, which) -> {
            Project newProject = new Project();
            newProject.name = input.getText().toString();
            repository.doAction(dao -> dao.insertProject(newProject));
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();

    }
}
