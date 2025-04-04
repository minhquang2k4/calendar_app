package com.example.calendar_app.DAO;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.calendar_app.Entities.EventEntity;

import java.util.List;

@Dao
public interface EventDAO {
    @Insert
    Long insert(EventEntity event);

    @Update
    void update(EventEntity event);

    @Delete
    void delete(EventEntity event);

    @Query("SELECT * FROM events WHERE id = :id")
    EventEntity getEventById(int id);

    @Query("SELECT * FROM events WHERE user_id = :userId ORDER BY start_date ASC, start_time ASC")
    List<EventEntity> getEventsByUserId(int userId);

    @Query("SELECT * FROM events WHERE user_id = :userId AND start_date = :date ORDER BY start_time ASC")
    List<EventEntity> getEventsByUserAndDate(int userId, String date);
}