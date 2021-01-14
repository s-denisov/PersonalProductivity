package com.example.personalproductivity;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;

public class WorkTimerFragment extends Fragment {

    private WorkTimerViewModel viewModel;
    private ProjectViewModel projectViewModel;
    private TextView timeLeftText;
    private TextView workTypeText;
    private Button startStopButton;
    private Spinner taskChoice;

    private void createNotification(String text) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), MainActivity.WORK_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(text);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(requireContext());
        notificationManager.notify(1, builder.build());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_work_timer, container, false);
        timeLeftText = view.findViewById(R.id.text_timer);
        workTypeText = view.findViewById(R.id.text_work_type);
        startStopButton = view.findViewById(R.id.button_start_stop);
        startStopButton.setOnClickListener(v -> viewModel.flipTimerOnValue());
        viewModel = new ViewModelProvider(requireActivity()).get(WorkTimerViewModel.class);

        projectViewModel = new ViewModelProvider(requireActivity()).get(ProjectViewModel.class);
        List<Task> adapterTasks = new ArrayList<>();
        ArrayAdapter<Task> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, adapterTasks);
        projectViewModel.getProjectDao().getTasksLastUsed().observe(getViewLifecycleOwner(), tasks -> {
            adapterTasks.clear();
            adapterTasks.addAll(tasks);
            adapter.notifyDataSetChanged();
            if (viewModel.getTaskSelected() != null) taskChoice.setSelection(adapter.getPosition(viewModel.getTaskSelected()));
        });
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        taskChoice = view.findViewById(R.id.spinner_task_name);
        taskChoice.setAdapter(adapter);
        taskChoice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateSelectedTask();
                viewModel.setTaskSelected(adapterTasks.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        if (viewModel.getTimer() == null) createTimer();
        viewModel.getTimer().setOnTick(this::onTick); viewModel.getTimer().setOnFinish(this::onFinish);
        viewModel.getTimerType().observe(getViewLifecycleOwner(), type -> workTypeText.setText(type));
        viewModel.getTimerOn().observe(getViewLifecycleOwner(), this::startStop);
        updateTimeDisplay(viewModel.getTimer().getTimeLeft());

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        updateSelectedTask();
    }

    private void updateSelectedTask() {
        if (viewModel.getTaskSelected() != null ) {
            viewModel.getTaskSelected().lastUsed = System.currentTimeMillis();
            projectViewModel.doAction(dao -> dao.updateTask(viewModel.getTaskSelected()));
        }
    }

    private void createTimer() {
        viewModel.setWorkTimer(!viewModel.isWorkTimer());
//        long sessionLength = viewModel.isWorkTimer() ? 10 * 1000 : 5 * 1000;
        long sessionLength = viewModel.isWorkTimer() ? 30 * 60 * 1000 : 5 * 60 * 1000;
        viewModel.setTimerTypeValue(viewModel.isWorkTimer() ? "Work" : "Break");
        WorkOrBreakTimer timer = new WorkOrBreakTimer(sessionLength);
        timer.setOnTick(this::onTick); timer.setOnFinish(this::onFinish);
        viewModel.setTimer(timer);
        viewModel.setPreviousTimeRemaining(sessionLength);
    }

    private void startStop(boolean timerOn) {
        if (timerOn) {
            startStopButton.setText(getText(R.string.btn_work_stop_text));
            viewModel.getTimer().start();
            updateTaskChoiceEnabled();
        } else {
            startStopButton.setText(getText(R.string.btn_work_start_text));
            viewModel.getTimer().pause();
            taskChoice.setEnabled(true);
        }
    }

    private void updateTimeDisplay(long millisUntilFinished) {
        timeLeftText.setText(WorkOrBreakTimer.formatMilliseconds(millisUntilFinished));
    }

    private void updateTaskChoiceEnabled() {
        taskChoice.setEnabled(!viewModel.isWorkTimer());
    }

    private void onTick(long millisUntilFinished) {
        if (viewModel.isWorkTimer()) {
            viewModel.getTaskSelected().timeSpent += viewModel.getPreviousTimeRemaining() - millisUntilFinished;
            viewModel.setPreviousTimeRemaining(millisUntilFinished);
        }
        updateTimeDisplay(millisUntilFinished);
        updateTaskChoiceEnabled();
    }

    private void onFinish() {
        createNotification(viewModel.getTimerType().getValue() + " finished");
        createTimer();
        viewModel.getTimer().start();
    }

//    public void addToDayTotal(long millis) {
        //((TextView) view.findViewById(R.id.text_worked_today)).setText(WorkOrBreakTimer.toHoursMinutes(workedToday + millis));
//    }
}