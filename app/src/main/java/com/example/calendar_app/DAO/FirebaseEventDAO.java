package com.example.calendar_app.DAO;

import android.util.Log;
import com.example.calendar_app.Entities.EventEntity;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FirebaseEventDAO implements EventDAO {
    private final EventDAO originalDAO;
    private final FirebaseFirestore firestore;
    private final ExecutorService executorService;

    public FirebaseEventDAO(EventDAO originalDAO) {
        this.originalDAO = originalDAO;
        this.executorService = Executors.newSingleThreadExecutor();
        try {
            this.firestore = FirebaseFirestore.getInstance();
            Log.d("FirebaseEventDAO", "Firestore initialized successfully");
        } catch (Exception e) {
            Log.e("FirebaseEventDAO", "Failed to initialize Firestore", e);
            throw new RuntimeException("Failed to initialize Firestore", e);
        }
    }

    @Override
    public void insert(EventEntity event) {
        Log.d("FirebaseEventDAO", "Insert method called for event: " + event.id);
        originalDAO.insert(event);
        syncToFirebase(event);
    }

    @Override
    public void update(EventEntity event) {
        Log.d("FirebaseEventDAO", "Update method called for event: " + event.id);
        originalDAO.update(event);
        syncToFirebase(event);
    }

    @Override
    public void delete(EventEntity event) {
        Log.d("FirebaseEventDAO", "Delete method called for event: " + event.id);
        originalDAO.delete(event);
        deleteFromFirebase(event);
    }

    @Override
    public EventEntity getEventById(String id) {
        Log.d("FirebaseEventDAO", "GetEventById called for id: " + id);
        return originalDAO.getEventById(id);
    }

    @Override
    public List<EventEntity> getEventsByUserId(String userId) {
        Log.d("FirebaseEventDAO", "GetEventsByUserId called for userId: " + userId);
        return originalDAO.getEventsByUserId(userId);
    }

    @Override
    public List<EventEntity> getEventsByUserAndDate(String userId, String date) {
        Log.d("FirebaseEventDAO", "GetEventsByUserAndDate called for userId: " + userId + ", date: " + date);
        return originalDAO.getEventsByUserAndDate(userId, date);
    }

    @Override
    public List<EventEntity> getUpcomingEventsWithNotifications(String userId, String date) {
        Log.d("FirebaseEventDAO", "getUpcomingEventsWithNotifications called for userId: " + userId + ", date: " + date);
        return originalDAO.getUpcomingEventsWithNotifications(userId, date);
    }

    public Task<Void> syncEventsFromFirebase(String userId) {
        Log.d("FirebaseEventDAO", "syncEventsFromFirebase called for userId: " + userId);
        TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();

        firestore.collection("users")
                .document(userId)
                .collection("events")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot == null || querySnapshot.isEmpty()) {
                        Log.d("FirebaseEventDAO", "No events found in Firestore for userId: " + userId);
                        taskCompletionSource.setResult(null);
                        return;
                    }

                    List<EventEntity> eventsToSync = new ArrayList<>();

                    // Chuyển đổi dữ liệu từ Firestore một cách thủ công để tránh lỗi
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        try {
                            EventEntity event = new EventEntity();

                            // Lấy ID từ document ID
                            String docId = document.getId();
                            if (document.contains("id")) {
                                event.id = document.getString("id");
                            } else {
                                // Nếu không có trường id, sử dụng document ID
                                event.id = docId;
                            }

                            // Đọc các trường khác
                            event.userId = userId;

                            if (document.contains("title"))
                                event.title = document.getString("title");
                            if (document.contains("description"))
                                event.description = document.getString("description");
                            if (document.contains("startDate"))
                                event.startDate = document.getString("startDate");
                            if (document.contains("startTime"))
                                event.startTime = document.getString("startTime");
                            if (document.contains("endDate"))
                                event.endDate = document.getString("endDate");
                            if (document.contains("endTime"))
                                event.endTime = document.getString("endTime");
                            if (document.contains("hasNotification"))
                                event.hasNotification = Boolean.TRUE.equals(document.getBoolean("hasNotification"));
                            if (document.contains("reminderOffset")) {
                                Object offset = document.get("reminderOffset");
                                if (offset instanceof Number) {
                                    event.reminderOffset = ((Number) offset).intValue();
                                }
                            }

                            eventsToSync.add(event);
                            Log.d("FirebaseEventDAO", "Event mapped from Firestore: " + event.id);
                        } catch (Exception e) {
                            Log.e("FirebaseEventDAO", "Error mapping document to event: " + e.getMessage());
                        }
                    }

                    // Xử lý các sự kiện trong background thread
                    executorService.execute(() -> {
                        try {
                            for (EventEntity event : eventsToSync) {
                                Log.d("FirebaseEventDAO", "Processing event from Firestore: " + event.id);
                                EventEntity existingEvent = originalDAO.getEventById(event.id);
                                if (existingEvent != null) {
                                    originalDAO.update(event);
                                    Log.d("FirebaseEventDAO", "Event updated in Room: " + event.id);
                                } else {
                                    originalDAO.insert(event);
                                    Log.d("FirebaseEventDAO", "Event inserted into Room: " + event.id);
                                }
                            }
                            taskCompletionSource.setResult(null);
                        } catch (Exception e) {
                            Log.e("FirebaseEventDAO", "Error syncing events to Room: " + e.getMessage());
                            taskCompletionSource.setException(e);
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseEventDAO", "Failed to sync events from Firestore: " + e);
                    taskCompletionSource.setException(e);
                });

        return taskCompletionSource.getTask();
    }

    private void syncToFirebase(EventEntity event) {
        Log.d("FirebaseEventDAO", "syncToFirebase called for event: " + event.id);
        firestore.collection("users")
                .document(event.userId)
                .collection("events")
                .document(event.id)
                .set(event)
                .addOnSuccessListener(aVoid -> Log.d("FirebaseEventDAO", "Event synced successfully: " + event.id))
                .addOnFailureListener(e -> Log.e("FirebaseEventDAO", "Failed to sync event: " + event.id, e));
    }

    private void deleteFromFirebase(EventEntity event) {
        Log.d("FirebaseEventDAO", "deleteFromFirebase called for event: " + event.id);
        firestore.collection("users")
                .document(event.userId)
                .collection("events")
                .document(event.id)
                .delete()
                .addOnSuccessListener(aVoid -> Log.d("FirebaseEventDAO", "Event deleted from Firebase: " + event.id))
                .addOnFailureListener(e -> Log.e("FirebaseEventDAO", "Failed to delete event from Firebase: " + event.id, e));
    }
}