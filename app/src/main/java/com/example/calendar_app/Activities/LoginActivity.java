package com.example.calendar_app.Activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.calendar_app.AppDatabase;
import com.example.calendar_app.DAO.FirebaseEventDAO;
import com.example.calendar_app.DAO.FirebaseUserDAO;
import com.example.calendar_app.DAO.UserDAO;
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
    private FirebaseUserDAO userDAO;
    private FirebaseEventDAO eventDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = AppDatabase.getDatabase(this);
        UserDAO roomUserDAO = db.userDao();
        userDAO = new FirebaseUserDAO(roomUserDAO);
        eventDAO = new FirebaseEventDAO(db.eventDao());

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
        syncAndLogin(phone, password);
    }

    private void syncAndLogin(String phone, String password) {
        userDAO.syncFromFirebase(phone).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                UserEntity user = task.getResult();
                if (user != null) {
                    Log.d(TAG, "User synced from Firestore: " + user.getId());
                    eventDAO.syncEventsFromFirebase(user.getId()).addOnCompleteListener(eventTask -> {
                        if (eventTask.isSuccessful()) {
                            Log.d(TAG, "Events synced from Firestore for user: " + user.getId());
                            checkLogin(phone, password);
                        } else {
                            Log.e(TAG, "Failed to sync events: " + eventTask.getException());
                            Toast.makeText(this, "Không thể đồng bộ sự kiện: " + eventTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                            checkLogin(phone, password);
                        }
                    });
                } else {
                    Log.d(TAG, "No user found in Firestore for phone: " + phone);
                    checkLogin(phone, password);
                }
            } else {
                Log.e(TAG, "Failed to sync user: " + task.getException());
                Toast.makeText(this, "Không thể đồng bộ người dùng: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                checkLogin(phone, password);
            }
        });
    }

    private void checkLogin(String phone, String password) {
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
                UserEntity user = userDAO.getUserByPhoneAndPassword(phone, password);
                Log.d(TAG, "User from DB: " + (user != null ? user.getPhone() : "null"));
                return user;
            } catch (Exception e) {
                Log.e(TAG, "Error querying database: " + e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(UserEntity user) {
            if (user != null) {
                Log.d(TAG, "Login successful for user: " + user.getPhone() + ", ID: " + user.getId());
                Toast.makeText(LoginActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, CalendarActivity.class);
                intent.putExtra("USER_ID", user.getId());
                startActivity(intent);
                finish();
            } else {
                Log.d(TAG, "Login failed: Invalid phone or password");
                Toast.makeText(LoginActivity.this, "Số điện thoại hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show();
            }
        }
    }
}