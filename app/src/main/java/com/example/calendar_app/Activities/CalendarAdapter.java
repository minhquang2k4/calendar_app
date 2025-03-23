package com.example.calendar_app.Activities;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calendar_app.R;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder> {

    private final ArrayList<LocalDate> days;
    private final OnItemListener onItemListener;
    private final LocalDate selectedDate;
    private final Context context;
    private final Map<LocalDate, List<Reminder>> reminderMap;

    public CalendarAdapter(Context context, ArrayList<LocalDate> days, LocalDate selectedDate,
                           OnItemListener onItemListener, Map<LocalDate, List<Reminder>> reminderMap) {
        this.context = context;
        this.days = days;
        this.onItemListener = onItemListener;
        this.selectedDate = selectedDate;
        this.reminderMap = reminderMap != null ? reminderMap : new HashMap<>();
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.calendar_day_item, parent, false);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = (int) (parent.getHeight() * 0.166666666); // 1/6 of the parent height
        return new CalendarViewHolder(view, onItemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        LocalDate date = days.get(position);
        if (date == null) {
            holder.dayNumber.setText("");
            holder.dayItemContainer.setBackgroundResource(0);
            holder.itemView.setClickable(false);
        } else {
            holder.dayNumber.setText(String.valueOf(date.getDayOfMonth()));
            holder.itemView.setClickable(true);

            // Check if this day is today
            if (date.equals(LocalDate.now())) {
                holder.dayNumber.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
                holder.dayNumber.setTextSize(16);
                holder.dayNumber.setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                // Check if it's a weekend
                int dayOfWeek = date.getDayOfWeek().getValue();
                if (dayOfWeek == 6 || dayOfWeek == 7) { // Saturday or Sunday
                    holder.dayNumber.setTextColor(ContextCompat.getColor(context, R.color.red));
                } else {
                    holder.dayNumber.setTextColor(ContextCompat.getColor(context, R.color.black));
                }
                holder.dayNumber.setTextSize(14);
                holder.dayNumber.setTypeface(null, android.graphics.Typeface.NORMAL);
            }

            // Check if this day is selected
            if (date.equals(selectedDate)) {
                holder.dayItemContainer.setSelected(true);
                holder.dayNumber.setTextColor(Color.WHITE);
            } else {
                holder.dayItemContainer.setSelected(false);
            }

            // Show reminder indicators
            List<Reminder> reminders = reminderMap.get(date);
            if (reminders != null && !reminders.isEmpty()) {
                int size = reminders.size();
                if (size >= 1) {
                    holder.indicator1.setVisibility(View.VISIBLE);
                } else {
                    holder.indicator1.setVisibility(View.GONE);
                }

                if (size >= 2) {
                    holder.indicator2.setVisibility(View.VISIBLE);
                } else {
                    holder.indicator2.setVisibility(View.GONE);
                }

                if (size >= 3) {
                    holder.indicator3.setVisibility(View.VISIBLE);
                } else {
                    holder.indicator3.setVisibility(View.GONE);
                }

                if (size > 3) {
                    holder.moreIndicator.setVisibility(View.VISIBLE);
                } else {
                    holder.moreIndicator.setVisibility(View.GONE);
                }
            } else {
                holder.indicator1.setVisibility(View.GONE);
                holder.indicator2.setVisibility(View.GONE);
                holder.indicator3.setVisibility(View.GONE);
                holder.moreIndicator.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    public interface OnItemListener {
        void onItemClick(int position, LocalDate date);
    }

    public class CalendarViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView dayNumber;
        public final ConstraintLayout dayItemContainer;
        public final View indicator1, indicator2, indicator3;
        public final TextView moreIndicator;
        private final OnItemListener onItemListener;
        public final LinearLayout reminderIndicators;

        public CalendarViewHolder(@NonNull View itemView, OnItemListener onItemListener) {
            super(itemView);
            dayNumber = itemView.findViewById(R.id.dayNumber);
            dayItemContainer = itemView.findViewById(R.id.dayItemContainer);
            indicator1 = itemView.findViewById(R.id.indicator1);
            indicator2 = itemView.findViewById(R.id.indicator2);
            indicator3 = itemView.findViewById(R.id.indicator3);
            moreIndicator = itemView.findViewById(R.id.moreIndicator);
            reminderIndicators = itemView.findViewById(R.id.reminderIndicators);
            this.onItemListener = onItemListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION && days.get(position) != null) {
                onItemListener.onItemClick(position, days.get(position));
            }
        }
    }
}