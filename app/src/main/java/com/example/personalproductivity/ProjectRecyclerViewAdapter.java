package com.example.personalproductivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ProjectRecyclerViewAdapter extends ListAdapter<TaskOrParent, ProjectRecyclerViewAdapter.ViewHolder> {

    class ViewHolder extends RecyclerView.ViewHolder {

        private final View view;
        private final TextView nameText;
        private final TextView timeSpentText;
        private final RadioGroup completionStatusGroup;
        private long timeSpent = 0;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            nameText = view.findViewById(R.id.text_name);
            timeSpentText = view.findViewById(R.id.text_time_spent);
            completionStatusGroup = view.findViewById(R.id.radio_group_completion_status);
        }

        public void bind(TaskOrParent item) {
            nameText.setText(item.getName());
            view.setOnClickListener(v -> setListState.accept(item));
            List<TaskOrParent> l = new ArrayList<>();
            l.add(item);
            findTimeSpent(l);
            Log.d("project", item.getName() + " " + item.getCompletionStatus());
            switch (item.getCompletionStatus()) {
                case IN_PROGRESS: ((RadioButton) view.findViewById(R.id.radio_in_progress)).setChecked(true); break;
                case COMPLETE: ((RadioButton) view.findViewById(R.id.radio_tick)).setChecked(true); break;
                case FAILED: ((RadioButton) view.findViewById(R.id.radio_cross)).setChecked(true);
            }
            completionStatusGroup.setOnCheckedChangeListener((group, checkedId) -> {
                switch (checkedId) {
                    case R.id.radio_in_progress: item.setCompletionStatus(CompletionStatus.IN_PROGRESS); break;
                    case R.id.radio_tick: item.setCompletionStatus(CompletionStatus.COMPLETE); break;
                    case R.id.radio_cross: item.setCompletionStatus(CompletionStatus.FAILED);
                }
                if (item instanceof Project) viewModel.doAction(dao -> dao.updateProject((Project) item));
                if (item instanceof TaskGroup) viewModel.doAction(dao -> dao.updateTaskGroup((TaskGroup) item));
                if (item instanceof Task) viewModel.doAction(dao -> dao.updateTask((Task) item));
            });
        }

        private void findTimeSpent(List<? extends TaskOrParent> taskOrParentList) {
            for (TaskOrParent t : taskOrParentList) {
                if (t instanceof Task) {
                    timeSpent += ((Task) t).timeSpent;
                    timeSpentText.setText(formatTime(timeSpent));
                } else {
                    t.getChildren(viewModel.getProjectDao()).observe(owner, this::findTimeSpent);
                }
            }
        }

        private String formatTime(long milliseconds) {
            long seconds = milliseconds / 1000;
            return seconds / 3600 + " hours, " + seconds % 3600 / 60 + " minutes";
        }
    }

    public static class ProjectDiff extends DiffUtil.ItemCallback<TaskOrParent> {

        @Override
        public boolean areItemsTheSame(@NonNull TaskOrParent oldItem, @NonNull TaskOrParent newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull TaskOrParent oldItem, @NonNull TaskOrParent newItem) {
            return oldItem.equals(newItem);
        }
    }

    private final Consumer<TaskOrParent> setListState;
    private final LifecycleOwner owner;
    private final ProjectViewModel viewModel;

    public ProjectRecyclerViewAdapter(@NonNull DiffUtil.ItemCallback<TaskOrParent> diffCallback,
                                      Consumer<TaskOrParent> setListState, LifecycleOwner owner,
                                      ProjectViewModel viewModel) {
        super(diffCallback);
        this.setListState = setListState;
        this.owner = owner;
        this.viewModel = viewModel;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.project_recycler_row_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        viewHolder.bind(getItem(position));
    }

    public void convertAndSubmitList(@Nullable List<? extends TaskOrParent> list) {
        if (list == null) {
            submitList(null);
        } else {
            submitList(list.stream().map(x -> (TaskOrParent) x).collect(Collectors.toList()));
        }
    }
}

