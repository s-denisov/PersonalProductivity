package com.example.personalproductivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.example.personalproductivity.databinding.FragmentTaskCreatorBinding;

import java.time.LocalDateTime;

public class TaskCreatorFragment extends Fragment {

    private FragmentTaskCreatorBinding binding;
    private ProjectViewModel projectViewModel;
    private NavController navController;
    private int parentId;
    private Task startingTask;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentTaskCreatorBinding.inflate(inflater, container, false);
        binding.buttonSubmit.setOnClickListener(this::submit);
        projectViewModel = new ViewModelProvider(requireActivity()).get(ProjectViewModel.class);
        if (getArguments() != null) {
            TaskCreatorFragmentArgs args = TaskCreatorFragmentArgs.fromBundle(getArguments());
            parentId = args.getParentId();
            startingTask = args.getStartingTask();
        }
        if (startingTask != null) binding.editTaskName.setText(startingTask.getName());
        ArrayAdapter<Priority> adapter = new ArrayAdapter<>(requireActivity(),
                android.R.layout.simple_spinner_dropdown_item, Priority.values());
        binding.spinnerTaskPriority.setAdapter(adapter);
        binding.spinnerTaskPriority.setSelection(2);
        if (startingTask != null) {
            binding.spinnerTaskPriority.setSelection(adapter.getPosition(startingTask.getPriority()));
            binding.editDeadline.setText(String.valueOf(startingTask.getDeadlineDate() - WorkTimerFragment.findDaysSinceEpoch()));
            binding.editExpectedTime.setText(WorkOrBreakTimer.toHoursMinutes(startingTask.expectedTime));
        }

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        navController = Navigation.findNavController(view);
    }

    private void submit(View view) {
        Task task = startingTask == null ? new Task() : startingTask;
        task.setName(binding.editTaskName.getText().toString());
        task.setParentId(parentId);
        task.setPriority((Priority) binding.spinnerTaskPriority.getSelectedItem());
        String[] expectedTimes = binding.editExpectedTime.getText().toString().split(":");
        task.setExpectedTime(60_000 * (Long.parseLong(expectedTimes[0]) * 60 + Long.parseLong(expectedTimes[1])));
        LocalDateTime deadline = LocalDateTime.now().withHour(22).withMinute(0).withSecond(0).withNano(0)
                .plusDays(Integer.parseInt(binding.editDeadline.getText().toString()) - 1);
        task.setDeadlineDate(WorkTimerFragment.findDaysSinceEpoch() + Integer.parseInt(binding.editDeadline.getText().toString()));
        projectViewModel.doAction(dao -> {
            if (startingTask == null) dao.insertTask(task); else dao.updateTask(task);
        });
        navController.popBackStack();
    }
}
