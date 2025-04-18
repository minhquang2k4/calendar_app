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
import com.example.calendar_app.DAO.FirebaseEventDAO;
import com.example.calendar_app.DAO.FirebaseUserDAO;
import com.example.calendar_app.DAO.UserDAO;
import com.example.calendar_app.Entities.UserEntity;
import com.example.calendar_app.R;
import com.google.android.material.textfield.TextInputEditText;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private TextInputEditText etUsername, etPassword;
    private CheckBox cbRemember;
    private Button btnLogin;
    private TextView tvRegister, tvForgotPassword;
    private AppDatabase db;
    private FirebaseUserDAO userDAO;
    private FirebaseEventDAO eventDAO;
    private ExecutorService executorService;


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
        UserDAO roomUserDAO = db.userDao();
        userDAO = new FirebaseUserDAO(roomUserDAO);
        eventDAO = new FirebaseEventDAO(db.eventDao());
        executorService = Executors.newSingleThreadExecutor();

        initViews();
        setupListeners();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
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
        btnLogin.setEnabled(false);

        checkLocalLogin(phone, password);
    }

    private void checkLocalLogin(String phone, String password) {
        executorService.execute(() -> {
            try {
                final UserEntity user = db.userDao().getUserByPhoneAndPassword(phone, password);

                runOnUiThread(() -> {
                    if (user != null) {
                        // Local login success
                        syncEventsAndLogin(user); // Sync events immediately after successful local login
                    } else {
                        // Try Firebase sync
                        syncFromFirebaseAndLogin(phone, password);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error checking local login: " + e.getMessage());
                runOnUiThread(() -> {
                    // Try Firebase sync as fallback
                    syncFromFirebaseAndLogin(phone, password);
                });
            }
        });
    }

    private void syncFromFirebaseAndLogin(String phone, String password) {
        userDAO.syncFromFirebase(phone).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                UserEntity user = task.getResult();
                if (user != null) {
                    Log.d(TAG, "User synced from Firestore: " + user.getId());
                    syncEventsAndLogin(user); // Sync events after successful Firebase user sync
                } else {
                    Log.d(TAG, "No user found in Firestore for phone: " + phone);
                    finalCheckLogin(phone, password); // Proceed to final check even if user not found on Firebase (maybe only local exists)
                }
            } else {
                Log.e(TAG, "Failed to sync user: " + task.getException());
                Toast.makeText(this, "Không thể đồng bộ người dùng: " +
                                (task.getException() != null ? task.getException().getMessage() : "Unknown error"),
                        Toast.LENGTH_LONG).show();
                finalCheckLogin(phone, password);
            }
        });
    }

    private void syncEventsAndLogin(UserEntity user) {
        eventDAO.syncEventsFromFirebase(user.getId())
                .addOnCompleteListener(eventTask -> {
                    boolean eventsSynced = eventTask.isSuccessful();
                    runOnUiThread(() -> {
                        if (eventsSynced) {
                            Log.d(TAG, "Events synced from Firestore for user: " + user.getId());
                        } else {
                            Log.e(TAG, "Failed to sync events: " +
                                    (eventTask.getException() != null ? eventTask.getException().getMessage() : "Unknown error"));
                            Toast.makeText(LoginActivity.this, "Lỗi đồng bộ sự kiện: " +
                                            (eventTask.getException() != null ? eventTask.getException().getMessage() : "Unknown error"),
                                    Toast.LENGTH_SHORT).show();
                        }
                        // Proceed to login after attempting to sync events
                        loginSuccessful(user);
                    });
                });
    }

    private void finalCheckLogin(String phone, String password) {
        executorService.execute(() -> {
            try {
                final UserEntity user = db.userDao().getUserByPhoneAndPassword(phone, password);

                runOnUiThread(() -> {
                    btnLogin.setEnabled(true);

                    if (user != null) {
                        syncEventsAndLogin(user); // Sync events if login successful after Firebase check
                    } else {
                        Log.d(TAG, "Login failed: Invalid phone or password");
                        Toast.makeText(LoginActivity.this,
                                "Số điện thoại hoặc mật khẩu không đúng",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error in final login check: " + e.getMessage());
                runOnUiThread(() -> {
                    btnLogin.setEnabled(true);
                    Toast.makeText(LoginActivity.this,
                            "Lỗi đăng nhập: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void loginSuccessful(UserEntity user) {
        Log.d(TAG, "Login successful for user: " + user.getPhone() + ", ID: " + user.getId());
        Toast.makeText(LoginActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();

        // Save user ID to shared preferences
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        prefs.edit().putString("USER_ID", user.getId()).apply();

        // Schedule reminder checks now that user is logged in
        App.getInstance().scheduleReminderChecks();

        Intent intent = new Intent(LoginActivity.this, CalendarActivity.class);
        intent.putExtra("USER_ID", user.getId());
        startActivity(intent);
        finish();
    }
}