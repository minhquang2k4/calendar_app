package com.example.calendar_app.DAO;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Update;

import com.example.calendar_app.Entities.EventEntity;

@Dao
public interface EventDAO {
    @Insert
    void insert(EventEntity event);

    @Update
    void update(EventEntity event);
}
