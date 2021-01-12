package com.example.personalproductivity;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ProjectListFragment extends Fragment {

    private ProjectViewModel viewModel;
    private TaskOrParentType type;
    private ProjectRecyclerViewAdapter adapter;
    private TaskOrParent parent;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        parent = getArguments() == null ? null : (TaskOrParent) getArguments().getSerializable("parent");
        type = parent == null ? TaskOrParentType.PROJECT :
                parent instanceof Project ? TaskOrParentType.TASK_GROUP : TaskOrParentType.TASK;

        View view = inflater.inflate(R.layout.fragment_project_list, container, false);
        viewModel = new ViewModelProvider(Objects.requireNonNull(getActivity())).get(ProjectViewModel.class);
        LiveData<? extends List<? extends TaskOrParent>> taskOrParentList =
                parent == null ? viewModel.getProjects() : parent.getChildren(viewModel.getProjectDao());
        adapter = new ProjectRecyclerViewAdapter(new ProjectRecyclerViewAdapter.ProjectDiff(), this::setChildAsState);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerview_projects);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        taskOrParentList.observe(Objects.requireNonNull(getActivity()), adapter::convertAndSubmitList);
        view.findViewById(R.id.fab_create_project).setOnClickListener(this::createProject);
        return view;
    }

    public void createProject(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
        builder.setTitle("New " + type);

        FrameLayout container = new FrameLayout(getActivity());
        container.setPadding(50, 5, 40, 5);
        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint(type.toString().substring(0, 1).toUpperCase(Locale.ROOT) + type.toString().substring(1) + " name");
        container.addView(input);
        builder.setView(container);

        builder.setPositiveButton("Submit", (dialog, which) -> {
            switch (type) {
                case PROJECT:
                    Project newProject = new Project();
                    newProject.setName(input.getText().toString());
                    viewModel.doAction(dao -> dao.insertProject(newProject));
                    break;
                case TASK_GROUP:
                    TaskGroup newTaskGroup = new TaskGroup();
                    newTaskGroup.setName(input.getText().toString());
                    if (parent instanceof Project) newTaskGroup.parentProjectName = parent.getName();
                    viewModel.doAction(dao -> dao.insertTaskGroup(newTaskGroup));
                    break;
                case TASK:
                    Task newTask = new Task();
                    newTask.setName(input.getText().toString());
                    if (parent instanceof TaskGroup) newTask.parentTaskGroupId = ((TaskGroup) parent).id;
                    viewModel.doAction(dao -> dao.insertTask(newTask));
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void setChildAsState(TaskOrParent newParent) {
        if (newParent instanceof Task) return;
        /*
        LiveData<? extends List<? extends TaskOrParent>> tasks = newParent.getChildren(viewModel.getProjectDao());
        tasks.observe(Objects.requireNonNull(getActivity()), adapter::convertAndSubmitList);
        switch (type) {
            case PROJECT: type = TaskOrParentType.TASK_GROUP; break;
            case TASK_GROUP: type = TaskOrParentType.TASK;
        }*/

        NavHostFragment n = (NavHostFragment) Objects.requireNonNull(getActivity()).getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        assert n != null;
        Bundle bundle = new Bundle();
        bundle.putSerializable("parent", newParent);
        n.getNavController().navigate(R.id.project_list_fragment, bundle);
    }
}
