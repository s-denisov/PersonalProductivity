package com.example.personalproductivity;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.room.Embedded;
import lombok.Data;

import java.util.List;

@Data
public class TaskView implements TaskOrParent {
    @Embedded private Task task;
    private long daysSinceEpoch;
    private long totalLength;
    private long lengthToday;

    public double findAdjustedPriority() {
        if (findDaysUntilDeadline() <= 0 || task.expectedTime <= totalLength) return -1;
        return (double) findDaysUntilDeadline() / (task.expectedTime - totalLength);
    }

    public double findTargetToday() {
        if (findDaysUntilDeadline() <= 0 || task.expectedTime <= totalLength) return 0;
        double defaultTime = (double) (task.expectedTime - totalLength + lengthToday) / findDaysUntilDeadline();
        return Math.max(defaultTime, Math.min(15 * 60_000, task.expectedTime - totalLength));
    }

    public long findTimeToDoToday() {
        return (long) (findTargetToday() - lengthToday);
    }

    public long findDaysUntilDeadline() {
        return task.deadlineDate - daysSinceEpoch;
    }

    @Override
    public String getName() {
        return task.getName();
    }

    @Override
    public void setName(String name) {
        task.setName(name);
    }

    @Override
    public CompletionStatus getCompletionStatus() {
        return task.getCompletionStatus();
    }

    @Override
    public void setCompletionStatus(CompletionStatus completionStatus) {
        task.setCompletionStatus(completionStatus);
    }

    @Override
    public LiveData<? extends List<? extends TaskOrParent>> getChildren(ProjectDao source) {
        return task.getChildren(source);
    }

    @Override
    public int getId() {
        return task.getId();
    }

    @Override
    public void setParentId(int id) {
        task.setParentId(id);
    }

    @Override
    public void updateInDb(ProjectViewModel viewModel) {
        task.updateInDb(viewModel);
    }

    @Override
    public void deleteInDb(ProjectViewModel viewModel) {
        task.deleteInDb(viewModel);
    }

    @NonNull
    @Override
    public String toString() {
        return task.getName() + " (" + WorkOrBreakTimer.toHoursMinutes(findTimeToDoToday()) + ")";
    }

}
