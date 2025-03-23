package com.example.calendar_app.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calendar_app.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CalendarActivity extends AppCompatActivity implements ReminderAdapter.OnReminderClickListener {

    private CalendarView calendarView;
    private RecyclerView reminderRecyclerView;
    private TextView selectedDateTV;
    private LocalDate selectedDate;

    // Temporary data storage - in a real app, this would come from a database
    private Map<LocalDate, List<Reminder>> reminderMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        // Initialize UI components
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        calendarView = findViewById(R.id.calendarView);
        selectedDateTV = findViewById(R.id.selectedDateTV);
        reminderRecyclerView = findViewById(R.id.reminderRecyclerView);
        FloatingActionButton addReminderFAB = findViewById(R.id.addReminderFAB);

        // Initialize date
        selectedDate = LocalDate.now();

        // Add some sample data

        // Set up calendar
        setupCalendarView();

        // Set up reminder list
        updateReminderList();

        // Set up add reminder button
        addReminderFAB.setOnClickListener(v -> addNewReminder());
    }

    private void setupCalendarView() {
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            // Convert selected date to LocalDate
            selectedDate = LocalDate.of(year, month + 1, dayOfMonth);
            updateReminderList();
        });
    }

    private void updateReminderList() {
        // Update selected date text
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
        selectedDateTV.setText("Reminders for " + selectedDate.format(formatter));

        // Get reminders for selected date
        List<Reminder> reminders = reminderMap.getOrDefault(selectedDate, new ArrayList<>());

        // Set up reminder adapter
        ReminderAdapter reminderAdapter = new ReminderAdapter(reminders, this);

        // Set up RecyclerView
        reminderRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        reminderRecyclerView.setAdapter(reminderAdapter);
    }

    private void addNewReminder() {
        // In a real app, this would open a dialog or activity to add a new reminder
        // For this example, we'll just add a dummy reminder
        String id = UUID.randomUUID().toString();
        String title = "New Reminder";
        String description = "Reminder description";
        LocalTime time = LocalTime.now();

        Reminder newReminder = new Reminder(id, title, description, selectedDate, time, selectedDate, time, true, 10);

        // Add to map
        List<Reminder> reminders = reminderMap.getOrDefault(selectedDate, new ArrayList<>());
        reminders.add(newReminder);
        reminderMap.put(selectedDate, reminders);

        // Update UI
        updateReminderList();

        Toast.makeText(this, "New reminder added", Toast.LENGTH_SHORT).show();
    }



    @Override
    public void onReminderClick(Reminder reminder) {
        // Create intent to open ReminderDetailActivity
        Intent intent = new Intent(this, ReminderDetailActivity.class);

        // Pass reminder data to the detail activity
        intent.putExtra("REMINDER_ID", reminder.getId());
        intent.putExtra("REMINDER_TITLE", reminder.getTitle());
        intent.putExtra("REMINDER_DESCRIPTION", reminder.getDescription());
        intent.putExtra("REMINDER_START_DATE", reminder.getStartDate().toString());
        intent.putExtra("REMINDER_START_TIME", reminder.getStartTime().toString());
        intent.putExtra("REMINDER_END_DATE", reminder.getEndDate().toString());
        intent.putExtra("REMINDER_END_TIME", reminder.getEndTime().toString());
        intent.putExtra("REMINDER_HAS_NOTIFICATION", reminder.isHasNotification());
        intent.putExtra("REMINDER_NOTIFICATION_MINUTES", reminder.getNotificationMinutes());

        // Start the activity
        startActivity(intent);
    }
}