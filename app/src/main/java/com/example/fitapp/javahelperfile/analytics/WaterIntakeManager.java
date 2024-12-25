package com.example.fitapp.javahelperfile.analytics;

import android.content.Context;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fitapp.javahelperfile.profile.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class WaterIntakeManager {

    private static final int DAILY_WATER_GOAL = 1600; // Maximum water intake per day
    private DatabaseReference databaseReference;
    private String userId; // Example User ID (dynamic in real scenarios)
    private ProgressBar progressBar;
    private TextView progressText;
    private Context context;

    public WaterIntakeManager(User user, ProgressBar progressBar, TextView progressText, Context context) {
        this.context = context;
        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference();
        userId= user.getUserId();
        this.progressBar = progressBar;
        this.progressText = progressText;
    }

    public void addWaterIntake(int intakeAmount) {
        // Format the current datetime in ISO 8601 format (with time)
        String currentDateTime = getCurrentISODateTime(); // Full ISO 8601 format

        // Check if an entry already exists for the user on the current date (using only the date part for comparison)
        databaseReference.child("Water_Intake").orderByChild(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean entryExists = false;
                String existingKey = null;
                int existingIntake = 0;

                for (DataSnapshot intakeSnapshot : snapshot.getChildren()) {
                    HashMap<String, Object> data = (HashMap<String, Object>) intakeSnapshot.child(userId).getValue();
                    if (data != null && data.get("water_datetime") != null) {
                        String existingDateTime = (String) data.get("water_datetime");
                        String existingDate = existingDateTime.substring(0, 10); // Extract date (first 10 characters)

                        if (existingDate.equals(currentDateTime.substring(0, 10))) { // Compare only the date part
                            entryExists = true;
                            existingKey = intakeSnapshot.getKey();
                            existingIntake = ((Long) data.get("water_intake")).intValue();
                            break;
                        }
                    }
                }

                // Update the existing record or create a new one
                if (entryExists && existingKey != null) {
                    int newIntake = Math.min(existingIntake + intakeAmount, DAILY_WATER_GOAL); // Cap at 1600
                    updateWaterIntake(existingKey, currentDateTime, newIntake);
                } else {
                    int newIntake = Math.min(intakeAmount, DAILY_WATER_GOAL); // Cap at 1600
                    addNewWaterIntake(currentDateTime, newIntake);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("WaterIntakeManager", "Error checking water intake: " + error.getMessage());
            }
        });
    }

    private void addNewWaterIntake(String dateTime, int intakeAmount) {
        // Generate a new unique key
        String waterIntakeId = databaseReference.child("Water_Intake").push().getKey();
        if (waterIntakeId != null) {
            // Prepare data
            HashMap<String, Object> waterIntakeData = new HashMap<>();
            waterIntakeData.put("water_datetime", dateTime);
            waterIntakeData.put("water_intake", intakeAmount);

            // Save to Firebase
            databaseReference.child("Water_Intake").child(waterIntakeId).child(userId).setValue(waterIntakeData)
                    .addOnSuccessListener(aVoid -> Log.d("WaterIntakeManager", "Water intake added successfully"))
                    .addOnFailureListener(e -> Log.e("WaterIntakeManager", "Failed to add water intake: " + e.getMessage()));
        }
    }

    private void updateWaterIntake(String key, String dateTime, int intakeAmount) {
        // Prepare updated data
        HashMap<String, Object> updatedData = new HashMap<>();
        updatedData.put("water_datetime", dateTime);
        updatedData.put("water_intake", intakeAmount);

        // Update the existing record in Firebase
        databaseReference.child("Water_Intake").child(key).child(userId).updateChildren(updatedData)
                .addOnSuccessListener(aVoid -> Log.d("WaterIntakeManager", "Water intake updated successfully"))
                .addOnFailureListener(e -> Log.e("WaterIntakeManager", "Failed to update water intake: " + e.getMessage()));
    }

    private String getCurrentISODateTime() {
        // Get the current date and time
        Date now = new Date();

        // Format in ISO 8601 format (yyyy-MM-dd'T'HH:mm:ss'Z')
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        return isoFormat.format(now);
    }

    public void getWaterIntakeFromDatabase() {
        // Get current date in ISO format (yyyy-MM-dd)
        String currentDate = getCurrentISODateTime().substring(0, 10);  // Get just the date (yyyy-MM-dd)

        // Query Firebase for data matching the current date and the userId
        databaseReference.child("Water_Intake")
                .orderByChild(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        Log.d("WaterIntakeManager", "Number of children in snapshot: " + snapshot.getChildrenCount());

                        int totalIntake = 0;
                        boolean dataFoundForToday = false;

                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Log.d("WaterIntakeManager", "Data Snapshot: " + dataSnapshot.getKey());

                            // Assuming your data has a date field to compare
                            String recordDate = dataSnapshot.child(userId).child("water_datetime").getValue(String.class).substring(0, 10);

                            if (recordDate != null && recordDate.equals(currentDate)) {
                                Integer waterIntake = dataSnapshot.child(userId).child("water_intake").getValue(Integer.class);

                                if (waterIntake != null) {
                                    totalIntake += waterIntake;
                                    dataFoundForToday = true;
                                }
                            }
                        }

                        // If no data found for today, set total intake to 0
                        if (!dataFoundForToday) {
                            totalIntake = 0;
                        }

                        updateUIWithWaterIntake(totalIntake);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(context, "Failed to read water intake data: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void updateUIWithWaterIntake(int totalIntake) {
        // Ensure UI updates happen on the main thread
        progressText.post(new Runnable() {
            @Override
            public void run() {

                // Update the progress text and progress bar
                progressText.setText(totalIntake + " / " + DAILY_WATER_GOAL);
                progressBar.setProgress(totalIntake);
            }
        });
    }



}