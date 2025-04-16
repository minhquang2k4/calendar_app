package com.example.calendar_app.Entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.UUID;

@Entity(tableName = "events")
public class EventEntity {
    @PrimaryKey
    @NonNull
    public String id = UUID.randomUUID().toString();

    @ColumnInfo(name = "user_id")
    public String userId;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "description")
    public String description;

    @ColumnInfo(name = "start_time")
    public String startTime;

    @ColumnInfo(name = "end_time")
    public String endTime;

    @ColumnInfo(name = "start_date")
    public String startDate;

    @ColumnInfo(name = "end_date")
    public String endDate;

    //    thoi gian nhac nho truoc khi den start time
    @ColumnInfo(name = "reminder_offset")
    public int reminderOffset;
    
    public boolean hasNotification;
}