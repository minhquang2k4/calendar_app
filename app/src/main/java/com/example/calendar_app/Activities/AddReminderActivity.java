package com.example.calendar_app.Activities;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.calendar_app.AppDatabase;
import com.example.calendar_app.DAO.EventDAO;
import com.example.calendar_app.Entities.EventEntity;
import com.example.calendar_app.MyService;
import com.example.calendar_app.R;
import com.example.calendar_app.ReminderReceiver;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class AddReminderActivity extends AppCompatActivity {

    private TextInputEditText titleEditText, descriptionEditText;
    private MaterialButton startDateButton, startTimeButton, endDateButton, endTimeButton, saveButton;
    private SwitchMaterial notificationSwitch;
    private RadioGroup notificationTimeRadioGroup;
    private AppDatabase db;
    private int userId;
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
        userId = getIntent().getIntExtra("USER_ID", -1);
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
    private EventEntity currentEvent;

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

        currentEvent = new EventEntity();


        currentEvent.userId = userId;
        currentEvent.title = title;
        currentEvent.description = description;
        currentEvent.startDate = startDate.format(dbDateFormatter);
        currentEvent.startTime = startTime.format(timeFormatter);
        currentEvent.endDate = endDate.format(dbDateFormatter);
        currentEvent.endTime = endTime.format(timeFormatter);
        currentEvent.hasNotification = hasNotification;
        currentEvent.reminderOffset = notificationMinutes;

        new SaveEventTask().execute(currentEvent);
    }

    private class SaveEventTask extends AsyncTask<EventEntity, Void, Long> {
        private EventEntity eventToInsert;
        @Override
        protected Long doInBackground(EventEntity... events) {
            eventToInsert = events[0];
            long insertedId = db.eventDao().insert(eventToInsert);
            eventToInsert.id = (int) insertedId; // Gán lại ID thực vào object
            return insertedId;
        }

        @Override
        protected void onPostExecute(Long eventId) {

            Toast.makeText(AddReminderActivity.this, "Sự kiện đã được thêm", Toast.LENGTH_SHORT).show();



            Intent resultIntent = new Intent();
            resultIntent.putExtra("EVENT_ID", eventId.intValue());
            setResult(RESULT_OK, resultIntent);
//            Intent intent = new Intent(AddReminderActivity.this, MyService.class);
//            intent.putExtra("title", "Sự kiện: " + currentEvent.title);
//            intent.putExtra("description", currentEvent.description);

//
//            startService(intent);
            finish();
            if (currentEvent.hasNotification) {
                Log.d("Add", "co thong bao" );
                scheduleNotification(currentEvent);
            }

        }
        private void scheduleNotification(EventEntity event) {
            Intent intent = new Intent(AddReminderActivity.this, ReminderReceiver.class);
            intent.putExtra("title", event.title);
            intent.putExtra("description", event.description);
            intent.putExtra("eventId", event.id);

            Log.d("Add", "Đang lên lịch thông báo cho event ID: " + event.id);


            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    AddReminderActivity.this,
                    event.id,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
            );

            LocalDateTime eventDateTime = LocalDateTime.parse(
                    event.startDate + "T" + event.startTime
            );

            Log.d("Add", "Thời gian sự kiện: " + eventDateTime);
            LocalDateTime reminderDateTime = eventDateTime.minusMinutes(event.reminderOffset);
            Log.d("Add", "Thời gian thông báo: " + reminderDateTime);

            long triggerAtMillis = reminderDateTime
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli();


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                if (!alarmManager.canScheduleExactAlarms()) {
                    Toast.makeText(AddReminderActivity.this,
                            "Vui lòng cho phép ứng dụng đặt báo thức chính xác trong cài đặt",
                            Toast.LENGTH_LONG).show();

                    Intent settingsIntent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    startActivity(settingsIntent);
                    return;
                }
            }

            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (alarmManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerAtMillis,
                            pendingIntent
                    );
                } else {
                    alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            triggerAtMillis,
                            pendingIntent
                    );
                    Log.d("Add", "setExact");
                }
            }
        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}