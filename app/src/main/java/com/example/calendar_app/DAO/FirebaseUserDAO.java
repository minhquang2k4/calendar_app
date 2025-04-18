package com.example.calendar_app.DAO;

import android.util.Log;
import com.example.calendar_app.Entities.UserEntity;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FirebaseUserDAO implements UserDAO {
    private final UserDAO originalDAO;
    private final FirebaseFirestore firestore;
    private final ExecutorService executorService;

    public FirebaseUserDAO(UserDAO originalDAO) {
        this.originalDAO = originalDAO;
        this.executorService = Executors.newSingleThreadExecutor();
        try {
            this.firestore = FirebaseFirestore.getInstance();
            Log.d("FirebaseUserDAO", "Firestore initialized successfully");
        } catch (Exception e) {
            Log.e("FirebaseUserDAO", "Failed to initialize Firestore", e);
            throw new RuntimeException("Failed to initialize Firestore", e);
        }
    }

    @Override
    public void insert(UserEntity user) {
        Log.d("FirebaseUserDAO", "Insert method called for user: " + user.id);
        originalDAO.insert(user);
        executorService.execute(() -> {
            UserEntity insertedUser = originalDAO.getUserByPhone(user.phone);
            if (insertedUser != null) {
                syncToFirebase(insertedUser);
            } else {
                Log.e("FirebaseUserDAO", "Failed to retrieve inserted user for sync: " + user.phone);
            }
        });
    }

    public Task<Void> insertWithSync(UserEntity user) {
        Log.d("FirebaseUserDAO", "InsertWithSync method called for user: " + user.id);
        TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();

        executorService.execute(() -> {
            try {
                originalDAO.insert(user);

                syncToFirebase(user)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                taskCompletionSource.setResult(null);
                            } else {
                                taskCompletionSource.setException(task.getException());
                            }
                        });
            } catch (Exception e) {
                Log.e("FirebaseUserDAO", "Error inserting user: " + e.getMessage());
                taskCompletionSource.setException(e);
            }
        });

        return taskCompletionSource.getTask();
    }

    @Override
    public void update(UserEntity user) {
        Log.d("FirebaseUserDAO", "Update method called for user: " + user.id);
        originalDAO.update(user);
        syncToFirebase(user);
    }

    @Override
    public UserEntity getUserById(String id) {
        Log.d("FirebaseUserDAO", "GetUserById called for id: " + id);
        return originalDAO.getUserById(id);
    }

    @Override
    public UserEntity getUserByPhoneAndPassword(String phone, String password) {
        Log.d("FirebaseUserDAO", "GetUserByPhoneAndPassword called for phone: " + phone);
        return originalDAO.getUserByPhoneAndPassword(phone, password);
    }

    @Override
    public UserEntity getUserByPhone(String phone) {
        Log.d("FirebaseUserDAO", "GetUserByPhone called for phone: " + phone);
        return originalDAO.getUserByPhone(phone);
    }

    public Task<Void> syncToFirebase(UserEntity user) {
        Log.d("FirebaseUserDAO", "syncToFirebase called for user: " + user.id);
        return firestore.collection("users")
                .document(user.id)
                .set(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("FirebaseUserDAO", "User synced successfully: " + user.id);
                    } else {
                        Log.e("FirebaseUserDAO", "Failed to sync user: " + user.id, task.getException());
                    }
                });
    }

    public Task<UserEntity> syncFromFirebase(String phone) {
        Log.d("FirebaseUserDAO", "syncFromFirebase called for phone: " + phone);
        TaskCompletionSource<UserEntity> taskCompletionSource = new TaskCompletionSource<>();

        firestore.collection("users")
                .whereEqualTo("phone", phone)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        UserEntity user = querySnapshot.getDocuments().get(0).toObject(UserEntity.class);
                        if (user != null) {
                            Log.d("FirebaseUserDAO", "User found in Firestore: " + user.id);

                            // Process Room operations in background thread
                            executorService.execute(() -> {
                                try {
                                    UserEntity existingUser = originalDAO.getUserByPhone(phone);
                                    if (existingUser != null) {
                                        // Giữ nguyên id trong Room nếu đã tồn tại
                                        // Vì đã đổi sang UUID, nên không cần gán lại id nữa
                                        originalDAO.update(user);
                                        Log.d("FirebaseUserDAO", "User updated in Room: " + user.id);
                                    } else {
                                        originalDAO.insert(user);
                                        Log.d("FirebaseUserDAO", "User inserted into Room: " + user.id);
                                    }
                                    taskCompletionSource.setResult(user);
                                } catch (Exception e) {
                                    Log.e("FirebaseUserDAO", "Error updating/inserting user in Room: " + e.getMessage());
                                    taskCompletionSource.setException(e);
                                }
                            });
                        } else {
                            Log.d("FirebaseUserDAO", "Query returned empty user object for phone: " + phone);
                            taskCompletionSource.setResult(null);
                        }
                    } else {
                        Log.d("FirebaseUserDAO", "No user found in Firestore for phone: " + phone);
                        taskCompletionSource.setResult(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseUserDAO", "Failed to sync from Firestore: " + e);
                    taskCompletionSource.setException(e);
                });

        return taskCompletionSource.getTask();
    }
}