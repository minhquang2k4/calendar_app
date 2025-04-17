package com.example.calendar_app.Activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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
    private AppDatabase db;
    private FirebaseEventDAO eventDAO;
    private EventEntity event;
    private String eventId, userId;
    private LocalDate startDate, endDate;
    private LocalTime startTime, endTime;

    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy");
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private DateTimeFormatter fullFormatter = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy 'at' HH:mm");
    private DateTimeFormatter dbDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_details);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        db = AppDatabase.getDatabase(this);
        EventDAO roomEventDAO = db.eventDao();
        eventDAO = new FirebaseEventDAO(roomEventDAO);
        eventId = getIntent().getStringExtra("EVENT_ID");
        Log.d("Notisss", "id 2 " + eventId);
        userId = getIntent().getStringExtra("USER_ID");

        initializeViews();
        loadEventData();
        setupListeners();

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

    private void loadEventData() {
        new LoadEventTask().execute(eventId);
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
        titleTextView.setText(event.title);
        descriptionTextView.setText(event.description);
        startTimeTextView.setText(LocalDate.parse(event.startDate, dbDateFormatter)
                .atTime(LocalTime.parse(event.startTime, timeFormatter)).format(fullFormatter));
        endTimeTextView.setText(LocalDate.parse(event.endDate, dbDateFormatter)
                .atTime(LocalTime.parse(event.endTime, timeFormatter)).format(fullFormatter));
        notificationTextView.setText(formatNotificationTime(event.reminderOffset) + " trước");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDateTime = LocalDate.parse(event.startDate, dbDateFormatter)
                .atTime(LocalTime.parse(event.startTime, timeFormatter));
        LocalDateTime endDateTime = LocalDate.parse(event.endDate, dbDateFormatter)
                .atTime(LocalTime.parse(event.endTime, timeFormatter));

        if (now.isAfter(endDateTime)) {
            statusTextView.setText("Đã hoàn thành");
            statusTextView.setBackgroundResource(R.drawable.status_background);
        } else if (now.isAfter(startDateTime)) {
            statusTextView.setText("Đang diễn ra");
            statusTextView.setBackgroundResource(R.drawable.status_background);
            statusTextView.setTextColor(getResources().getColor(R.color.white));
        } else {
            statusTextView.setText("Sắp tới");
            statusTextView.setBackgroundResource(R.drawable.status_background);
            statusTextView.setTextColor(getResources().getColor(R.color.white));
        }

        titleEditText.setText(event.title);
        descriptionEditText.setText(event.description);
        startDate = LocalDate.parse(event.startDate, dbDateFormatter);
        startTime = LocalTime.parse(event.startTime, timeFormatter);
        endDate = LocalDate.parse(event.endDate, dbDateFormatter);
        endTime = LocalTime.parse(event.endTime, timeFormatter);
        updateDateTimeButtons();
        notificationSwitch.setChecked(event.hasNotification);

        int minutes = event.reminderOffset;
        if (minutes == 10) notificationTimeRadioGroup.check(R.id.radio10min);
        else if (minutes == 30) notificationTimeRadioGroup.check(R.id.radio30min);
        else if (minutes == 60) notificationTimeRadioGroup.check(R.id.radio1hour);
        else if (minutes == 24 * 60) notificationTimeRadioGroup.check(R.id.radio1day);
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

    private void saveChanges() {
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
            if (selectedRadioId == R.id.radio10min) notificationMinutes = 10;
            else if (selectedRadioId == R.id.radio30min) notificationMinutes = 30;
            else if (selectedRadioId == R.id.radio1hour) notificationMinutes = 60;
            else if (selectedRadioId == R.id.radio1day) notificationMinutes = 24 * 60;
        }

        event.title = title;
        event.description = description;
        event.startDate = startDate.format(dbDateFormatter);
        event.startTime = startTime.format(timeFormatter);
        event.endDate = endDate.format(dbDateFormatter);
        event.endTime = endTime.format(timeFormatter);
        event.hasNotification = hasNotification;
        event.reminderOffset = notificationMinutes;

        new UpdateEventTask().execute(event);
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Xóa sự kiện")
                .setMessage("Bạn có chắc chắn muốn xóa sự kiện này?")
                .setPositiveButton("Xóa", (dialog, which) -> new DeleteEventTask().execute(event))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private String formatNotificationTime(int minutes) {
        if (minutes < 60) return minutes + " phút";
        else if (minutes < 24 * 60) return (minutes / 60) + " giờ";
        else return (minutes / (24 * 60)) + " ngày";
    }

    private class LoadEventTask extends AsyncTask<String, Void, EventEntity> {
        @Override
        protected EventEntity doInBackground(String... ids) {
            return eventDAO.getEventById(ids[0]);
        }

        @Override
        protected void onPostExecute(EventEntity loadedEvent) {
            if (loadedEvent != null) {
                event = loadedEvent;
                updateUI();
            } else {
                Toast.makeText(ReminderDetailActivity.this, "Không tìm thấy sự kiện", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private class UpdateEventTask extends AsyncTask<EventEntity, Void, Void> {
        @Override
        protected Void doInBackground(EventEntity... events) {
            db.eventDao().update(events[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Toast.makeText(ReminderDetailActivity.this, "Đã lưu thay đổi", Toast.LENGTH_SHORT).show();
            updateUI();
            switchToViewMode();
            setResult(RESULT_OK);
        }
    }

    private class DeleteEventTask extends AsyncTask<EventEntity, Void, Void> {
        @Override
        protected Void doInBackground(EventEntity... events) {
            eventDAO.delete(events[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Toast.makeText(ReminderDetailActivity.this, "Đã xóa sự kiện", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
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