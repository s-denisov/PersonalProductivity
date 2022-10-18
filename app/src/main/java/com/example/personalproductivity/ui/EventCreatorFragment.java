package com.example.personalproductivity.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.example.personalproductivity.viewmodels.ProjectViewModel;
import com.example.personalproductivity.WorkOrBreakTimer;
import com.example.personalproductivity.databinding.FragmentEventCreatorBinding;
import com.example.personalproductivity.db.Event;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class EventCreatorFragment extends Fragment {

    private FragmentEventCreatorBinding binding;
    private ProjectViewModel projectViewModel;
    private NavController navController;
    private Event event;
    private boolean editing;

    @SuppressLint("SetTextI18n")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEventCreatorBinding.inflate(inflater, container, false);
        binding.buttonSubmit.setOnClickListener(this::submit);
        projectViewModel = new ViewModelProvider(requireActivity()).get(ProjectViewModel.class);
        if (getArguments() != null) event = EventCreatorFragmentArgs.fromBundle(getArguments()).getStartingEvent();
        editing = event != null;
        if (event == null) event = new Event("", 0, 0, 0, 0, 0, 0);
        binding.editName.setText(event.getName());
        binding.editSchoolLessons.setText(Integer.toString(event.getSchoolLessons()));
        binding.editChoreTime.setText(WorkOrBreakTimer.toHoursMinutes(event.getChoreLength()));
        binding.editFunTime.setText(WorkOrBreakTimer.toHoursMinutes(event.getFunLength()));

        if (event == null) {
            binding.editSchoolLessons.setText("0");
            binding.editTimeStart.setText("0");
        } else {
            binding.editName.setText(event.getName());
            binding.editSchoolLessons.setText(Integer.toString(event.getSchoolLessons()));
        }
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        navController = Navigation.findNavController(view);
    }

    private void submit(View view) {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        try {
            LocalDate date = LocalDate.parse(binding.editDate.getText().toString(), DateTimeFormatter.ofPattern("dd/MM/yy"));
            LocalTime start = LocalTime.parse(binding.editTimeStart.getText().toString(), timeFormatter);
            long epochTime = LocalDateTime.of(date, start).atZone(ZoneId.systemDefault()).toEpochSecond() * 1000L;
            LocalTime end = LocalTime.parse(binding.editTimeEnd.getText().toString(), timeFormatter);
            long length = (end.toSecondOfDay() - start.toSecondOfDay()) * 1000L;
            if (editing && epochTime + length > event.getStartTimeStamp() + event.getLength()) {
                binding.textMessage.setText("New end can't be later than old end");
                return;
            }
            event.setName(binding.editName.getText().toString());
            event.setDaysSinceEpoch(date.toEpochDay());
            event.setStartTimeStamp(epochTime);
            event.setLength(length);
            event.setSchoolLessons(Integer.parseInt(binding.editSchoolLessons.getText().toString()));
            event.setChoreLength(TaskCreatorFragment.hourMinToMillis(binding.editChoreTime.getText().toString()));
            event.setFunLength(TaskCreatorFragment.hourMinToMillis(binding.editFunTime.getText().toString()));
            if (event.getSchoolLessons() == 0 && Math.max(event.getChoreLength(), event.getFunLength()) < 600_000) {
                binding.textMessage.setText("Useful time must be at least 10 minutes");
                return;
            }
            if (editing) projectViewModel.doAction(dao -> dao.updateEvent(event));
            else projectViewModel.doAction(dao -> dao.insertEvent(event));
            navController.popBackStack();
        } catch (DateTimeParseException e) {
            binding.textMessage.setText("Invalid input");
        }
        Log.d("project", binding.textMessage.getText().toString());
    }
}
