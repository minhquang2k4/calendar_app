package com.example.calendar_app.Activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calendar_app.AppDatabase;
import com.example.calendar_app.Entities.EventEntity;
import com.example.calendar_app.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CalendarActivity extends AppCompatActivity implements ReminderAdapter.OnReminderClickListener {

    private CalendarView calendarView;
    private RecyclerView reminderRecyclerView;
    private TextView selectedDateTV;
    private LocalDate selectedDate;
    private AppDatabase db;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        db = AppDatabase.getDatabase(this);
        currentUserId = getIntent().getStringExtra("USER_ID");
        if (currentUserId == null) {
            Toast.makeText(this, "Không tìm thấy người dùng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Lịch");

        calendarView = findViewById(R.id.calendarView);
        selectedDateTV = findViewById(R.id.selectedDateTV);
        reminderRecyclerView = findViewById(R.id.reminderRecyclerView);
        FloatingActionButton addReminderFAB = findViewById(R.id.addReminderFAB);

        selectedDate = LocalDate.now();

        setupCalendarView();
        updateReminderList();

        addReminderFAB.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddReminderActivity.class);
            intent.putExtra("USER_ID", currentUserId);
            intent.putExtra("SELECTED_DATE", selectedDate.toString());
            startActivityForResult(intent, 1);
        });
    }

    private void setupCalendarView() {
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = LocalDate.of(year, month + 1, dayOfMonth);
            updateReminderList();
        });
    }

    private void updateReminderList() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
        selectedDateTV.setText("Nhắc nhở cho " + selectedDate.format(formatter));
        new LoadEventsTask().execute();
    }

    @Override
    public void onReminderClick(Reminder reminder) {
        Intent intent = new Intent(this, ReminderDetailActivity.class);
        intent.putExtra("EVENT_ID", reminder.getId());
        intent.putExtra("USER_ID", currentUserId);
        startActivityForResult(intent, 2);
    }

    private class LoadEventsTask extends AsyncTask<Void, Void, List<Reminder>> {
        @Override
        protected List<Reminder> doInBackground(Void... voids) {
            List<EventEntity> events = db.eventDao().getEventsByUserId(currentUserId);
            List<Reminder> reminders = new ArrayList<>();
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

            for (EventEntity event : events) {
                LocalDate startDate = LocalDate.parse(event.startDate, dateFormatter);
                if (startDate.equals(selectedDate)) {
                    Reminder reminder = new Reminder(
                            event.id,
                            event.title,
                            event.description,
                            startDate,
                            LocalTime.parse(event.startTime, timeFormatter),
                            LocalDate.parse(event.endDate, dateFormatter),
                            LocalTime.parse(event.endTime, timeFormatter),
                            event.hasNotification,
                            event.reminderOffset
                    );
                    reminders.add(reminder);
                }
            }
            return reminders;
        }

        @Override
        protected void onPostExecute(List<Reminder> reminders) {
            ReminderAdapter reminderAdapter = new ReminderAdapter(reminders, CalendarActivity.this);
            reminderRecyclerView.setLayoutManager(new LinearLayoutManager(CalendarActivity.this));
            reminderRecyclerView.setAdapter(reminderAdapter);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            updateReminderList();
            if (requestCode == 1 && data != null) { // Sau khi thêm sự kiện
                String eventId = data.getStringExtra("EVENT_ID");
                if (eventId != null) {
                    Intent detailIntent = new Intent(this, ReminderDetailActivity.class);
                    detailIntent.putExtra("EVENT_ID", eventId);
                    detailIntent.putExtra("USER_ID", currentUserId);
                    startActivityForResult(detailIntent, 2);
                }
            }
        }
    }
}