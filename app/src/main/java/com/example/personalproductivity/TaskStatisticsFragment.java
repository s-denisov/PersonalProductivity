package com.example.personalproductivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.tabs.TabLayout;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class TaskStatisticsFragment extends Fragment {

    private static final ValueFormatter daysValueFormatter = new ValueFormatter() {
        @Override
        public String getFormattedValue(float value) {
            final String[] DAYS = { "MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN" };
            return DAYS[(int) value];
        }
    };
    private final float SCHOOL_EFFICIENCY_MODIFIER = 0.8f;
    private View view;
    private BarChart chart;
    private ProjectViewModel viewModel;
    private ProjectDao dao;
    private TextView weekTotalText;
    private TextView dateText;
    private NavController navController;
    private long daysBeforeCurrent = 0;
    private Consumer<Task> onResult;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_task_statistics, container, false);
        viewModel =  new ViewModelProvider(this).get(ProjectViewModel.class);
        dao = viewModel.getProjectDao();
        weekTotalText = view.findViewById(R.id.text_week_total);
        dateText = view.findViewById(R.id.text_date);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        navController = Navigation.findNavController(view);

        if (onResult != null) {
            FragmentResultHelper helper = new FragmentResultHelper(navController);
            helper.getNavigationResultLiveData(ProjectListFragment.resultReference).observe(getViewLifecycleOwner(), task -> {
                onResult.accept(((TaskView) task).getTask());
                onResult = null;
            });
        }

        TabLayout chartType = view.findViewById(R.id.tab_chart_type);
        AtomicBoolean selectedByUser = new AtomicBoolean(true);
        chartType.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (selectedByUser.get()) {
                    navController.navigate(TaskStatisticsFragmentDirections.openTab().setTab(tab.getPosition()));
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        if (getArguments() != null) {
            TabLayout.Tab selectedTab = chartType.getTabAt(TaskStatisticsFragmentArgs.fromBundle(getArguments()).getTab());
            if (selectedTab != null) {
                selectedByUser.set(false);
                selectedTab.select();
                selectedByUser.set(true);

                if (Objects.equals(selectedTab.getText(), getString(R.string.record_tab))) createRecordChart();
                else if (Objects.equals(selectedTab.getText(), getString(R.string.target_tab))) createTargetChart();
                else createWorkList();
                return;
            }
        }
        createRecordChart();
    }

    private void createRecordChart() {
        FrameLayout layout = view.findViewById(R.id.frame_chart);
        chart = new BarChart(getContext());
        layout.removeAllViews();
        layout.addView(chart);
        chart.getXAxis().setValueFormatter(daysValueFormatter);
        chart.getAxisLeft().setAxisMinimum(0);
        chart.getAxisLeft().setAxisMaximum(10);
        chart.getAxisRight().setAxisMinimum(0);
        chart.getAxisRight().setAxisMaximum(10);
        chart.getLegend().setWordWrapEnabled(true);
        chart.getDescription().setEnabled(false);
        manageWeekChanges(true, this::updateRecordChart);
    }

    private void updateRecordChart() {
        dao.getProjects().observe(getViewLifecycleOwner(), projects -> {
            List<BarEntry> entries = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                float[] defaultData = new float[projects.size() + 1];
                Arrays.fill(defaultData, 0);
                entries.add(new BarEntry(i, defaultData));
            }
            List<Integer> projectColors = new ArrayList<>();
            for (int i = 0; i <= projects.size(); i++) {
                projectColors.add(Color.HSVToColor(new float[]{(float) i * 360 / (projects.size() + 1), 0.4f, 1}));
                // remove key and replace with recyclerview, which has project name, color, and hours worked during period
            }

            BarDataSet dataSet = new BarDataSet(entries, "");
            dataSet.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return formatHours(value);
                }
            });
            dataSet.setValueTextSize(12);

            BarData data = new BarData(dataSet);
            chart.setData(data);
            dataSet.setColors(projectColors);
            dataSet.setStackLabels(Stream.concat(Stream.of("School"), projects.stream().map(Project::getName)).toArray(String[]::new));
            chart.invalidate();
            MutableLiveData<Float> totalHours = new MutableLiveData<>(0f);
            totalHours.observe(getViewLifecycleOwner(), hours -> weekTotalText.setText(formatHours(hours) + " h"));
            long day = findPreviousMonday();
            for (int i = 0; i < 7; i++) {
                int daysSinceStart = i;
                dao.getTaskRecordsByDay(day + i).observe(getViewLifecycleOwner(), records -> dao.getDayView(
                        day + daysSinceStart).observe(getViewLifecycleOwner(), dayData -> {
                    float[] projectTimes = new float[projects.size() + 1];
                    Arrays.fill(projectTimes, 0);
                    if (dayData != null) {
                        projectTimes[0] = SCHOOL_EFFICIENCY_MODIFIER * dayData.getTotalSchoolLessons();
                        totalHours.setValue(totalHours.getValue() + projectTimes[0]);
                    }
                    if (records.isEmpty()) {
                        chart.notifyDataSetChanged();
                        chart.invalidate();
                    }
                    for (TaskTimeRecord record : records) {
                        dao.getProjectFromTask(record.getTaskId()).observe(getViewLifecycleOwner(), project -> {
                            int index = projects.indexOf(project) + 1;
                            float hoursWorked = (float) record.getLength() / 3600_000;
                            projectTimes[index] += hoursWorked;
                            assert totalHours.getValue() != null;
                            totalHours.setValue(totalHours.getValue() + hoursWorked);
                            entries.set(daysSinceStart, new BarEntry(daysSinceStart, projectTimes));
                            chart.notifyDataSetChanged();
                            chart.invalidate();
                        });
                    }
                }));
            }
        });
    }

    private String formatHours(float hours) {
        return String.format("%.2f", hours);
    }

    private void createTargetChart() {
        FrameLayout layout = view.findViewById(R.id.frame_chart);
        layout.removeAllViews();
        LineChart timeChart = new LineChart(getContext());
        timeChart.getXAxis().setValueFormatter(daysValueFormatter);
        timeChart.getXAxis().setAxisMinimum(-0.5f);
        timeChart.getXAxis().setAxisMaximum(6.5f);
        timeChart.getAxisLeft().setAxisMinimum(0);
        timeChart.getAxisLeft().setAxisMaximum(100);
        timeChart.getAxisRight().setAxisMinimum(0);
        timeChart.getAxisRight().setAxisMaximum(100);
        timeChart.getLegend().setEnabled(false);
        timeChart.getDescription().setEnabled(false);
        layout.addView(timeChart);
        manageWeekChanges(true, () -> updateTargetChart(timeChart));
    }

    private void updateTargetChart(LineChart timeChart) {

        List<ILineDataSet> dataSets = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            List<Entry> barPoints = new ArrayList<>();
            barPoints.add(new Entry((float) (i - 0.5), 0));
            barPoints.add(new Entry((float) (i + 0.5), 0));
            LineDataSet dataSet = new LineDataSet(barPoints, "");
            dataSets.add(dataSet);
        }
        LineData data = new LineData(dataSets);
        timeChart.setData(data);

        long day = findPreviousMonday();
        AtomicReference<Float> totalMillisWorked = new AtomicReference<>(0f);
        AtomicLong totalMillisAvailable = new AtomicLong(0);
        for (int i = 0; i < 7; i++) {
            int daysSinceStart = i;
            dao.getTaskRecordsByDay(day + i).observe(getViewLifecycleOwner(), records -> dao.getDayView(
                    day + daysSinceStart).observe(getViewLifecycleOwner(), dayData -> { if (dayData == null) return;
                    Log.d("project", dayData.toString());
                long millisWorked = 0;
                for (TaskTimeRecord record : records) millisWorked += record.getLength();
                float adjustedMillisWorked = 7.0f / 6 * dayData.subtractPrivateStudy(millisWorked);
                float percentageWorked = Math.max(0, (100 * adjustedMillisWorked + 50 * dayData.getTotalFunLength())
                        / dayData.findTargetWorkTime());
                if (daysSinceStart == 5) Log.d("project", String.valueOf(adjustedMillisWorked / dayData.findTargetWorkTime()));
                List<Entry> barPoints = new ArrayList<>();
                float halfBarWidth = (float) dayData.findTargetWorkTime() / 100_000_000; // 13.89 hours fills entire width
                barPoints.add(new Entry(daysSinceStart - halfBarWidth, percentageWorked));
                barPoints.add(new Entry(daysSinceStart + halfBarWidth, percentageWorked));
                LineDataSet dataSet = new LineDataSet(barPoints, "");
                dataSet.setFillDrawable(ContextCompat.getDrawable(requireContext(), R.color.colorPrimary));
                dataSet.setDrawCircles(false);
                dataSet.setDrawFilled(true);
                dataSet.setDrawValues(false);
                dataSets.set(daysSinceStart, dataSet);

                timeChart.notifyDataSetChanged();
                timeChart.invalidate();

                totalMillisWorked.set(totalMillisWorked.get() + adjustedMillisWorked);
                totalMillisAvailable.addAndGet(dayData.findTargetWorkTime());
                weekTotalText.setText(String.format("%.1f", 100 * totalMillisWorked.get() / totalMillisAvailable.get()) + "%");
            }));
        }
    }

    private void createWorkList() {
        FrameLayout layout = view.findViewById(R.id.frame_chart);
        layout.removeAllViews();
        weekTotalText.setText("");
        RecyclerView recyclerView = new RecyclerView(requireContext());
        layout.addView(recyclerView);
        TimeRangeItemAdapter adapter = new TimeRangeItemAdapter((recordTask, record) ->
                dao.getTask(((TaskTimeRecord) record).getTaskId()).observe(getViewLifecycleOwner(), task -> recordTask.setText(task.getName())),
                (record) -> {
                    onResult = task -> {
                        TaskTimeRecord taskRecord = (TaskTimeRecord) record;
                        taskRecord.setTaskId(task.id);
                        viewModel.doAction(dao -> dao.updateTaskRecord(taskRecord));
                    };
                    navController.navigate(ProjectListFragmentDirections.requestTaskOrParent().setIsRequest(true));
                });
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        manageWeekChanges(false, () -> updateWorkList(adapter));
    }

    private void updateWorkList(TimeRangeItemAdapter adapter) {
        dao.getTaskRecordsByDay(WorkTimerFragment.findDaysSinceEpoch() - daysBeforeCurrent)
                .observe(getViewLifecycleOwner(), adapter::convertAndSubmitList);
    }

    private String formatDate(long daysSinceEpoch) {
        return LocalDate.ofEpochDay(daysSinceEpoch).format(DateTimeFormatter.ofPattern("dd/MM/yy"));
    }

    private long findPreviousMonday() {
        long day = WorkTimerFragment.findDaysSinceEpoch() - daysBeforeCurrent;
        while (day % 7 != 4) { // day % 7 == 4 means day is Monday
            day--;
        }
        return day;
    }

    private void manageWeekChanges(boolean updateWeek, Runnable after) {
        int change = updateWeek ? 7 : 1;
        updateWeekTitle(updateWeek);
        after.run();
        view.findViewById(R.id.button_previous).setOnClickListener(v -> {
            daysBeforeCurrent += change;
            updateWeekTitle(updateWeek);
            after.run();
        });
        view.findViewById(R.id.button_next).setOnClickListener(v -> {
            daysBeforeCurrent -= change;
            updateWeekTitle(updateWeek);
            after.run();
        });
    }


    private void updateWeekTitle(boolean updateWeek) {
        long firstDay = updateWeek ? findPreviousMonday() : WorkTimerFragment.findDaysSinceEpoch() - daysBeforeCurrent;
        String result = formatDate(firstDay);
        if (updateWeek) result += " - " + formatDate(firstDay + 6);
        dateText.setText(result);
    }
}
