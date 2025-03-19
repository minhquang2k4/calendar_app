package com.example.calendar_app.DAO;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.calendar_app.Entities.EventEntity;

@Dao
public interface EventDAO {
    @Insert
    void insert(EventEntity event);

    @Query("SELECT * FROM events WHERE id = :id")
    EventEntity getEventById(int id);

    @Update
    void update(EventEntity event);

    @Delete
    void delete(EventEntity event);
}
