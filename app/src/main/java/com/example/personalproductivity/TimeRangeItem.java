package com.example.personalproductivity;

public interface TimeRangeItem {
    long getStartTimeStamp();
    long getLength();
    boolean equals(Object o);
}
