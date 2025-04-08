package com.example.calendar_app.Activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.calendar_app.AppDatabase;
import com.example.calendar_app.DAO.FirebaseUserDAO;
import com.example.calendar_app.DAO.UserDAO;
import com.example.calendar_app.Entities.UserEntity;
import com.example.calendar_app.R;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etPhone, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvLogin;
    private AppDatabase db;
    private FirebaseUserDAO userDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = AppDatabase.getDatabase(this);
        UserDAO roomUserDAO = db.userDao();
        userDAO = new FirebaseUserDAO(roomUserDAO);

        initViews();
        setupListeners();
    }

    private void initViews() {
        etUsername = findViewById(R.id.etUsername);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> attemptRegister());

        tvLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void attemptRegister() {
        String name = etUsername.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (name.isEmpty()) {
            etUsername.setError("Name is required");
            return;
        }

        if (phone.isEmpty()) {
            etPhone.setError("Phone number is required");
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords don't match");
            return;
        }

        new RegisterTask(name, phone, password).execute();
    }

    private class RegisterTask extends AsyncTask<Void, Void, UserEntity> {
        private final String name;
        private final String phone;
        private final String password;

        RegisterTask(String name, String phone, String password) {
            this.name = name;
            this.phone = phone;
            this.password = password;
        }

        @Override
        protected UserEntity doInBackground(Void... voids) {
            // Check if phone already exists
            UserEntity existingUser = userDAO.getUserByPhone(phone);
            if (existingUser != null) {
                return null;
            }

            // Create new user
            UserEntity newUser = new UserEntity();
            newUser.setName(name);
            newUser.setPhone(phone);
            newUser.setPassword(password);

            userDAO.insert(newUser); // Lưu vào Room (đã chạy trên luồng nền)
            return userDAO.getUserByPhone(phone); // Lấy lại user đã được gán ID
        }

        @Override
        protected void onPostExecute(UserEntity user) {
            if (user == null) {
                Toast.makeText(RegisterActivity.this, "Phone number already registered", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(RegisterActivity.this, "Registration successful in Room", Toast.LENGTH_SHORT).show();
            // Chỉ đồng bộ lên Firestore, không gọi lại insert
            userDAO.syncToFirebase(user).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(RegisterActivity.this, "Đồng bộ lên Firebase thành công", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(RegisterActivity.this, "Đồng bộ lên Firebase thất bại: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            });
        }
    }
}