package com.example.calendar_app.Activities;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.calendar_app.AppDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

public class test_DB extends AppCompatActivity {

    AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_main);

//          db = AppDatabase.getDatabase(this);
//
//        UserEntity user = new UserEntity();
//        user.phone = "quang";
//        user.password = "123456";
//
//
//       Log.d("calendar_log", "before add user");
//       new Thread(() -> {
//          db.userDao().insert(user);
//       }).start();
//       Log.d("calendar_log", "after add user");



        Log.d("calendar_log", "before get user");
        checkFirestoreConnection();
        Log.d("calendar_log", "after get user");

    }
    private void checkFirestoreConnection() {
        Log.d("calendar_log", "checkFirestoreConnection: ");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Log.d("calendar_log", "checkFirestoreConnection 2: ");

        db.collection("test_connection")  // Collection không tồn tại
                .limit(1) // Hạn chế truy vấn
                .get()
                .addOnSuccessListener(queryDocumentSnapshots ->
                        Log.d("Firestore", "🔥 Kết nối thành công với Firestore!")
                )
                .addOnFailureListener(e ->
                        Log.e("Firestore", "❌ Lỗi kết nối Firestore", e)
                );
    }

}