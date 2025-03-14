package com.example.calendar_app.UI;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.calendar_app.AppDatabase;
import com.example.calendar_app.CalendarRepository;
import com.example.calendar_app.Entities.UserEntity;
import com.example.calendar_app.R;

public class MainActivity extends AppCompatActivity {

    CalendarRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        repository = CalendarRepository.getInstance(this);

        UserEntity user = new UserEntity();
        user.phone = "quang";
        user.password = "123456";


        Log.d("calendar_log", "before add user");
        repository.addUser(user);
        Log.d("calendar_log", "after add user");

    }
}