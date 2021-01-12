package com.example.personalproductivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ProjectRecyclerViewAdapter extends ListAdapter<TaskOrParent, ProjectRecyclerViewAdapter.ViewHolder> {

    class ViewHolder extends RecyclerView.ViewHolder {

        private final View view;
        public ViewHolder(View view) {
            super(view);
            this.view = view.findViewById(R.id.text_view);
        }

        public void bind(TaskOrParent item) {
            view.setOnClickListener(view -> setListState.accept(item));
            TextView textView = view.findViewById(R.id.text_view);
            textView.setText(item.getName());
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

    public ProjectRecyclerViewAdapter(@NonNull DiffUtil.ItemCallback<TaskOrParent> diffCallback,
                                      Consumer<TaskOrParent> setListState) {
        super(diffCallback);
        this.setListState = setListState;
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

