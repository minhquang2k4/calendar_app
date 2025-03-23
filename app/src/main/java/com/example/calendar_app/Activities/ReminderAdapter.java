package com.example.calendar_app.Activities;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calendar_app.R;


import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {

    private final List<Reminder> reminders;
    private final OnReminderClickListener listener;

    public ReminderAdapter(List<Reminder> reminders, OnReminderClickListener listener) {
        this.reminders = reminders;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.reminder_item, parent, false);
        return new ReminderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        Reminder reminder = reminders.get(position);
        holder.reminderTitle.setText(reminder.getTitle());
        holder.reminderDescription.setText(reminder.getDescription());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onReminderClick(reminder);
            }
        });
    }

    @Override
    public int getItemCount() {
        return reminders.size();
    }

    public interface OnReminderClickListener {
        void onReminderClick(Reminder reminder);
    }

    static class ReminderViewHolder extends RecyclerView.ViewHolder {
        TextView reminderTitle, reminderDescription, reminderTime;

        public ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            reminderTitle = itemView.findViewById(R.id.reminderTitle);
            reminderDescription = itemView.findViewById(R.id.reminderDescription);
            reminderTime = itemView.findViewById(R.id.reminderTime);
        }
    }
}