package com.example.personalproductivity.db.types;

import androidx.annotation.NonNull;

public enum TaskOrParentType {
    TASK("task"), TASK_GROUP("task group"), PROJECT("project");

    private final String stringValue;

    TaskOrParentType(String stringValue) {
        this.stringValue = stringValue;
    }

    public TaskOrParentType findParentType() {
        switch (this) {
            case TASK_GROUP: return PROJECT;
            case TASK: return TASK_GROUP;
        }
        return null;
    }

    @NonNull
    @Override
    public String toString() {
        return stringValue;
    }
}
