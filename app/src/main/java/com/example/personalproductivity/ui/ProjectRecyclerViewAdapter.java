package com.example.personalproductivity.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.personalproductivity.db.types.CompletionStatus;
import com.example.personalproductivity.viewmodels.ProjectViewModel;
import com.example.personalproductivity.R;
import com.example.personalproductivity.WorkOrBreakTimer;
import com.example.personalproductivity.db.Task;
import com.example.personalproductivity.db.TaskOrParent;
import com.example.personalproductivity.db.TaskView;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ProjectRecyclerViewAdapter extends ListAdapter<TaskOrParent, ProjectRecyclerViewAdapter.ViewHolder> {

    class ViewHolder extends RecyclerView.ViewHolder {

        private final View view;
        private final TextView nameText;
        private final TextView timeSpentText;
        private final TextView priorityText;
        private final TextView todayText;
        private long timeSpent = 0;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            nameText = view.findViewById(R.id.text_name);
            timeSpentText = view.findViewById(R.id.text_time_spent);
            priorityText = view.findViewById(R.id.text_priority);
            todayText = view.findViewById(R.id.text_time_today);
        }

        public void bind(TaskOrParent item) {
            nameText.setText(item.getName());
            if (item instanceof Task) priorityText.setText(((Task) item).getPriority().toString());
            if (item instanceof TaskView) {
                TaskView taskView = (TaskView) item;
                priorityText.setText(taskView.getTask().getPriority().toString());
                todayText.setText(WorkOrBreakTimer.toHoursMinutes(taskView.findTimeToDoToday()));
            }
            view.setOnClickListener(v -> onClick.accept(item));
            view.setOnLongClickListener(v -> {
                onLongClick.accept(item);
                return true;
            });

            List<TaskOrParent> l = new ArrayList<>();
            l.add(item);
            timeSpent = 0;
            findTimeSpent(l, true);
            RadioButton[] radios = { view.findViewById(R.id.radio_todo_later), view.findViewById(R.id.radio_in_progress),
                                    view.findViewById(R.id.radio_tick), view.findViewById(R.id.radio_cross) };
            CompletionStatus[] statuses = { CompletionStatus.TODO_LATER, CompletionStatus.IN_PROGRESS,
                                    CompletionStatus.COMPLETE, CompletionStatus.FAILED };

            for (int i = 0; i < statuses.length; i++) {
                if (item.getCompletionStatus() == statuses[i]) radios[i].setChecked(true);
                int finalI = i;
                radios[i].setOnClickListener(v -> {
                    item.setCompletionStatus(statuses[finalI]);
                    item.updateInDb(viewModel);
                });
            }
        }

        private void findTimeSpent(List<? extends TaskOrParent> taskOrParentList, boolean isItem) {
            for (TaskOrParent t : taskOrParentList) {
                if (t instanceof Task) {
                    LiveData<Long> spentLiveData = viewModel.getProjectDao().findTimeSpent(t.getId());
                    spentLiveData.observe(owner, spent -> {
                        if (spent != null) {
                            timeSpent += spent;
                        }
                        timeSpentText.setText(WorkOrBreakTimer.toHoursMinutes(timeSpent));
                        spentLiveData.removeObservers(owner);
                    });
                } else if (t instanceof TaskView) {
                    timeSpent += ((TaskView) t).getTotalLength();
                    String resultText = WorkOrBreakTimer.toHoursMinutes(timeSpent);
                    if (isItem) resultText += "/" + WorkOrBreakTimer.toHoursMinutes(((TaskView) t).getTask().expectedTime);
                    timeSpentText.setText(resultText);
                } else {
                    t.getChildren(viewModel.getProjectDao()).observe(owner, newList -> findTimeSpent(newList, false));
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

    private final Consumer<TaskOrParent> onClick;
    private final Consumer<TaskOrParent> onLongClick;
    private final LifecycleOwner owner;
    private final ProjectViewModel viewModel;

    public ProjectRecyclerViewAdapter(@NonNull DiffUtil.ItemCallback<TaskOrParent> diffCallback,
                                      Consumer<TaskOrParent> onClick, Consumer<TaskOrParent> onLongClick, LifecycleOwner owner,
                                      ProjectViewModel viewModel) {
        super(diffCallback);
        this.onClick = onClick;
        this.onLongClick = onLongClick;
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

