package com.example.personalproductivity;

import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.AbstractMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class ProjectListFragment extends Fragment {

    private ProjectViewModel projectViewModel;
    private TaskOrParentType type;
    private TaskOrParent parent;
    private TaskOrParentType requestedType;
    private boolean isEventList;
    private NavController navController;
    private FragmentResultHelper helper;
    public static final String resultReference = "com.example.personalproductivity.ProjectListFragment.result";

    private FloatingActionButton fab;
    private RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_project_list, container, false);
//        viewModel = new ViewModelProvider(Objects.requireNonNull(getActivity())).get(ProjectListViewModel.class);
        projectViewModel = new ViewModelProvider(requireActivity()).get(ProjectViewModel.class);
        fab = view.findViewById(R.id.fab_create_project);
        recyclerView = view.findViewById(R.id.recyclerview_projects);
        if (getArguments() != null) {
            ProjectListFragmentArgs args = ProjectListFragmentArgs.fromBundle(getArguments());
            isEventList = args.getIsEventList();
            if (args.getIsRequest()) requestedType = args.getRequestedType();
        }
        if (getArguments() == null || !isEventList) {
            parent = getArguments() == null ? null : ProjectListFragmentArgs.fromBundle(getArguments()).getParent();
            type = parent == null ? TaskOrParentType.PROJECT :
                    parent instanceof Project ? TaskOrParentType.TASK_GROUP : TaskOrParentType.TASK;
            createProjectList();
        } else {
            createEventList();
        }


        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        navController = Navigation.findNavController(view);
        helper = new FragmentResultHelper(navController);
        AtomicBoolean notPopped = new AtomicBoolean(true);
        helper.getNavigationResultLiveData(resultReference).observe(requireActivity(), result -> {
            helper.setNavigationResult(resultReference, result);
            if (result != null && requestedType != null && notPopped.get()) {
                navController.popBackStack();
                notPopped.set(false);
            }
        });
        TabLayout tab = view.findViewById(R.id.tab_list_type);
        if (isEventList) Objects.requireNonNull(tab.getTabAt(1)).select();
        tab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (Objects.equals(tab.getText(), getString(R.string.project_list))) {
                    navController.navigate(ProjectListFragmentDirections.changeListType().setIsEventList(false));
                }
                else if (Objects.equals(tab.getText(), getString(R.string.event_list))) {
                    navController.navigate(ProjectListFragmentDirections.changeListType().setIsEventList(true));
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void createProjectList() {
        LiveData<? extends List<? extends TaskOrParent>> taskOrParentList =
                parent == null ? projectViewModel.getProjects() : parent.getChildren(projectViewModel.getProjectDao());
        ProjectRecyclerViewAdapter adapter =
                new ProjectRecyclerViewAdapter(new ProjectRecyclerViewAdapter.ProjectDiff(), this::setChildAsState,
                        this::editItem, getViewLifecycleOwner(), projectViewModel);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        taskOrParentList.observe(requireActivity(), adapter::convertAndSubmitList);
        fab.setOnClickListener(this::createItem);
    }

    public void createItem(View v) {
        AbstractMap.SimpleEntry<EditText, AlertDialog.Builder> values = createDialogBuilder();
        AlertDialog.Builder builder = values.getValue();
        builder.setTitle("New " + type);
        EditText input = values.getKey();

        builder.setPositiveButton("Submit", (dialog, which) -> {
            switch (type) {
                case PROJECT:
                    Project newProject = new Project();
                    newProject.setName(input.getText().toString());
                    projectViewModel.doAction(dao -> dao.insertProject(newProject));
                    break;
                case TASK_GROUP:
                    TaskGroup newTaskGroup = new TaskGroup();
                    newTaskGroup.setName(input.getText().toString());
                    if (parent instanceof Project) newTaskGroup.parentProjectName = parent.getName();
                    projectViewModel.doAction(dao -> dao.insertTaskGroup(newTaskGroup));
                    break;
                case TASK:
                    Task newTask = new Task();
                    newTask.setName(input.getText().toString());
                    if (parent instanceof TaskGroup) newTask.parentTaskGroupId = ((TaskGroup) parent).id;
                    projectViewModel.doAction(dao -> dao.insertTask(newTask));
                    break;
            }
        });

        builder.show();
    }

    private void editItem(TaskOrParent item) {
        AbstractMap.SimpleEntry<EditText, AlertDialog.Builder> values = createDialogBuilder();
        AlertDialog.Builder builder = values.getValue();
        builder.setTitle("Edit " + type);
        EditText input = values.getKey();
        input.setText(item.getName());

        builder.setPositiveButton("Submit", (dialog, which) -> {
            item.setName(input.getText().toString());
            switch (type) {
                case PROJECT: projectViewModel.doAction(dao -> dao.updateProject((Project) item)); break;
                case TASK_GROUP: projectViewModel.doAction(dao -> dao.updateTaskGroup((TaskGroup) item)); break;
                case TASK: projectViewModel.doAction(dao -> dao.updateTask((Task) item)); break;
            }
        });

        builder.show();
    }

    private AbstractMap.SimpleEntry<EditText, AlertDialog.Builder> createDialogBuilder() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
        FrameLayout container = new FrameLayout(getActivity());
        container.setPadding(50, 5, 40, 5);
        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint(type.toString().substring(0, 1).toUpperCase(Locale.ROOT) + type.toString().substring(1) + " name");
        container.addView(input);
        builder.setView(container);
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        return new AbstractMap.SimpleEntry<>(input, builder);
    }

    private void setChildAsState(TaskOrParent newParent) {
        if (type == requestedType) {
            Log.d("project", "initial");
            helper.setNavigationResult(resultReference, newParent);
            return;
        }
        if (newParent instanceof Task) return;
        navController.navigate(ProjectListFragmentDirections.changeListType()
                .setParent(newParent).setIsRequest(requestedType != null)
                .setRequestedType(requestedType == null ? TaskOrParentType.TASK : requestedType).setIsEventList(false));
    }



    private void createEventList() {
        TimeRangeItemAdapter adapter = new TimeRangeItemAdapter((view, item) -> view.setText(((Event) item).getName()), item ->
            navController.navigate(ProjectListFragmentDirections.actionCreateEvent().setStartingEvent((Event) item)));
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        projectViewModel.getProjectDao().getEventsByDay(WorkTimerFragment.findDaysSinceEpoch()).observe(requireActivity(),
                adapter::convertAndSubmitList);
        fab.setOnClickListener(view -> navController.navigate(ProjectListFragmentDirections.actionCreateEvent()));
    }
}
