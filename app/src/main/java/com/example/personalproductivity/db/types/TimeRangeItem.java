package com.example.personalproductivity.db.types;

public interface TimeRangeItem {
    long getStartTimeStamp();
    long getLength();
    boolean equals(Object o);
}
