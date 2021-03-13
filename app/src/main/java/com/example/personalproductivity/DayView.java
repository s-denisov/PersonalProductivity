package com.example.personalproductivity;

import androidx.room.Embedded;
import lombok.Data;

@Data
public class DayView {
    @Embedded private Day day;
    private int totalSchoolLessons;
    private long totalChoreLength;
    private long totalFunLength;

    public long subtractPrivateStudy(long millisWorked) {
        long schoolTime = day.getSchoolTime() != 0 ? day.getSchoolTime() : totalSchoolLessons * 3600_000L;
        long workedModifier = schoolTime == 0 ? 0 : schoolTime - 5 * 3600_000;
        return millisWorked + workedModifier;
    }

    public long findTargetWorkTime() {
        return day.getTargetWorkTime() + day.getMissedSleep() - totalChoreLength;
    }
}