package com.example.personalproductivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
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

    private DayView dayView;
    private List<Schedule.ScheduledItem> schedule;
    private long sessionLength;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;
    private SharedPreferences settingsPref;
    private final String[] routineTaskReferences = { "morningPrepSelected", "lunchDinerSelected", "exerciseSelected" };
    private final String sleepReference = "sleeping";
    private final String taskStartRef = "WorkTimerFragment taskStart";
    private boolean taskStartNotAdded = true;
    private final long[] routineTimes = { 3600_000, 1800_000, 3600_000 };
    private final boolean[] routineTasksDone = new boolean[routineTaskReferences.length];
    private long adjustedTargetWorkTime = 0;
    public static double targetWorkProportion = 0.65;


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
        view.findViewById(R.id.button_schedule).setOnClickListener(this::showSchedule);

        viewModel = new WorkTimerViewModel();//new ViewModelProvider(requireActivity()).get(WorkTimerViewModel.class);
        projectViewModel = new ViewModelProvider(requireActivity()).get(ProjectViewModel.class);

        if (viewModel.getRecord() != null) privateStudyBox.setChecked(viewModel.getRecord().isPrivateStudy());

        // TEMP
       /*projectViewModel.getProjectDao().getTaskRecordsByDay(findDaysSinceEpoch()).observe(getViewLifecycleOwner(), records -> {
           for (TaskTimeRecord record : records) {
               SimpleDateFormat format = new SimpleDateFormat("HH:mm");
               format.setTimeZone(TimeZone.getDefault());
               Log.d("project", "log " + record + " " + format.format(record.getStartTimeStamp()));
               if (record.getStartTimeStamp() == 1614167654365L) {
                   record.setLength(23 * 60_000);
                   projectViewModel.doAction(dao -> dao.updateTaskRecord(record));
               }
           }
       });*/

        manageTaskChoice(view);

        projectViewModel.getProjectDao().getDayView(findDaysSinceEpoch()).observe(getViewLifecycleOwner(), dayData -> {
            dayView = dayData;
            manageDay();
        });

        if (viewModel.getTimer() == null) createTimer();
        viewModel.getTimer().setOnTick(this::onTick); viewModel.getTimer().setOnFinish(this::onFinish);
        viewModel.getTimerType().observe(getViewLifecycleOwner(), type -> workTypeText.setText(type));
        viewModel.getTimerOn().observe(getViewLifecycleOwner(), this::startStop);
        updateTimeDisplay(viewModel.getTimer().getTimeLeft());

        viewModel.getTimeSpentToday().observe(getViewLifecycleOwner(), millis ->
                ((TextView) view.findViewById(R.id.text_time_today)).setText(WorkOrBreakTimer.toHoursMinutes(millis)));

        manageSharedPref(view);

        return view;
    }

    private void manageTaskChoice(View view) {
        List<TaskView> adapterTaskViews = new ArrayList<>();
        ArrayAdapter<TaskView> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, adapterTaskViews);
        view.findViewById(R.id.button_set_complete).setOnClickListener(v -> {
            Task task = ((TaskView) taskChoice.getSelectedItem()).getTask();
            task.setCompletionStatus(CompletionStatus.COMPLETE);
            projectViewModel.doAction(dao -> dao.updateTask(task));
            taskChoice.setSelection(0);
            adapter.notifyDataSetChanged();
        });
        projectViewModel.getProjectDao().getTaskViews(findDaysSinceEpoch()).observe(getViewLifecycleOwner(), taskViews -> {
            adapterTaskViews.clear();
            int highestPriority = -1;
            for (TaskView taskView : taskViews) {
                if (taskView.getTask().getPriority().getNumber() >= highestPriority && taskView.findTimeToDoToday() > 0 //&&
                        //(taskView.findTargetToday() >= 900_000 || taskView.findDaysUntilDeadline() <= 1)
                        // A task must either last at least 15 minutes or be close to deadline (without the second case, short tasks would never be done).
                        && taskView.getCompletionStatus() == CompletionStatus.IN_PROGRESS) {
                    if (highestPriority == -1) highestPriority = taskView.getTask().getPriority().getNumber();
                    adapterTaskViews.add(taskView);
                }
            }
            adapterTaskViews.sort(Comparator.comparingLong(taskView -> ((TaskView) taskView).getTask().lastUsed).reversed());
            adapter.notifyDataSetChanged();
            if (adapterTaskViews.isEmpty()) {
                viewModel.setTaskSelected(null);
                viewModel.setRecord(null);
                editor.putLong(taskStartRef, -1);
                editor.apply();
            } else {
                boolean taskNotFound = true;
                if (viewModel.getTaskSelected() != null) {
                    for (TaskView taskView : adapterTaskViews) {
                        if (taskView.getTask().id == viewModel.getTaskSelected().id) {
                            taskNotFound = false;
                            break;
                        }
                    }
                }
                if (taskNotFound) {
                    taskChoice.setSelection(0);
                    viewModel.setTaskSelected(adapterTaskViews.get(0).getTask());
                    changeRecordTask(adapterTaskViews.get(0).getTask());
                }
            }
            if (adapterTaskViews.isEmpty()) startStopButton.setEnabled(false);
            if (viewModel.getTaskSelected() != null) {
                int i = 0;
                for (TaskView taskView : adapterTaskViews) {
                    if (taskView.getTask().id == viewModel.getTaskSelected().id) {
                        taskChoice.setSelection(i);
                        break;
                    }
                    i++;
                }
            }
        });
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        taskChoice = view.findViewById(R.id.spinner_task_name);
        taskChoice.setAdapter(adapter);
        taskChoice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                changeRecordTask(adapterTaskViews.get(position).getTask());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void changeRecordTask(Task newTask) {
        updateRecord(() -> {
            viewModel.setTaskSelected(newTask);
            //noinspection ConstantConditions
            if (viewModel.getTimerOn().getValue()) {
                long time = System.currentTimeMillis();
                viewModel.setRecord(new TaskTimeRecord(time, findDaysSinceEpoch(), viewModel.getTaskSelected().id,
                        privateStudyBox.isChecked()));
                editor.putLong(taskStartRef, time);
                editor.apply();
            }
        });
    }

    private void manageDay() {
        if (dayView == null) {
            /*AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
            builder.setTitle("Hours in school today");

            FrameLayout dialogContainer = new FrameLayout(getActivity());
            dialogContainer.setPadding(50, 5, 40, 5);
            final EditText input = new EditText(getActivity());
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
            dialogContainer.addView(input);
            builder.setView(dialogContainer);
            builder.setPositiveButton("Submit", (dialog, which) -> {*/
//                long schoolMillis = (long) (Double.parseDouble(input.getText().toString()) * 3600_000);
//                long targetMillis = schoolMillis == 0 ? 25 * 1800_000 : 13 * 1800_000; // "else" clause is 8.5 hours so 3.5 hours target normally
                AlertDialog dialog = new AlertDialog.Builder(requireActivity()).create();
                dialog.setTitle("Welcome back!");
                dialog.setMessage("A new day has started.");
                dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "OK", (dialog2, which) -> dialog2.dismiss());
                dialog.show();

                Day newDay = new Day(findDaysSinceEpoch(), 30 * 1800_000, 0, 0);
                projectViewModel.doAction(dao -> dao.insertDay(newDay));
//            });
//            builder.show();

        } else {
            updateDayWorkTime(dayView);
            projectViewModel.getProjectDao().getTaskRecordsByDay(findDaysSinceEpoch()).observe(getViewLifecycleOwner(), records -> {
                long starting = dayView.subtractPrivateStudy(0);
                viewModel.setTimeSpentTodayValue(starting);

                projectViewModel.getProjectDao().getEventsByDay(findDaysSinceEpoch()).observe(getViewLifecycleOwner(), events -> {
                    schedule = new Schedule(settingsPref).schedule(timeToMillisSinceEpoch(LocalDateTime.now()
                                    .withHour(9).withMinute(0).withSecond(0).withNano(0)),
                    adjustedTargetWorkTime - starting, events);
                    long recordedStart = sharedPref.getLong(taskStartRef, -1);
                    if (recordedStart != -1) {
                        projectViewModel.getProjectDao().getTaskRecordByTime(recordedStart).observe(getViewLifecycleOwner(), record -> {
                            if (record != null && taskStartNotAdded) {
                                viewModel.setRecord(record);
                                record.setLength(System.currentTimeMillis() - record.getStartTimeStamp());
                                Schedule.ScheduledItem previousItem = findItem(false);
                                sessionLength = Math.max(10_000,
                                        previousItem.getStart() + previousItem.getLength() - System.currentTimeMillis());
                                viewModel.setTimerOnValue(true);
                                taskStartNotAdded = false;
                            }
                        });
                    }
                });
                for (TaskTimeRecord record : records) viewModel.increaseTimeSpentTodayValue(record.getLength());
                
            });
            final long targetChange = 1800_000;
            /*view.findViewById(R.id.button_decrease_target).setOnClickListener(v -> {
                day.setTargetWorkTime(day.findTargetWorkTime() - targetChange);
                updateDayWorkTime(day, true);
            });
            view.findViewById(R.id.button_increase_target).setOnClickListener(v -> {
                day.setTargetWorkTime(day.findTargetWorkTime() + targetChange);
                updateDayWorkTime(day, true);
            });*/
        }
    }

    private void manageSharedPref(View view) {
        sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        final String dayReference = "WorkTimerFragment.daysSinceEpoch";

        if (sharedPref.getLong(dayReference, -1) != findDaysSinceEpoch()) {
            for (String ref : routineTaskReferences) editor.putBoolean(ref, false);
            if (!sharedPref.getBoolean(sleepReference, false)) {
                projectViewModel.getProjectDao().getDay(findDaysSinceEpoch() - 1)
                        .observe(getViewLifecycleOwner(), previous -> {
                            if (previous != null) {
                                previous.setMissedSleep(7200_000);
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

        settingsPref = PreferenceManager.getDefaultSharedPreferences(requireActivity());
        targetWorkProportion = Double.parseDouble(settingsPref.getString(SettingsFragment.TARGET_WORK_KEY, "")) / 100;
    }



    private void updateDayWorkTime(DayView dayView) {
        adjustedTargetWorkTime = (long) (dayView.findTargetWorkTime() * targetWorkProportion * 6 / 7);
        timeTargetText.setText(WorkOrBreakTimer.toHoursMinutes(adjustedTargetWorkTime));
        totalTimeText.setText(WorkOrBreakTimer.toHoursMinutes(dayView.findTargetWorkTime()));
    }

    public static long findDaysSinceEpoch() {
        return System.currentTimeMillis() / (24 * 3600 * 1000);
    }

    public static long timeToMillisSinceEpoch(LocalDateTime time) {
        return time.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000L;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateRecord(() -> {});
    }

    @Override
    public void onPause() {
        super.onPause();
        updateRecord(() -> {});
    }

    private void updateRecord(Runnable afterUpdate) {
        if (viewModel.getTaskSelected() != null) {
            viewModel.getTaskSelected().lastUsed = System.currentTimeMillis();
            projectViewModel.doAction(dao -> {
                dao.updateTask(viewModel.getTaskSelected());
                if (viewModel.getRecord() != null && viewModel.getRecord().getLength() > 0) {
                    if (dao.insertTaskRecord(viewModel.getRecord()) == -1) {
                        dao.updateTaskRecord(viewModel.getRecord());
                    }
                }
                afterUpdate.run();
            });
        }
    }

    private void createTimer() {
//        viewModel.setWorkTimer(true);
        viewModel.setTimerTypeValue("Work");
        WorkOrBreakTimer timer = new WorkOrBreakTimer(sessionLength);
        timer.setOnTick(this::onTick); timer.setOnFinish(this::onFinish);
        viewModel.setTimer(timer);
        viewModel.setPreviousTimeRemaining(sessionLength);
    }

    private boolean findItemCompare(long a, long b, boolean isNext) {
        return isNext ? a < b : a > b;
    }

    private Schedule.ScheduledItem findItem(boolean isNext) { // if isNext is false then isPrevious
        Schedule.ScheduledItem result = null;
        for (Schedule.ScheduledItem item : schedule) {
            if (findItemCompare(timeToMillisSinceEpoch(LocalDateTime.now()), item.getStart(), isNext)) {
                if (result == null || findItemCompare(item.getStart(), result.getStart(), isNext)) {
                    result = item;
                }
            }
        }
        return result;
    }

    private void startStop(boolean timerOn) {
        if (timerOn) {
            if (viewModel.getRecord() == null) {
                long time = System.currentTimeMillis();
                viewModel.setRecord(new TaskTimeRecord(time, findDaysSinceEpoch(), viewModel.getTaskSelected().id,
                        privateStudyBox.isChecked()));
                editor.putLong(taskStartRef, time);
                editor.apply();
            }

            startStopButton.setText(getText(R.string.btn_work_stop_text));
            startStopButton.setEnabled(true);
            createTimer();
            viewModel.getTimer().start();
            workTypeText.setText(viewModel.getTimerType().getValue());
            updateTaskChoiceEnabled();
        } else {
            if (viewModel.getRecord() != null) {
                editor.putLong(taskStartRef, -1);
                editor.apply();
                updateRecord(() -> viewModel.setRecord(null));
            }
            startStopButton.setText(getText(R.string.btn_work_start_text));
            startStopButton.setEnabled(false);
            viewModel.getTimer().pause();
            taskChoice.setEnabled(true);
            updatePrivateStudyBoxEnabled();
            workTypeText.setText("Break");

            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!Objects.requireNonNull(viewModel.getTimerOn().getValue())) {
                        long untilEnd = Duration.between(LocalTime.now(), LocalTime.of(22, 0)).toMillis();

                        long currentTime = timeToMillisSinceEpoch(LocalDateTime.now());
                        if (schedule != null) {
                            Schedule.ScheduledItem nextItem = findItem(true);
                            if (nextItem == null) {
                                startStopButton.setEnabled(false);
                                timeLeftText.setText(WorkOrBreakTimer.formatMilliseconds(untilEnd));
                            } else {
                                if (getContext() != null) {
                                    Intent intent = new Intent(requireContext(), ScheduledNotificationBroadcast.class);
                                    intent.setFlags(Intent.FLAG_RECEIVER_FOREGROUND);
                                    PendingIntent pendingIntent = PendingIntent.getBroadcast(requireContext(), 0, intent, 0);
                                    AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
                                    alarmManager.cancel(pendingIntent);
                                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextItem.getStart() - 60_000, pendingIntent);
                                    // Can prevent multiple alarms by adding a condition that currentMillis < nextItem.getStart() - 60_000. However, I currently like the multiple alarms.
                                }

                                sessionLength = nextItem.getLength();
                                long timeLeft = nextItem.getStart() - currentTime;
                                startStopButton.setEnabled(timeLeft < 300_000);
                                timeLeftText.setText(WorkOrBreakTimer.formatMilliseconds(timeLeft));
                            }
                        }

                        if (untilEnd < 0) {
                            workTypeText.setText("Free time");
                            startStopButton.setText("Sleep");
                            startStopButton.setEnabled(!sharedPref.getBoolean(sleepReference, false));
                            startStopButton.setOnClickListener(view -> {
                                dayView.getDay().setMissedSleep(-untilEnd);
                                projectViewModel.doAction(dao -> dao.updateDay(dayView.getDay()));
                                editor.putBoolean(sleepReference, true);
                                editor.apply();
                                startStopButton.setEnabled(false);
                            });
                        }
                       /* assert viewModel.getTimeSpentToday().getValue() != null;
                        long workLeftToday = adjustedTargetWorkTime - viewModel.getTimeSpentToday().getValue();
                        long workLeftWithBreaks = workLeftToday + 300_000 *
                                ((long) Math.ceil((double) workLeftToday / 1800_000) - 1);
                        long routineMillis = 0;
                        for (int i = 0; i < routineTasksDone.length; i++) if (!routineTasksDone[i]) routineMillis += routineTimes[i];
                        timeLeftText.setText(WorkOrBreakTimer.formatMilliseconds(untilEnd - workLeftWithBreaks - routineMillis));*/


                        handler.postDelayed(this, 1000);
                    }
                }
            }, 0);
        }
    }


    private void showSchedule(View view) {
        AlertDialog dialog = new AlertDialog.Builder(requireActivity()).create();
        dialog.setTitle("Schedule");
        StringBuilder scheduleString = new StringBuilder();
        for (Schedule.ScheduledItem item : schedule) {
            scheduleString.append(item.toString()).append("\n");
        }
        dialog.setMessage(scheduleString.toString());
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "OK", (dialog2, which) -> {
            dialog2.dismiss();
        });
        dialog.show();
    }

    private void updateTimeDisplay(long millisUntilFinished) {
        timeLeftText.setText(WorkOrBreakTimer.formatMilliseconds(millisUntilFinished));
    }

    private void updateTaskChoiceEnabled() {
//        taskChoice.setEnabled(!viewModel.isWorkTimer()); // Currently is always enabled
        updatePrivateStudyBoxEnabled();
    }

    private void updatePrivateStudyBoxEnabled() {
        privateStudyBox.setEnabled(taskChoice.isEnabled());
    }

    private void onTick(long millisUntilFinished) {
        if (/*viewModel.isWorkTimer() && */viewModel.getTaskSelected() != null) {
            long change = viewModel.getPreviousTimeRemaining() - millisUntilFinished;
            viewModel.increaseTimeSpentTodayValue(change);
            viewModel.getRecord().setLength(System.currentTimeMillis() - viewModel.getRecord().getStartTimeStamp());
            viewModel.setPreviousTimeRemaining(millisUntilFinished);
        }
        updateTimeDisplay(millisUntilFinished);
        updateTaskChoiceEnabled();
    }

    private void onFinish() {
        if (getContext() != null) {
            ScheduledNotificationBroadcast.createNotification(requireContext(), viewModel.getTimerType().getValue() + " finished");
        }
//        createTimer();
//        viewModel.getTimer().start();
        Log.d("project", "Timer finish");
        viewModel.setTimerOnValue(false);
    }
}