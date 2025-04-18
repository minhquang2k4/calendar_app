package com.example.calendar_app.Activities;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.calendar_app.AppDatabase;
import com.example.calendar_app.DAO.EventDAO;
import com.example.calendar_app.DAO.FirebaseEventDAO;
import com.example.calendar_app.Entities.EventEntity;

import com.example.calendar_app.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class AddReminderActivity extends AppCompatActivity {

    private TextInputEditText titleEditText, descriptionEditText;
    private MaterialButton startDateButton, startTimeButton, endDateButton, endTimeButton, saveButton;
    private SwitchMaterial notificationSwitch;
    private RadioGroup notificationTimeRadioGroup;
    private AppDatabase db;
    private EventDAO eventDAO;
    private String userId;
    private LocalDate startDate, endDate;
    private LocalTime startTime, endTime;

    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy");
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private DateTimeFormatter dbDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_reminder);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Thêm sự kiện mới");

        db = AppDatabase.getDatabase(this);
        EventDAO roomEventDAO = db.eventDao();
        eventDAO = new FirebaseEventDAO(roomEventDAO);

        userId = getIntent().getStringExtra("USER_ID");
        String selectedDateStr = getIntent().getStringExtra("SELECTED_DATE");

        initializeViews();
        initializeDefaultValues(selectedDateStr);
        setupListeners();
    }

    private void initializeViews() {
        titleEditText = findViewById(R.id.titleEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        startDateButton = findViewById(R.id.startDateButton);
        startTimeButton = findViewById(R.id.startTimeButton);
        endDateButton = findViewById(R.id.endDateButton);
        endTimeButton = findViewById(R.id.endTimeButton);
        notificationSwitch = findViewById(R.id.notificationSwitch);
        notificationTimeRadioGroup = findViewById(R.id.notificationTimeRadioGroup);
        saveButton = findViewById(R.id.saveButton);


        if (notificationSwitch.isChecked()) {
            requestNotificationPermissionIfNeeded();
        }
    }
    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1001);
            }
        }

    }


    private void initializeDefaultValues(String selectedDateStr) {
        startDate = selectedDateStr != null ? LocalDate.parse(selectedDateStr) : LocalDate.now();
        startTime = LocalTime.now().withSecond(0).withNano(0);
        endDate = startDate;
        endTime = startTime.plusHours(1);

        updateDateTimeButtons();
    }

    private void setupListeners() {
        startDateButton.setOnClickListener(v -> showDatePicker(true));
        startTimeButton.setOnClickListener(v -> showTimePicker(true));
        endDateButton.setOnClickListener(v -> showDatePicker(false));
        endTimeButton.setOnClickListener(v -> showTimePicker(false));
        saveButton.setOnClickListener(v -> saveEvent());
    }

    private void updateDateTimeButtons() {
        startDateButton.setText(startDate.format(dateFormatter));
        startTimeButton.setText(startTime.format(timeFormatter));
        endDateButton.setText(endDate.format(dateFormatter));
        endTimeButton.setText(endTime.format(timeFormatter));
    }

    private void showDatePicker(boolean isStart) {
        LocalDate currentDate = isStart ? startDate : endDate;
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    LocalDate selectedDate = LocalDate.of(year, month + 1, dayOfMonth);
                    if (isStart) {
                        startDate = selectedDate;
                        if (endDate.isBefore(startDate)) endDate = startDate;
                    } else {
                        endDate = selectedDate;
                    }
                    updateDateTimeButtons();
                },
                currentDate.getYear(), currentDate.getMonthValue() - 1, currentDate.getDayOfMonth()
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
                        }
                    } else {
                        endTime = selectedTime;
                    }
                    updateDateTimeButtons();
                },
                currentTime.getHour(), currentTime.getMinute(), true
        );
        timePickerDialog.show();
    }

    private void saveEvent() {
        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();

        if (title.isEmpty()) {
            titleEditText.setError("Tiêu đề không được để trống");
            return;
        }

        boolean hasNotification = notificationSwitch.isChecked();
        int notificationMinutes = 10;
        if (hasNotification) {
            int selectedRadioId = notificationTimeRadioGroup.getCheckedRadioButtonId();
            RadioButton selectedRadio = findViewById(selectedRadioId);
            if (selectedRadio != null) {
                String selectedText = selectedRadio.getText().toString();
                if (selectedText.equals(getString(R.string.notification_10min))) notificationMinutes = 10;
                else if (selectedText.equals(getString(R.string.notification_30min))) notificationMinutes = 30;
                else if (selectedText.equals(getString(R.string.notification_1hour))) notificationMinutes = 60;
                else if (selectedText.equals(getString(R.string.notification_1day))) notificationMinutes = 24 * 60;
            }
        }

        EventEntity event = new EventEntity();
        event.userId = userId;
        event.title = title;
        event.description = description;
        event.startDate = startDate.format(dbDateFormatter);
        event.startTime = startTime.format(timeFormatter);
        event.endDate = endDate.format(dbDateFormatter);
        event.endTime = endTime.format(timeFormatter);
        event.hasNotification = hasNotification;
        event.reminderOffset = notificationMinutes;

        new SaveEventTask().execute(event);
    }

    private class SaveEventTask extends AsyncTask<EventEntity, Void, Void> {
        @Override
        protected Void doInBackground(EventEntity... events) {
            eventDAO.insert(events[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Toast.makeText(AddReminderActivity.this, "Sự kiện đã được thêm", Toast.LENGTH_SHORT).show();
            Intent resultIntent = new Intent();
            setResult(RESULT_OK, resultIntent);
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}