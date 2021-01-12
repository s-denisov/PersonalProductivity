package com.example.personalproductivity;

import androidx.annotation.NonNull;

public enum TaskOrParentType {
    TASK("task"), TASK_GROUP("task group"), PROJECT("project");

    private final String stringValue;

    TaskOrParentType(String stringValue) {
        this.stringValue = stringValue;
    }

    @NonNull
    @Override
    public String toString() {
        return stringValue;
    }
}
