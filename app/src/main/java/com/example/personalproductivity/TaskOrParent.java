package com.example.personalproductivity;

import androidx.lifecycle.LiveData;

import java.io.Serializable;
import java.util.List;

public interface TaskOrParent extends Serializable {
    String getName();
    void setName(String name);
    CompletionStatus getCompletionStatus();
    void setCompletionStatus(CompletionStatus completionStatus);
    LiveData<? extends List<? extends TaskOrParent>> getChildren(ProjectDao source);
    int getId();
    void setParentId(int id);
    void updateInDb(ProjectViewModel viewModel);
    void deleteInDb(ProjectViewModel viewModel);
    boolean equals(Object t2);
}
