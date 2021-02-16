package com.example.personalproductivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TimeRangeItemAdapter extends ListAdapter<TimeRangeItem, TimeRangeItemAdapter.ViewHolder> {

    class ViewHolder extends RecyclerView.ViewHolder {

        private final View view;
        private final TextView recordTask;
        private final TextView recordLength;
        private final TextView recordStartTime;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            recordTask = view.findViewById(R.id.text_record_task);
            recordLength = view.findViewById(R.id.text_record_length);
            recordStartTime = view.findViewById(R.id.text_record_start_time);
        }

        public void bind(TimeRangeItem record) {
            recordLength.setText(WorkOrBreakTimer.formatMilliseconds(record.getLength()));
            SimpleDateFormat format = new SimpleDateFormat("HH:mm");
            format.setTimeZone(TimeZone.getDefault());
            recordStartTime.setText(format.format(new Date(record.getStartTimeStamp())));
//            dao.getTask(record.getTaskId()).observe(owner, task -> recordTask.setText(task.getName()));
            nameSetter.accept(recordTask, record);
            view.setOnLongClickListener(v -> {
                onLongClick.accept(record);
                return true;
            });
        }
    }

    public static class RecordDiff extends DiffUtil.ItemCallback<TimeRangeItem> {

        @Override
        public boolean areItemsTheSame(@NonNull TimeRangeItem oldItem, @NonNull TimeRangeItem newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull TimeRangeItem oldItem, @NonNull TimeRangeItem newItem) {
            return oldItem.equals(newItem);
        }
    }

    private final BiConsumer<TextView, TimeRangeItem> nameSetter;
    private final Consumer<TimeRangeItem> onLongClick;

    public TimeRangeItemAdapter(BiConsumer<TextView, TimeRangeItem> nameSetter, Consumer<TimeRangeItem> onLongClick) {
        super(new RecordDiff());
        this.nameSetter = nameSetter;
        this.onLongClick = onLongClick;
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

    public void convertAndSubmitList(List<? extends TimeRangeItem> items) {
        submitList(items.stream().map(item -> (TimeRangeItem) item).collect(Collectors.toList()));
    }
}
