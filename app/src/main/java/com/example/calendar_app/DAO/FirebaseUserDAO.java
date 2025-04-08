package com.example.calendar_app.DAO;

import android.util.Log;
import com.example.calendar_app.Entities.UserEntity;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class FirebaseUserDAO implements UserDAO {
    private final UserDAO originalDAO;
    private final FirebaseFirestore firestore;

    public FirebaseUserDAO(UserDAO originalDAO) {
        this.originalDAO = originalDAO;
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
        Log.d("FirebaseUserDAO", "Insert method called for user: " + user.getId());
        originalDAO.insert(user);
        UserEntity insertedUser = originalDAO.getUserByPhone(user.getPhone());
        if (insertedUser != null) {
            syncToFirebase(insertedUser);
        } else {
            Log.e("FirebaseUserDAO", "Failed to retrieve inserted user for sync: " + user.getPhone());
        }
    }

    public Task<Void> insertWithSync(UserEntity user) {
        Log.d("FirebaseUserDAO", "InsertWithSync method called for user: " + user.getId());
        return syncToFirebase(user);
    }

    @Override
    public void update(UserEntity user) {
        Log.d("FirebaseUserDAO", "Update method called for user: " + user.getId());
        originalDAO.update(user);
        syncToFirebase(user);
    }

    @Override
    public UserEntity getUserById(int id) {
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
        Log.d("FirebaseUserDAO", "syncToFirebase called for user: " + user.getId());
        return firestore.collection("users")
                .document(String.valueOf(user.getId()))
                .set(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("FirebaseUserDAO", "User synced successfully: " + user.getId());
                    } else {
                        Log.e("FirebaseUserDAO", "Failed to sync user: " + user.getId(), task.getException());
                    }
                });
    }

    public Task<UserEntity> syncFromFirebase(String phone) {
        Log.d("FirebaseUserDAO", "syncFromFirebase called for phone: " + phone);
        return firestore.collection("users")
                .whereEqualTo("phone", phone)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            UserEntity user = querySnapshot.getDocuments().get(0).toObject(UserEntity.class);
                            if (user != null) {
                                Log.d("FirebaseUserDAO", "User found in Firestore: " + user.getId());
                                UserEntity existingUser = originalDAO.getUserByPhone(phone);
                                if (existingUser != null) {
                                    user.setId(existingUser.getId());
                                    originalDAO.update(user);
                                    Log.d("FirebaseUserDAO", "User updated in Room: " + user.getId());
                                } else {
                                    originalDAO.insert(user);
                                    Log.d("FirebaseUserDAO", "User inserted into Room: " + user.getId());
                                }
                                return user;
                            }
                        } else {
                            Log.d("FirebaseUserDAO", "No user found in Firestore for phone: " + phone);
                        }
                    } else {
                        Log.e("FirebaseUserDAO", "Failed to sync from Firestore: " + task.getException());
                    }
                    return null;
                });
    }
}