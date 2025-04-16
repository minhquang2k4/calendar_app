package com.example.calendar_app.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.calendar_app.App;
import com.example.calendar_app.AppDatabase;
import com.example.calendar_app.Entities.UserEntity;
import com.example.calendar_app.R;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private TextInputEditText etUsername, etPassword;
    private CheckBox cbRemember;
    private Button btnLogin;
    private TextView tvRegister, tvForgotPassword;
    private AppDatabase db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String savedUserId = prefs.getString("USER_ID", null);

        if (savedUserId != null) {
            Intent intent = new Intent(this, CalendarActivity.class);
            intent.putExtra("USER_ID", savedUserId);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_login);
        db = AppDatabase.getDatabase(this);
        initViews();
        setupListeners();
    }

    private void initViews() {
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        cbRemember = findViewById(R.id.cb_remember);
        btnLogin = findViewById(R.id.btn_login);
        tvRegister = findViewById(R.id.tv_register);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> attemptLogin());

        tvRegister.setOnClickListener(v -> {
            Log.d(TAG, "Register button clicked");
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        tvForgotPassword.setOnClickListener(v -> {
            Toast.makeText(this, "Quên mật khẩu được bấm", Toast.LENGTH_SHORT).show();
        });
    }

    private void attemptLogin() {
        String phone = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (phone.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số điện thoại và mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Attempting login with phone: " + phone + ", password: " + password);
        new LoginTask(phone, password).execute();
    }

    private class LoginTask extends AsyncTask<Void, Void, UserEntity> {
        private final String phone;
        private final String password;

        LoginTask(String phone, String password) {
            this.phone = phone;
            this.password = password;
        }

        @Override
        protected UserEntity doInBackground(Void... voids) {
            try {
                UserEntity user = db.userDao().getUserByPhoneAndPassword(phone, password);
                Log.d(TAG, "User from DB: " + (user != null ? user.phone : "null"));
                return user;
            } catch (Exception e) {
                Log.e(TAG, "Error querying database: " + e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(UserEntity user) {
            if (user != null) {
                Log.d(TAG, "Login successful for user: " + user.phone + ", ID: " + user.id);
                Toast.makeText(LoginActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();

                SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                prefs.edit().putString("USER_ID", user.id).apply();

                // Schedule reminder checks now that user is logged in
                App.getInstance().scheduleReminderChecks();

                Intent intent = new Intent(LoginActivity.this, CalendarActivity.class);
                intent.putExtra("USER_ID", user.id);
                startActivity(intent);
                finish();
            } else {
                Log.d(TAG, "Login failed: Invalid phone or password");
                Toast.makeText(LoginActivity.this, "Số điện thoại hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show();
            }
        }
    }
}