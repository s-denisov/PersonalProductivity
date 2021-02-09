package com.example.personalproductivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WorkTimerFragment extends Fragment {

    private WorkTimerViewModel viewModel;
    private ProjectViewModel projectViewModel;
    private TextView timeLeftText;
    private TextView timeTargetText;
    private TextView totalTimeText;
    private TextView workTypeText;
    private Button startStopButton;
    private Spinner taskChoice;
    private CheckBox privateStudyBox;

    private Day day;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;
    private final String[] routineTaskReferences = { "morningPrepSelected", "lunchDinerSelected", "exerciseSelected" };
    private final String sleepReference = "sleeping";
    private final long[] routineTimes = { 3600_000, 1800_000, 3600_000 };
    private final boolean[] routineTasksDone = new boolean[routineTaskReferences.length];
    private long adjustedTargetWorkTime = 0;
    public static final double TARGET_WORK_PROPORTION = 0.35;

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
        privateStudyBox = view.findViewById(R.id.checkbox_private_study);
        timeTargetText = view.findViewById(R.id.text_time_target);
        totalTimeText = view.findViewById(R.id.text_total_time);

        viewModel = new ViewModelProvider(requireActivity()).get(WorkTimerViewModel.class);
        projectViewModel = new ViewModelProvider(requireActivity()).get(ProjectViewModel.class);

        if (viewModel.getRecord() != null) privateStudyBox.setChecked(viewModel.getRecord().isPrivateStudy());


        // TEMP
//       / for (int i = 0; i < 10; i++) {
//                if (day != null) {
//                    if (day.getSchoolTime() == 0) day.setTargetWorkTime(25 * 1800_000);
//                    else day.setTargetWorkTime(13 * 1800_000);
//                    projectViewModel.doAction(dao -> dao.updateDay(day));
//                }
//            });
//        }



        List<Task> adapterTasks = new ArrayList<>();
        ArrayAdapter<Task> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, adapterTasks);
        projectViewModel.getProjectDao().getTasksLastUsed().observe(getViewLifecycleOwner(), tasks -> {
            for (Task task : tasks) Log.d("project", task.getName() + " " + task.getCompletionStatus());
            adapterTasks.clear();
            adapterTasks.addAll(tasks);
            adapter.notifyDataSetChanged();
            if (adapterTasks.isEmpty()) {
                viewModel.setTaskSelected(null);
                viewModel.setRecord(null);
            } else if (!adapterTasks.contains(viewModel.getTaskSelected())) {
                taskChoice.setSelection(0);
                viewModel.setTaskSelected(tasks.get(0));
                viewModel.setRecord(new TaskTimeRecord(System.currentTimeMillis(), findDaysSinceEpoch(), tasks.get(0).id,
                        privateStudyBox.isChecked()));
            }
            startStopButton.setEnabled(!adapterTasks.isEmpty());
            if (viewModel.getTaskSelected() != null) taskChoice.setSelection(adapter.getPosition(viewModel.getTaskSelected()));
            Log.d("project", String.valueOf(viewModel.getTaskSelected()));
        });
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        taskChoice = view.findViewById(R.id.spinner_task_name);
        taskChoice.setAdapter(adapter);
        taskChoice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateSelectedTask(() -> {
                    viewModel.setTaskSelected(adapterTasks.get(position));
                    viewModel.setRecord(new TaskTimeRecord(System.currentTimeMillis(),
                            findDaysSinceEpoch(), viewModel.getTaskSelected().id, privateStudyBox.isChecked()));
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        projectViewModel.getProjectDao().getDay(findDaysSinceEpoch()).observe(getViewLifecycleOwner(), dayData -> {
            day = dayData;
            if (day == null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
                builder.setTitle("Hours in school today");

                FrameLayout dialogContainer = new FrameLayout(getActivity());
                dialogContainer.setPadding(50, 5, 40, 5);
                final EditText input = new EditText(getActivity());
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                dialogContainer.addView(input);
                builder.setView(dialogContainer);
                builder.setPositiveButton("Submit", (dialog, which) -> {
                    long schoolMillis = (long) (Double.parseDouble(input.getText().toString()) * 3600_000);
                    long targetMillis = schoolMillis == 0 ? 25 * 1800_000 : 13 * 1800_000; // "else" clause is 8.5 hours so 3.5 hours target normally
                    Day newDay = new Day(findDaysSinceEpoch(), targetMillis, schoolMillis);
                    projectViewModel.doAction(dao -> dao.insertDay(newDay));
                });
                builder.show();

            } else {
                updateDayWorkTime(day, false);
                projectViewModel.getProjectDao().getTaskRecordsByDay(findDaysSinceEpoch()).observe(getViewLifecycleOwner(), records -> {
                    viewModel.setTimeSpentTodayValue(day.subtractPrivateStudy(0));
                    for (TaskTimeRecord record : records) viewModel.increaseTimeSpentTodayValue(record.getLength());
                });
                final long targetChange = 1800_000;
                view.findViewById(R.id.button_decrease_target).setOnClickListener(v -> {
                    day.setTargetWorkTime(day.getTargetWorkTime() - targetChange);
                    updateDayWorkTime(day, true);
                });
                view.findViewById(R.id.button_increase_target).setOnClickListener(v -> {
                    day.setTargetWorkTime(day.getTargetWorkTime() + targetChange);
                    updateDayWorkTime(day, true);
                });
            }
        });

        if (viewModel.getTimer() == null) createTimer();
        viewModel.getTimer().setOnTick(this::onTick); viewModel.getTimer().setOnFinish(this::onFinish);
        viewModel.getTimerType().observe(getViewLifecycleOwner(), type -> workTypeText.setText(type));
        viewModel.getTimerOn().observe(getViewLifecycleOwner(), this::startStop);
        updateTimeDisplay(viewModel.getTimer().getTimeLeft());

        viewModel.getTimeSpentToday().observe(getViewLifecycleOwner(), millis ->
                ((TextView) view.findViewById(R.id.text_time_today)).setText(WorkOrBreakTimer.toHoursMinutes(millis)));

        sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        final String dayReference = "WorkTimerFragment.daysSinceEpoch";
        
        if (sharedPref.getLong(dayReference, -1) != findDaysSinceEpoch()) {
            for (String ref : routineTaskReferences) editor.putBoolean(ref, false);
            if (!sharedPref.getBoolean(sleepReference, false)) {
                projectViewModel.getProjectDao().getDay(findDaysSinceEpoch() - 1)
                        .observe(getViewLifecycleOwner(), previous -> {
                    if (previous != null) {
                        previous.setTargetWorkTime(previous.getTargetWorkTime() + 7200_000);
                        projectViewModel.doAction(dao -> dao.updateDay(previous));
                    }
                });
            }
            editor.putBoolean(sleepReference, false);
            editor.putLong(dayReference, findDaysSinceEpoch());
            editor.apply();
        }
        CheckBox[] routineBoxes =  { view.findViewById(R.id.checkbox_morning_prep),
                view.findViewById(R.id.checkbox_lunch_and_dinner), view.findViewById(R.id.checkbox_exercise) };
        for (int i = 0; i < routineBoxes.length; i++) {
            routineTasksDone[i] = sharedPref.getBoolean(routineTaskReferences[i], false);
            routineBoxes[i].setChecked(routineTasksDone[i]);
            int finalI = i;
            routineBoxes[i].setOnClickListener(v -> {
                boolean checked = ((CheckBox) v).isChecked();
                routineTasksDone[finalI] = checked;
                editor.putBoolean(routineTaskReferences[finalI], checked);
                editor.apply();
            });
        }

        // Schedule.schedule(LocalTime.of(9, 0).toSecondOfDay() * 1000L, 7 * 3600_000);
        return view;
    }

    private void updateDayWorkTime(Day day, boolean updateDay) {
        Log.d("project", String.valueOf((double) day.getTargetWorkTime() / 3600_000));
        adjustedTargetWorkTime = (long) (day.getTargetWorkTime() * TARGET_WORK_PROPORTION * 6 / 7);
        timeTargetText.setText(WorkOrBreakTimer.toHoursMinutes(adjustedTargetWorkTime));
        totalTimeText.setText(WorkOrBreakTimer.toHoursMinutes(day.getTargetWorkTime()));
        if (updateDay) projectViewModel.doAction(dao -> dao.updateDay(day));
    }

    public static long findDaysSinceEpoch() {
        return System.currentTimeMillis() / (24 * 3600 * 1000);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateSelectedTask(() -> {});
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("project", "onPause");
        updateSelectedTask(() -> {});
    }

    private void updateSelectedTask(Runnable afterUpdate) {
        if (viewModel.getTaskSelected() != null) {
            viewModel.getTaskSelected().lastUsed = System.currentTimeMillis();
            projectViewModel.doAction(dao -> {
                dao.updateTask(viewModel.getTaskSelected());
                if (viewModel.getRecord() != null && viewModel.getRecord().getLength() > 0) {
                    if (dao.insertTaskRecord(viewModel.getRecord()) == -1) {
                        dao.updateTaskRecord(viewModel.getRecord());
                    }
                    Log.d("project", "updateTask");
                }
                afterUpdate.run();
            });
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
            workTypeText.setText(viewModel.getTimerType().getValue());
            updateTaskChoiceEnabled();
        } else {
            startStopButton.setText(getText(R.string.btn_work_start_text));
            viewModel.getTimer().pause();
            taskChoice.setEnabled(true);
            updatePrivateStudyBoxEnabled();
            workTypeText.setText("Free time");
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!Objects.requireNonNull(viewModel.getTimerOn().getValue())) {
                        long untilEnd = Duration.between(LocalTime.now(), LocalTime.of(22, 0)).toMillis();
                        if (untilEnd < 0) {
                            startStopButton.setText("Sleep");
                            startStopButton.setEnabled(!sharedPref.getBoolean(sleepReference, false));
                            startStopButton.setOnClickListener(view -> {
                               day.setTargetWorkTime(day.getTargetWorkTime() - untilEnd);
                               projectViewModel.doAction(dao -> dao.updateDay(day));
                               editor.putBoolean(sleepReference, true);
                               editor.apply();
                               startStopButton.setEnabled(false);
                           });
                        }
                        assert viewModel.getTimeSpentToday().getValue() != null;
                        long workLeftToday = adjustedTargetWorkTime - viewModel.getTimeSpentToday().getValue();
                        long workLeftWithBreaks = workLeftToday + 300_000 *
                                ((long) Math.ceil((double) workLeftToday / 1800_000) - 1);
                        long routineMillis = 0;
                        for (int i = 0; i < routineTasksDone.length; i++) if (!routineTasksDone[i]) routineMillis += routineTimes[i];
                        timeLeftText.setText(WorkOrBreakTimer.formatMilliseconds(untilEnd - workLeftWithBreaks - routineMillis));
                        handler.postDelayed(this, 1000);
                    }
                }
            }, 0);
        }
    }
    private void updateTimeDisplay(long millisUntilFinished) {
        timeLeftText.setText(WorkOrBreakTimer.formatMilliseconds(millisUntilFinished));
    }

    private void updateTaskChoiceEnabled() {
        taskChoice.setEnabled(!viewModel.isWorkTimer());
        updatePrivateStudyBoxEnabled();
    }

    private void updatePrivateStudyBoxEnabled() {
        privateStudyBox.setEnabled(taskChoice.isEnabled());
    }

    private void onTick(long millisUntilFinished) {
        if (viewModel.isWorkTimer() && viewModel.getTaskSelected() != null) {
            long change = viewModel.getPreviousTimeRemaining() - millisUntilFinished;
            viewModel.getTaskSelected().timeSpent += change;
            viewModel.increaseTimeSpentTodayValue(change);
            viewModel.getRecord().addMilliseconds(change);
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
}