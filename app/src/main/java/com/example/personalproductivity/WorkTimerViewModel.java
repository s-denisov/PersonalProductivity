package com.example.personalproductivity;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import lombok.Getter;
import lombok.Setter;


public class WorkTimerViewModel {// extends ViewModel {

    @Getter @Setter private WorkOrBreakTimer timer;
    private final MutableLiveData<Boolean> timerOn = new MutableLiveData<>(false);
//    @Getter @Setter private boolean isWorkTimer;
    private final MutableLiveData<String> timerType = new MutableLiveData<>();
    @Getter @Setter private Task taskSelected;
    @Getter @Setter private TaskTimeRecord record;
    @Getter @Setter private long previousTimeRemaining;
    private final MutableLiveData<Long> timeSpentToday = new MutableLiveData<>(0L);


    public LiveData<String> getTimerType() {
        return timerType;
    }

    public void setTimerTypeValue(String value) {
        timerType.setValue(value);
    }

    public LiveData<Boolean> getTimerOn() {
        return timerOn;
    }

    public void setTimerOnValue(boolean value) {
        timerOn.setValue(value);
    }

    public void flipTimerOnValue() {
        //noinspection ConstantConditions
        timerOn.setValue(!timerOn.getValue());
    }


    public LiveData<Long> getTimeSpentToday() {
        return timeSpentToday;
    }

    public void setTimeSpentTodayValue(long millis) {
        timeSpentToday.setValue(millis);
    }

    public void increaseTimeSpentTodayValue(long increase) {
        //noinspection ConstantConditions
        timeSpentToday.setValue(timeSpentToday.getValue() + increase);
    }
}
