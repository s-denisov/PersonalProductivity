package com.example.personalproductivity.db;

import androidx.room.Embedded;
import com.example.personalproductivity.db.Day;
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

    public int getTotalSchoolLessons() {
        return (int) (totalSchoolLessons + day.getSchoolTime() / 3600_000);
    }
}
