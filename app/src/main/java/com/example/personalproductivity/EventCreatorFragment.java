package com.example.personalproductivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.example.personalproductivity.databinding.FragmentEventCreatorBinding;

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
    private Event startingEvent;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEventCreatorBinding.inflate(inflater, container, false);
        binding.buttonSubmit.setOnClickListener(this::submit);
        projectViewModel = new ViewModelProvider(requireActivity()).get(ProjectViewModel.class);
        if (getArguments() != null) {
            startingEvent = EventCreatorFragmentArgs.fromBundle(getArguments()).getStartingEvent();
            if (startingEvent != null) binding.editName.setText(startingEvent.getName());
        }
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        navController = Navigation.findNavController(view);
    }

    private void submit(View view) {
        boolean editing = startingEvent != null;
        Event event = editing ? startingEvent : new Event("", 0, 0, 0);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        try {
            LocalDate date = LocalDate.parse(binding.editDate.getText().toString(), DateTimeFormatter.ofPattern("dd/MM/yy"));
            LocalTime start = LocalTime.parse(binding.editTimeStart.getText().toString(), timeFormatter);
            long epochTime = LocalDateTime.of(date, start).atZone(ZoneId.systemDefault()).toEpochSecond() * 1000L;
            LocalTime end = LocalTime.parse(binding.editTimeEnd.getText().toString(), timeFormatter);
            long length = (end.toSecondOfDay() - start.toSecondOfDay()) * 1000L;

            event.setName(binding.editName.getText().toString());
            event.setDaysSinceEpoch(date.toEpochDay());
            event.setStartTimeStamp(epochTime);
            event.setLength(length);

            if (editing) projectViewModel.doAction(dao -> dao.updateEvent(event));
            else projectViewModel.doAction(dao -> dao.insertEvent(event));
            navController.popBackStack();
        } catch (DateTimeParseException e) {
            binding.textMessage.setText("Invalid input");
        }
    }
}
