package com.example.calendar_app;

import android.content.Context;

import com.example.calendar_app.DAO.EventDAO;
import com.example.calendar_app.DAO.UserDAO;
import com.example.calendar_app.Entities.EventEntity;
import com.example.calendar_app.Entities.UserEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CalendarRepository {
    private static CalendarRepository instance;

    public static CalendarRepository getInstance(final Context context) {
        if (instance == null) {
            instance = new CalendarRepository(context.getApplicationContext());
        }
        return instance;
    }

    private final UserDAO userDAO;
    private final EventDAO eventDAO;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public CalendarRepository(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        userDAO = db.userDao();
        eventDAO = db.eventDao();
    }

    public void addUser(UserEntity user) {
        executorService.execute(() -> userDAO.insert(user));
    }

    public void addEvent(EventEntity event) {
        executorService.execute(() -> eventDAO.insert(event));
    }
}
