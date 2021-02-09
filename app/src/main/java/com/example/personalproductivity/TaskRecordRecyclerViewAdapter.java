package com.example.personalproductivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class TaskRecordRecyclerViewAdapter extends ListAdapter<TaskTimeRecord, TaskRecordRecyclerViewAdapter.ViewHolder> {

    class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView recordTask;
        private final TextView recordLength;
        private final TextView recordStartTime;

        public ViewHolder(View view) {
            super(view);
            recordTask = view.findViewById(R.id.text_record_task);
            recordLength = view.findViewById(R.id.text_record_length);
            recordStartTime = view.findViewById(R.id.text_record_start_time);
        }

        public void bind(TaskTimeRecord record) {
            recordLength.setText(WorkOrBreakTimer.formatMilliseconds(record.getLength()));
            SimpleDateFormat format = new SimpleDateFormat("HH:mm");
            format.setTimeZone(TimeZone.getDefault());
            recordStartTime.setText(format.format(new Date(record.getStartTimeStamp())));
            dao.getTask(record.getTaskId()).observe(owner, task -> recordTask.setText(task.getName()));
        }
    }

    public static class RecordDiff extends DiffUtil.ItemCallback<TaskTimeRecord> {

        @Override
        public boolean areItemsTheSame(@NonNull TaskTimeRecord oldItem, @NonNull TaskTimeRecord newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull TaskTimeRecord oldItem, @NonNull TaskTimeRecord newItem) {
            return oldItem.equals(newItem);
        }
    }

    private final LifecycleOwner owner;
    private final ProjectDao dao;

    public TaskRecordRecyclerViewAdapter(LifecycleOwner owner, ProjectDao dao) {
        super(new RecordDiff());
        this.owner = owner;
        this.dao = dao;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.record_recycler_row_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        viewHolder.bind(getItem(position));
    }
}
