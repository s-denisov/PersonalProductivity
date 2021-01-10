package com.example.personalproductivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;

public class WorkTimerFragment extends Fragment {

    boolean workTimerOn = false;
    private WorkOrBreakTimer workTimer;
    private WorkOrBreakTimer breakTimer;
    private long workedToday = 0;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_work_timer, container, false);

        createTimers(3);
        view.findViewById(R.id.button_2_hours).setOnClickListener(view -> createTimers(2));
        view.findViewById(R.id.button_3_hours).setOnClickListener(view -> createTimers(3));
        view.findViewById(R.id.button_4_hours).setOnClickListener(view -> createTimers(4));
        view.findViewById(R.id.button_work).setOnClickListener(this::startStop);
        view.findViewById(R.id.button_break).setOnClickListener(this::startStop);
        return view;
    }

    private void createTimers(long workHours) {
        if (workTimer != null) {
            workTimer.pause();
            long timeSpent = workTimer.findTimeSpent();
            addToDayTotal(timeSpent);
            workedToday += timeSpent;
        }
        if (breakTimer != null) breakTimer.pause();
        workTimer = new WorkOrBreakTimer(view.findViewById(R.id.button_work),
                view.findViewById(R.id.progress_work), this, workHours * 3600 * 1000, true);
        breakTimer = new WorkOrBreakTimer(view.findViewById(R.id.button_break),
                view.findViewById(R.id.progress_break), this,  workHours * 600 * 1000, false);
        breakTimer.enable();
        workTimerOn = false;
    }

    public void startStop(View view) {
        if (workTimerOn) {
            workTimer.pause();
            breakTimer.start();
        } else {
            workTimer.start();
            breakTimer.pause();
        }
        workTimerOn = !workTimerOn;
    }

    public void breakTimerFinished() {
        startStop(null);
        workTimer.disable();
        breakTimer.disable();
    }

    public void addToDayTotal(long millis) {
        ((TextView) view.findViewById(R.id.text_worked_today)).setText(WorkOrBreakTimer.toHoursMinutes(workedToday + millis));
    }
}