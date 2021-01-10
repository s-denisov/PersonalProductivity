package com.example.personalproductivity;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Objects;

public class ProjectListFragment extends Fragment {

    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("project", "onCreateView");
        view = inflater.inflate(R.layout.fragment_project_list, container, false);
        ProjectRepository repository = new ProjectRepository(getActivity());
        LiveData<List<Project>> projects = repository.getProjects();
        ProjectRecyclerViewAdapter adapter = new ProjectRecyclerViewAdapter(new ProjectRecyclerViewAdapter.ProjectDiff());
        RecyclerView recyclerView = view.findViewById(R.id.recyclerview_projects);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        projects.observe(Objects.requireNonNull(getActivity()), adapter::submitList);
        Project p = new Project();
        p.name = "Hello";
        repository.doAction(dao -> dao.insertProject(p));
        return view;
    }
}
