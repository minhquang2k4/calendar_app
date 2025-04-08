package com.example.calendar_app.DAO;

import android.os.AsyncTask;
import android.util.Log;
import com.example.calendar_app.Entities.EventEntity;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class FirebaseEventDAO implements EventDAO {
    private final EventDAO originalDAO;
    private final FirebaseFirestore firestore;

    public FirebaseEventDAO(EventDAO originalDAO) {
        this.originalDAO = originalDAO;
        try {
            this.firestore = FirebaseFirestore.getInstance();
            Log.d("FirebaseEventDAO", "Firestore initialized successfully");
        } catch (Exception e) {
            Log.e("FirebaseEventDAO", "Failed to initialize Firestore", e);
            throw new RuntimeException("Failed to initialize Firestore", e);
        }
    }

    @Override
    public Long insert(EventEntity event) {
        Log.d("FirebaseEventDAO", "Insert method called for event: " + event.getId());
        Long id = originalDAO.insert(event);
        event.setId(id.intValue());
        syncToFirebase(event);
        return id;
    }

    @Override
    public void update(EventEntity event) {
        Log.d("FirebaseEventDAO", "Update method called for event: " + event.getId());
        originalDAO.update(event);
        syncToFirebase(event);
    }

    @Override
    public void delete(EventEntity event) {
        Log.d("FirebaseEventDAO", "Delete method called for event: " + event.getId());
        originalDAO.delete(event);
        deleteFromFirebase(event);
    }

    @Override
    public EventEntity getEventById(int id) {
        Log.d("FirebaseEventDAO", "GetEventById called for id: " + id);
        return originalDAO.getEventById(id);
    }

    @Override
    public List<EventEntity> getEventsByUserId(int userId) {
        Log.d("FirebaseEventDAO", "GetEventsByUserId called for userId: " + userId);
        return originalDAO.getEventsByUserId(userId);
    }

    @Override
    public List<EventEntity> getEventsByUserAndDate(int userId, String date) {
        Log.d("FirebaseEventDAO", "GetEventsByUserAndDate called for userId: " + userId + ", date: " + date);
        return originalDAO.getEventsByUserAndDate(userId, date);
    }

    public Task<Void> syncEventsFromFirebase(int userId) {
        Log.d("FirebaseEventDAO", "syncEventsFromFirebase called for userId: " + userId);
        return firestore.collection("users")
                .document(String.valueOf(userId))
                .collection("events")
                .get()
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        Log.e("FirebaseEventDAO", "Failed to sync events from Firestore: " + task.getException());
                        throw task.getException();
                    }

                    QuerySnapshot querySnapshot = task.getResult();
                    if (querySnapshot == null || querySnapshot.isEmpty()) {
                        Log.d("FirebaseEventDAO", "No events found in Firestore for userId: " + userId);
                        return Tasks.forResult(null);
                    }

                    List<EventEntity> events = querySnapshot.toObjects(EventEntity.class);
                    return new SyncEventsTask(events).execute();
                });
    }

    private class SyncEventsTask extends AsyncTask<Void, Void, Void> {
        private final List<EventEntity> events;
        private Task<Void> resultTask;

        SyncEventsTask(List<EventEntity> events) {
            this.events = events;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                for (EventEntity event : events) {
                    Log.d("FirebaseEventDAO", "Event found in Firestore: " + event.getId());
                    EventEntity existingEvent = originalDAO.getEventById(event.getId());
                    if (existingEvent != null) {
                        originalDAO.update(event);
                        Log.d("FirebaseEventDAO", "Event updated in Room: " + event.getId());
                    } else {
                        originalDAO.insert(event);
                        Log.d("FirebaseEventDAO", "Event inserted into Room: " + event.getId());
                    }
                }
                return null;
            } catch (Exception e) {
                Log.e("FirebaseEventDAO", "Error syncing events to Room: " + e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(Void result) {
            resultTask = Tasks.forResult(null);
        }

        public Task<Void> execute() {
            super.execute();
            while (resultTask == null) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Log.e("FirebaseEventDAO", "Interrupted while waiting for SyncEventsTask: " + e.getMessage());
                }
            }
            return resultTask;
        }
    }

    private void syncToFirebase(EventEntity event) {
        Log.d("FirebaseEventDAO", "syncToFirebase called for event: " + event.getId());
        firestore.collection("users")
                .document(String.valueOf(event.getUserId()))
                .collection("events")
                .document(String.valueOf(event.getId()))
                .set(event)
                .addOnSuccessListener(aVoid -> Log.d("FirebaseEventDAO", "Event synced successfully: " + event.getId()))
                .addOnFailureListener(e -> Log.e("FirebaseEventDAO", "Failed to sync event: " + event.getId(), e));
    }

    private void deleteFromFirebase(EventEntity event) {
        Log.d("FirebaseEventDAO", "deleteFromFirebase called for event: " + event.getId());
        firestore.collection("users")
                .document(String.valueOf(event.getUserId()))
                .collection("events")
                .document(String.valueOf(event.getId()))
                .delete()
                .addOnSuccessListener(aVoid -> Log.d("FirebaseEventDAO", "Event deleted from Firebase: " + event.getId()))
                .addOnFailureListener(e -> Log.e("FirebaseEventDAO", "Failed to delete event from Firebase: " + event.getId(), e));
    }
}