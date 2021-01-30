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
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
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
    private ProjectDao dao;
    private TextView totalHoursText;
    private long daysBeforeCurrent = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_task_statistics, container, false);
        dao = new ViewModelProvider(this).get(ProjectViewModel.class).getProjectDao();
        totalHoursText = view.findViewById(R.id.text_total_hours);

        TabLayout chartType = view.findViewById(R.id.tab_chart_type);
        TabLayout.Tab record = chartType.newTab().setText("Record");
        TabLayout.Tab target = chartType.newTab().setText("Target");
        chartType.addTab(record);
        chartType.addTab(target);
        chartType.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab == record) createRecordChart();
                else createTargetChart();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        createRecordChart();

        return view;
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

        updateRecordChart();
        view.findViewById(R.id.button_previous).setOnClickListener(v -> {
            daysBeforeCurrent += 7;
            updateRecordChart();
        });
        view.findViewById(R.id.button_next).setOnClickListener(v -> {
            daysBeforeCurrent -= 7;
            updateRecordChart();
        });
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
            totalHours.observe(getViewLifecycleOwner(), hours -> totalHoursText.setText(formatHours(hours) + " h"));
            long day = WorkTimerFragment.findDaysSinceEpoch() - daysBeforeCurrent;
            while (day % 7 != 4) { // day % 7 == 4 means day is Monday
                day--;
            }
            for (int i = 0; i < 7; i++) {
                int daysSinceStart = i;
                long day2 = day;
                dao.getTaskRecordsByDay(day + i).observe(getViewLifecycleOwner(), records -> dao.getDay(
                        day2 + daysSinceStart).observe(getViewLifecycleOwner(), dayData -> {
                    float[] projectTimes = new float[projects.size() + 1];
                    Arrays.fill(projectTimes, 0);
                    if (dayData != null) {
                        projectTimes[0] = SCHOOL_EFFICIENCY_MODIFIER * dayData.getSchoolTime() / 3600_000;
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

        view.findViewById(R.id.button_previous).setOnClickListener(v -> {
            daysBeforeCurrent += 7;
            updateTargetChart(timeChart);
        });
        view.findViewById(R.id.button_next).setOnClickListener(v -> {
            daysBeforeCurrent -= 7;
            updateTargetChart(timeChart);
        });

        updateTargetChart(timeChart);
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

        long day = WorkTimerFragment.findDaysSinceEpoch() - daysBeforeCurrent;
        while (day % 7 != 4) { // day % 7 == 4 means day is Monday
            day--;
        }
        AtomicReference<Float> totalMillisWorked = new AtomicReference<>(0f);
        AtomicLong totalMillisAvailable = new AtomicLong(0);
        for (int i = 0; i < 7; i++) {
            int daysSinceStart = i;
            long day2 = day;
            dao.getTaskRecordsByDay(day + i).observe(getViewLifecycleOwner(), records -> dao.getDay(
                    day2 + daysSinceStart).observe(getViewLifecycleOwner(), dayData -> { if (dayData == null) return;
                long millisWorked = 0;
                for (TaskTimeRecord record : records) millisWorked += record.getLength();
                float adjustedMillisWorked = 7.0f / 6 * dayData.subtractPrivateStudy(millisWorked);
                float percentageWorked = Math.max(0, 100 * adjustedMillisWorked / dayData.getTargetWorkTime());
                if (daysSinceStart == 5) Log.d("project", String.valueOf(adjustedMillisWorked / dayData.getTargetWorkTime()));
                List<Entry> barPoints = new ArrayList<>();
                float halfBarWidth = (float) dayData.getTargetWorkTime() / 100_000_000; // 13.89 hours fills entire width
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
                totalMillisAvailable.addAndGet(dayData.getTargetWorkTime());
                totalHoursText.setText(String.format("%.1f", 100 * totalMillisWorked.get() / totalMillisAvailable.get()) + "%");
            }));
        }
    }
}
