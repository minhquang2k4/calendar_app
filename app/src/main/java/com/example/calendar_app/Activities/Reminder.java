package com.example.calendar_app.Activities;

import java.time.LocalDate;
import java.time.LocalTime;

public class Reminder {
    private String id;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalTime startTime;
    private LocalDate endDate;
    private LocalTime endTime;
    private boolean hasNotification;
    private int notificationMinutes;

    public Reminder() {
    }

    public Reminder(String id, String title, String description, LocalDate startDate, LocalTime startTime,
                    LocalDate endDate, LocalTime endTime, boolean hasNotification, int notificationMinutes) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.startTime = startTime;
        this.endDate = endDate;
        this.endTime = endTime;
        this.hasNotification = hasNotification;
        this.notificationMinutes = notificationMinutes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public boolean isHasNotification() {
        return hasNotification;
    }

    public void setHasNotification(boolean hasNotification) {
        this.hasNotification = hasNotification;
    }

    public int getNotificationMinutes() {
        return notificationMinutes;
    }

    public void setNotificationMinutes(int notificationMinutes) {
        this.notificationMinutes = notificationMinutes;
    }
}