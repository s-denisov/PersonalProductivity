package com.example.personalproductivity.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.*;
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
import com.example.personalproductivity.*;
import com.example.personalproductivity.db.*;
import com.example.personalproductivity.db.types.CompletionStatus;
import com.example.personalproductivity.db.types.TaskOrParentType;
import com.example.personalproductivity.viewmodels.ProjectViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ProjectListFragment extends Fragment {

    private ProjectViewModel projectViewModel;
    private TaskOrParentType type;
    private TaskOrParent parent;
    private TaskOrParentType requestedType;
    private boolean isEventList;
    private boolean requesting = false;
    private boolean collapsed = false;
    private NavController navController;
    private FragmentResultHelper helper;
    public static final String resultReference = "com.example.personalproductivity.ui.ProjectListFragment.result";
    private Consumer<TaskOrParent> onResult;
    private ProjectRecyclerViewAdapter adapter;
    private List<TaskOrParent> itemsToShow = new ArrayList<>();
    private LiveData<? extends List<? extends TaskOrParent>> taskOrParentList;

    private final CompletionStatus[] completionStatuses = { CompletionStatus.TODO_LATER, CompletionStatus.IN_PROGRESS, CompletionStatus.COMPLETE, CompletionStatus.FAILED };
    private final boolean[] showWithCompletionStatus = { true, true, true, true };
    private final int[] completionStatusIds = { R.id.todo_later, R.id.in_progress, R.id.complete, R.id.failed };
    private final String[] statusSharedPrefRef = { "ProjectListFragment status TODO_LATER", "ProjectListFragment status IN_PROGRESS", "ProjectListFragment status COMPLETE", "ProjectListFragment status FAILED" };
    private SharedPreferences sharedPref;

    private FloatingActionButton fab;
    private RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("project", toString());
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
            createItemList();
        } else {
            createEventList();
        }

        setHasOptionsMenu(!isEventList);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        navController = Navigation.findNavController(view);
        helper = new FragmentResultHelper(navController);
        AtomicBoolean notPopped = new AtomicBoolean(true);
        helper.getNavigationResultLiveData(resultReference).observe(getViewLifecycleOwner(), result -> {
            if (onResult == null) {
                if (result != null && requestedType != null) {
                    helper.setNavigationResult(resultReference, result);
                    if (notPopped.get()) {
                        navController.popBackStack();
                        notPopped.set(false);
                    }
                }
            } else {
                onResult.accept((TaskOrParent) result);
                onResult = null;
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

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.task_list_options, menu);
        sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE);
//        for (int i = 0 )
        for (int i = 0; i < statusSharedPrefRef.length; i++) {
            showWithCompletionStatus[i] = sharedPref.getBoolean(statusSharedPrefRef[i], true);
            menu.findItem(completionStatusIds[i]).setChecked(showWithCompletionStatus[i]);
        }
        updateItemList();
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        item.setChecked(!item.isChecked());
        if (item.getItemId() == R.id.collapse) {
            collapsed = !collapsed;
            createTaskOrParentList();
        } else {
            SharedPreferences.Editor editor = sharedPref.edit();
            for (int i = 0; i < completionStatusIds.length; i++) {
                if (item.getItemId() == completionStatusIds[i]) {
                    showWithCompletionStatus[i] = !showWithCompletionStatus[i];
                    editor.putBoolean(statusSharedPrefRef[i], showWithCompletionStatus[i]);
                    break;
                }
            }
            editor.apply();
            updateItemList();
        }
        //setMenuVisibility(true);
        return true;
    }

    private void createTaskOrParentList() {
        if (taskOrParentList != null) taskOrParentList.removeObservers(getViewLifecycleOwner());
        taskOrParentList = collapsed
            ? projectViewModel.getProjectDao().getTaskViews(WorkTimerFragment.findDaysSinceEpoch())//.getTasksByAdjustedPriority(WorkTimerFragment.findDaysSinceEpoch())
            : parent == null
            ? projectViewModel.getProjects()
            : parent.getChildren(projectViewModel.getProjectDao());
        taskOrParentList.observe(getViewLifecycleOwner(), items -> {
            if (collapsed) {
                List<TaskView> converted = items.stream().map(item -> (TaskView) item)
                        .sorted(Comparator.comparingInt(view -> ((TaskView) view).getTask().getPriority().getNumber())
                                .thenComparingDouble(view -> ((TaskView) view).findAdjustedPriority()).reversed()).collect(Collectors.toList());
                itemsToShow.clear();
                itemsToShow.addAll(converted);
                for (TaskOrParent item : items) {
                    if (item instanceof TaskView) Log.d("project", item.toString());
                }
            } else {
                itemsToShow.clear();
                itemsToShow.addAll(items);
            }
            updateItemList();
        });
    }

    private void createItemList() {
        adapter = new ProjectRecyclerViewAdapter(new ProjectRecyclerViewAdapter.ProjectDiff(), this::onItemClick,
                        this::onItemLongClick, requireActivity(), projectViewModel);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        createTaskOrParentList();
        fab.setOnClickListener(this::createItem);
    }

    private void updateItemList() {
        List<TaskOrParent> filteredItems = new ArrayList<>();
        for (TaskOrParent item : itemsToShow) {
            for (int i = 0; i < completionStatuses.length; i++) {
                if (showWithCompletionStatus[i] && completionStatuses[i] == item.getCompletionStatus()) {
                    filteredItems.add(item);
                }
            }
        }
        adapter.convertAndSubmitList(filteredItems);
    }

    public void createItem(View v) {
        if (type == TaskOrParentType.TASK && parent != null) {
            navController.navigate(ProjectListFragmentDirections.actionCreateTask(parent.getId()));
            return;
        }
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
                    if (parent instanceof Project) newTaskGroup.parentProjectId = ((Project) parent).id;
                    projectViewModel.doAction(dao -> dao.insertTaskGroup(newTaskGroup));
                    break;
            }
        });

        builder.show();
    }

    private void onItemLongClick(TaskOrParent item) {
        if (collapsed) {
            renameItem(item);
            return;
        }
        AlertDialog.Builder b = new AlertDialog.Builder(requireActivity());
        b.setTitle("Edit " + type);
        String[] editingOptions = type == TaskOrParentType.PROJECT
                ? new String[]{"Rename", "Merge Into"} : new String[]{"Rename", "Merge Into", "Move"};
        b.setItems(editingOptions, (dialog, which) -> {
            dialog.dismiss();
            switch(which) {
                case 0: renameItem(item); break;
                case 1: mergeItem(item); break;
                case 2: moveItem(item); break;
            }
        });
        b.show();
    }

    private void renameItem(TaskOrParent item) {
        if (type == TaskOrParentType.TASK || collapsed) {
            Task item2 = ((TaskView) item).getTask();
            navController.navigate(ProjectListFragmentDirections.actionCreateTask(item2.parentTaskGroupId).setStartingTask(item2));
            return;
        }
        AbstractMap.SimpleEntry<EditText, AlertDialog.Builder> values = createDialogBuilder();
        AlertDialog.Builder builder = values.getValue();
        builder.setTitle("Edit " + type);
        EditText input = values.getKey();
        input.setText(item.getName());

        builder.setPositiveButton("Submit", (dialog, which) -> {
            item.setName(input.getText().toString());
            item.updateInDb(projectViewModel);
        });
        builder.show();
    }

    private void mergeItem(TaskOrParent absorbedItem) {
        navController.navigate(ProjectListFragmentDirections.requestTaskOrParent().setIsRequest(true).setRequestedType(type));
        onResult = keptItem -> {
            if (absorbedItem instanceof Task) {
                projectViewModel.getProjectDao().getTaskRecordsByTaskId(absorbedItem.getId()).observe(getViewLifecycleOwner(), records -> {
                    for (TaskTimeRecord record : records) {
                        record.setTaskId(keptItem.getId());
                        projectViewModel.doAction(dao -> dao.updateTaskRecord(record));
                    }
                    keptItem.updateInDb(projectViewModel);
                    absorbedItem.deleteInDb(projectViewModel);
                });
            } else {
                absorbedItem.getChildren(projectViewModel.getProjectDao()).observe(getViewLifecycleOwner(), children -> {
                    for (TaskOrParent child : children) {
                        child.setParentId(keptItem.getId());
                        child.updateInDb(projectViewModel);
                    }
                    absorbedItem.deleteInDb(projectViewModel);
                });
            }
        };
    }

    private void moveItem(TaskOrParent item) {
        if (type == TaskOrParentType.PROJECT) return;
        onResult = newParent -> {
            Log.d("project", "Observing: " + newParent);
            item.setParentId(newParent.getId());
            item.updateInDb(projectViewModel);
        };
        assert type.findParentType() != null;
        navController.navigate(ProjectListFragmentDirections.requestTaskOrParent()
                .setIsRequest(true).setRequestedType(type.findParentType()));
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

    private void onItemClick(TaskOrParent newParent) {
        if (type == requestedType) {
            helper.setNavigationResult(resultReference, newParent);
            navController.popBackStack();
            return;
        }
        if (newParent instanceof Task || newParent instanceof TaskView) return;
        navController.navigate(ProjectListFragmentDirections.changeListType()
                .setParent(newParent).setIsRequest(requestedType != null)
                .setRequestedType(requestedType == null ? TaskOrParentType.TASK : requestedType).setIsEventList(false));
    }



    private void createEventList() {
        TimeRangeItemAdapter eventAdapter = new TimeRangeItemAdapter((view, item) -> view.setText(((Event) item).getName()), item ->
            navController.navigate(ProjectListFragmentDirections.actionCreateEvent().setStartingEvent((Event) item)));
        recyclerView.setAdapter(eventAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        projectViewModel.getProjectDao().getEventsByDay(WorkTimerFragment.findDaysSinceEpoch()).observe(getViewLifecycleOwner(),
                eventAdapter::convertAndSubmitList);
        fab.setOnClickListener(view -> navController.navigate(ProjectListFragmentDirections.actionCreateEvent()));
    }
}
