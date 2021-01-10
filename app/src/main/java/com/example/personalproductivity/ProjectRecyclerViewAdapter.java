package com.example.personalproductivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

public class ProjectRecyclerViewAdapter extends ListAdapter<Project, ProjectRecyclerViewAdapter.ViewHolder> {

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView textView;
        public ViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.text_view);
        }

        public void bind(String text) {
            textView.setText(text);
        }

    }

    public static class ProjectDiff extends DiffUtil.ItemCallback<Project> {

        @Override
        public boolean areItemsTheSame(@NonNull Project oldItem, @NonNull Project newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Project oldItem, @NonNull Project newItem) {
            return oldItem.name.equals(newItem.name);
        }
    }

    protected ProjectRecyclerViewAdapter(@NonNull DiffUtil.ItemCallback<Project> diffCallback) {
        super(diffCallback);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.project_recycler_row_item, viewGroup, false);
        Log.d("project", "onCreateViewHolder");
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        Log.d("project", "onBindViewHolder");
        viewHolder.bind(getItem(position).name);
    }
}

