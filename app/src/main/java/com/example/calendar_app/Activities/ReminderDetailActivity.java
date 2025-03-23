package com.example.calendar_app.Activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.calendar_app.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ReminderDetailActivity extends AppCompatActivity {
    private LinearLayout viewModeContainer, editModeContainer;
    private TextView titleTextView, descriptionTextView, startTimeTextView, endTimeTextView, notificationTextView, statusTextView;
    private TextInputEditText titleEditText, descriptionEditText;
    private MaterialButton startDateButton, startTimeButton, endDateButton, endTimeButton;
    private MaterialButton editButton, deleteButton, saveButton, cancelButton;
    private SwitchMaterial notificationSwitch;
    private RadioGroup notificationTimeRadioGroup;

    private Reminder reminder;
    private LocalDate startDate, endDate;
    private LocalTime startTime, endTime;

    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy");
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");
    private DateTimeFormatter fullFormatter = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy 'at' h:mm a");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_details);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        initializeViews();

        loadReminderData();

        setupListeners();

        updateUI();
    }

    private void initializeViews() {
        viewModeContainer = findViewById(R.id.viewModeContainer);
        titleTextView = findViewById(R.id.titleTextView);
        descriptionTextView = findViewById(R.id.descriptionTextView);
        startTimeTextView = findViewById(R.id.startTimeTextView);
        endTimeTextView = findViewById(R.id.endTimeTextView);
        notificationTextView = findViewById(R.id.notificationTextView);
        statusTextView = findViewById(R.id.statusTextView);
        editButton = findViewById(R.id.editButton);
        deleteButton = findViewById(R.id.deleteButton);

        editModeContainer = findViewById(R.id.editModeContainer);
        titleEditText = findViewById(R.id.titleEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        startDateButton = findViewById(R.id.startDateButton);
        startTimeButton = findViewById(R.id.startTimeButton);
        endDateButton = findViewById(R.id.endDateButton);
        endTimeButton = findViewById(R.id.endTimeButton);
        notificationSwitch = findViewById(R.id.notificationSwitch);
        notificationTimeRadioGroup = findViewById(R.id.notificationTimeRadioGroup);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);
    }

    private void loadReminderData() {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.plusHours(1);
        LocalDateTime end = start.plusHours(1);

        reminder = new Reminder(
                "1",
                "Project Team Meeting",
                "Discuss project progress and next steps with the team. Prepare presentation and status report.",
                start.toLocalDate(),
                start.toLocalTime(),
                end.toLocalDate(),
                end.toLocalTime(),
                true,
                10
        );

        startDate = reminder.getStartDate();
        startTime = reminder.getStartTime();
        endDate = reminder.getEndDate();
        endTime = reminder.getEndTime();
    }

    private void setupListeners() {
        editButton.setOnClickListener(v -> switchToEditMode());
        deleteButton.setOnClickListener(v -> showDeleteConfirmation());

        startDateButton.setOnClickListener(v -> showDatePicker(true));
        startTimeButton.setOnClickListener(v -> showTimePicker(true));
        endDateButton.setOnClickListener(v -> showDatePicker(false));
        endTimeButton.setOnClickListener(v -> showTimePicker(false));
        saveButton.setOnClickListener(v -> saveChanges());
        cancelButton.setOnClickListener(v -> switchToViewMode());
    }

    private void updateUI() {
        titleTextView.setText(reminder.getTitle());
        descriptionTextView.setText(reminder.getDescription());

        LocalDateTime startDateTime = LocalDateTime.of(reminder.getStartDate(), reminder.getStartTime());
        LocalDateTime endDateTime = LocalDateTime.of(reminder.getEndDate(), reminder.getEndTime());

        startTimeTextView.setText(startDateTime.format(fullFormatter));
        endTimeTextView.setText(endDateTime.format(fullFormatter));

        notificationTextView.setText(formatNotificationTime(reminder.getNotificationMinutes()) + " before");

        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(endDateTime)) {
            statusTextView.setText("Completed");
            statusTextView.setBackgroundResource(R.drawable.status_background);
        } else if (now.isAfter(startDateTime)) {
            statusTextView.setText("In Progress");
            statusTextView.setBackgroundResource(R.drawable.status_background);
            statusTextView.setTextColor(getResources().getColor(R.color.white));
        } else {
            statusTextView.setText("Upcoming");
            statusTextView.setBackgroundResource(R.drawable.status_background);
            statusTextView.setTextColor(getResources().getColor(R.color.white));
        }

        titleEditText.setText(reminder.getTitle());
        descriptionEditText.setText(reminder.getDescription());

        updateDateTimeButtons();

        notificationSwitch.setChecked(reminder.isHasNotification());

        int notificationMinutes = reminder.getNotificationMinutes();
        if (notificationMinutes == 10) {
            notificationTimeRadioGroup.check(R.id.radio10min);
        } else if (notificationMinutes == 30) {
            notificationTimeRadioGroup.check(R.id.radio30min);
        } else if (notificationMinutes == 60) {
            notificationTimeRadioGroup.check(R.id.radio1hour);
        } else if (notificationMinutes == 24 * 60) {
            notificationTimeRadioGroup.check(R.id.radio1day);
        }
    }

    private void updateDateTimeButtons() {
        startDateButton.setText(startDate.format(dateFormatter));
        startTimeButton.setText(startTime.format(timeFormatter));
        endDateButton.setText(endDate.format(dateFormatter));
        endTimeButton.setText(endTime.format(timeFormatter));
    }

    private void switchToEditMode() {
        viewModeContainer.setVisibility(View.GONE);
        editModeContainer.setVisibility(View.VISIBLE);
    }

    private void switchToViewMode() {
        editModeContainer.setVisibility(View.GONE);
        viewModeContainer.setVisibility(View.VISIBLE);
    }

    private void showDatePicker(boolean isStart) {
        LocalDate currentDate = isStart ? startDate : endDate;

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    LocalDate selectedDate = LocalDate.of(year, month + 1, dayOfMonth);
                    if (isStart) {
                        startDate = selectedDate;

                        if (endDate.isBefore(startDate)) {
                            endDate = startDate;
                            updateDateTimeButtons();
                        }
                    } else {
                        endDate = selectedDate;
                    }
                    updateDateTimeButtons();
                },
                currentDate.getYear(),
                currentDate.getMonthValue() - 1,
                currentDate.getDayOfMonth()
        );

        if (!isStart) {
            datePickerDialog.getDatePicker().setMinDate(
                    startDate.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli());
        }

        datePickerDialog.show();
    }

    private void showTimePicker(boolean isStart) {
        LocalTime currentTime = isStart ? startTime : endTime;

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    LocalTime selectedTime = LocalTime.of(hourOfDay, minute);
                    if (isStart) {
                        startTime = selectedTime;

                        if (startDate.equals(endDate) && endTime.isBefore(startTime)) {
                            endTime = startTime.plusHours(1);
                            updateDateTimeButtons();
                        }
                    } else {
                        endTime = selectedTime;
                    }
                    updateDateTimeButtons();
                },
                currentTime.getHour(),
                currentTime.getMinute(),
                false
        );

        timePickerDialog.show();
    }

    private void saveChanges() {
        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();

        if (title.isEmpty()) {
            titleEditText.setError("Title is required");
            return;
        }

        boolean hasNotification = notificationSwitch.isChecked();
        int notificationMinutes = 10; // Default

        if (hasNotification) {
            int selectedRadioButtonId = notificationTimeRadioGroup.getCheckedRadioButtonId();

            if (selectedRadioButtonId == R.id.radio10min) {
                notificationMinutes = 10;
            } else if (selectedRadioButtonId == R.id.radio30min) {
                notificationMinutes = 30;
            } else if (selectedRadioButtonId == R.id.radio1hour) {
                notificationMinutes = 60;
            } else if (selectedRadioButtonId == R.id.radio1day) {
                notificationMinutes = 24 * 60;
            }
        }

        reminder.setTitle(title);
        reminder.setDescription(description);
        reminder.setStartDate(startDate);
        reminder.setStartTime(startTime);
        reminder.setEndDate(endDate);
        reminder.setEndTime(endTime);
        reminder.setHasNotification(hasNotification);
        reminder.setNotificationMinutes(notificationMinutes);


        updateUI();

        switchToViewMode();

        Toast.makeText(this, "Changes saved", Toast.LENGTH_SHORT).show();
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Reminder")
                .setMessage("Are you sure you want to delete this reminder?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    Toast.makeText(this, "Reminder deleted", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private String formatNotificationTime(int minutes) {
        if (minutes < 60) {
            return minutes + " minutes";
        } else if (minutes < 24 * 60) {
            int hours = minutes / 60;
            return hours + (hours == 1 ? " hour" : " hours");
        } else {
            int days = minutes / (24 * 60);
            return days + (days == 1 ? " day" : " days");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}