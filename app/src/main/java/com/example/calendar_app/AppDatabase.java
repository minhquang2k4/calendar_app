package com.example.calendar_app;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.calendar_app.DAO.EventDAO;
import com.example.calendar_app.DAO.UserDAO;
import com.example.calendar_app.Entities.EventEntity;
import com.example.calendar_app.Entities.UserEntity;

@Database(entities = {UserEntity.class, EventEntity.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase db;

    public abstract UserDAO userDao();
    public abstract EventDAO eventDao();

    public static synchronized AppDatabase getDatabase(final Context context) {
        if (db == null) {
            db = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "calendar")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return db;
    }
}