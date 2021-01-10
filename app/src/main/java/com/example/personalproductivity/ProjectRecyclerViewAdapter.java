package com.example.personalproductivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ProjectRecyclerViewAdapter extends RecyclerView.Adapter<ProjectRecyclerViewAdapter.ViewHolder> {

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

    private List<Project> projects;

    public ProjectRecyclerViewAdapter(List<Project> projects) {
        Log.d("project", "adapter init");
        this.projects = projects;
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
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int position) {
        Log.d("project", "onBindViewHolder");
        if (projects != null) viewHolder.bind(projects.get(position).name);
    }

    @Override
    public int getItemCount() {
        if (projects == null) {
            Log.d("project", "getItemCount: 0");
            return 0;
        }
        Log.d("project", "getItemCount: " + projects.size());
        return projects.size();
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
        Log.d("project", "setProjects: " + projects);
        notifyDataSetChanged();
    }
}

