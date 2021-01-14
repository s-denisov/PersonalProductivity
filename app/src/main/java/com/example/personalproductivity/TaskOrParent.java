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
    boolean equals(Object t2);
}
