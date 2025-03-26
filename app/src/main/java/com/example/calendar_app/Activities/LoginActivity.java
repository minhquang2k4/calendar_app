package com.example.calendar_app.Activities;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.calendar_app.AppDatabase;
import com.example.calendar_app.Entities.UserEntity;
import com.example.calendar_app.R;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etPassword;
    private CheckBox cbRemember;
    private Button btnLogin;
    private TextView tvRegister, tvForgotPassword;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        tvForgotPassword.setOnClickListener(v -> {
            // Handle forgot password logic
            Toast.makeText(this, "Forgot password clicked", Toast.LENGTH_SHORT).show();
        });
    }

    private void attemptLogin() {
        String phone = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (phone.isEmpty()) {
            etUsername.setError("Phone number is required");
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            return;
        }

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
            return db.userDao().getUserByPhoneAndPassword(phone, password);
        }

        @Override
        protected void onPostExecute(UserEntity user) {
            if (user != null) {
                // Đăng nhập thành công
                Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();

                // Save login state if remember me is checked
                if (cbRemember.isChecked()) {
                    // You can save user token or id in SharedPreferences here
                }

                // Proceed to main activity
                Intent intent = new Intent(LoginActivity.this, CalendarActivity.class);
                startActivity(intent);
                finish();
            } else {
                // Login failed
                Toast.makeText(LoginActivity.this, "Invalid phone or password", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
