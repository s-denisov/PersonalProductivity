package com.example.personalproductivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TaskStatisticsFragment extends Fragment {

    private static final String[] DAYS = { "MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN" };
    private BarChart chart;
    private ProjectDao dao;
    private TextView totalHoursText;
    private long daysBeforeCurrent = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task_statistics, container, false);
        totalHoursText = view.findViewById(R.id.text_total_hours);
        chart = view.findViewById(R.id.bar_chart_tasks);
        chart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return DAYS[(int) value];
            }
        });
        chart.getAxisLeft().setAxisMinimum(0);
        chart.getAxisLeft().setAxisMaximum(8);
        chart.getAxisRight().setAxisMinimum(0);
        chart.getAxisRight().setAxisMaximum(8);
        chart.getLegend().setWordWrapEnabled(true);
        chart.getDescription().setEnabled(false);

        dao = new ViewModelProvider(this).get(ProjectViewModel.class).getProjectDao();
        updateChart();
        view.findViewById(R.id.button_previous).setOnClickListener(v -> {
            daysBeforeCurrent += 7;
            updateChart();
        });
        view.findViewById(R.id.button_next).setOnClickListener(v -> {
            daysBeforeCurrent -= 7;
            updateChart();
        });

        return view;
    }

    private void updateChart() {
        dao.getProjects().observe(getViewLifecycleOwner(), projects -> {
            List<BarEntry> entries = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                float[] defaultData = new float[projects.size()];
                Arrays.fill(defaultData, 0);
                entries.add(new BarEntry(i, defaultData));
            }
            List<Integer> projectColors = new ArrayList<>();
            for (int i = 0; i < projects.size(); i++) {
                projectColors.add(Color.HSVToColor(new float[]{(float) i * 360 / projects.size(), 0.4f, 1}));
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
            dataSet.setStackLabels(projects.stream().map(Project::getName).toArray(String[]::new));
            chart.invalidate();
            MutableLiveData<Float> totalHours = new MutableLiveData<>(0f);
            totalHours.observe(getViewLifecycleOwner(), hours -> totalHoursText.setText(formatHours(hours)));
            long day = WorkTimerFragment.findDaysSinceEpoch() - daysBeforeCurrent;
            while (day % 7 != 4) {
                day--;
            }
            for (int daysSinceStart = 0; daysSinceStart < 7; daysSinceStart++) {
                int finalDaysSinceStart = daysSinceStart;
                dao.getTaskRecordsByDay(day + daysSinceStart).observe(getViewLifecycleOwner(),
                    records -> {
                        float[] projectTimes = new float[projects.size()];
                        Arrays.fill(projectTimes, 0);
                        for (TaskTimeRecord record : records) {
                            dao.getProjectFromTask(record.getTaskId()).observe(getViewLifecycleOwner(), project -> {
                                int index = projects.indexOf(project);
                                float hoursWorked = (float) record.getLength() / 3600_000;
                                projectTimes[index] += hoursWorked;
                                assert totalHours.getValue() != null;
                                totalHours.setValue(totalHours.getValue() + hoursWorked);
                                entries.set(finalDaysSinceStart, new BarEntry(finalDaysSinceStart, projectTimes));
                                chart.notifyDataSetChanged();
                                chart.invalidate();
                            });
                        }
                    });
            }
        });
    }

    private String formatHours(float hours) {
        return String.format("%.2f", hours);
    }
}
