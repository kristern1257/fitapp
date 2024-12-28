package com.example.fitapp.ui.analytics;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.example.fitapp.ui.home.MainActivity;
import com.example.fitapp.R;
import com.example.fitapp.javahelperfile.profile.User;
import com.example.fitapp.ui.profile.Profile;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ViewHealthReport extends AppCompatActivity {

    private User user;
    private FirebaseDatabase database;
    private DatabaseReference waterIntakeRef;

    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_health_report);

        // Retrieve the User object
        user = getIntent().getParcelableExtra("user");
        userId=user.getUserId();

        // Firebase initialization
        database = FirebaseDatabase.getInstance();
        waterIntakeRef = database.getReference("Water_Intake");

        getAverageWaterIntake(userId);
        getAverageSleepHours(userId);
        getTotalWeightReduction(userId);
        // Bottom menu
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.bottom_analytics);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.bottom_home) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra("user", user);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (item.getItemId() == R.id.bottom_analytics) {
                Intent intent = new Intent(getApplicationContext(), Analytics.class);
                intent.putExtra("user", user); // Attach the Parcelable User object
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
            } else if (item.getItemId() == R.id.bottom_profile) {
                Intent intent = new Intent(getApplicationContext(), Profile.class);
                intent.putExtra("user", user); // Attach the Parcelable User object
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            }
            return false;
        });

        // Upper Toolbar
        Toolbar toolbar = findViewById(R.id.toolbarHealthReport);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Drawable upArrow = ContextCompat.getDrawable(this, R.drawable.baseline_arrow_back_ios_24);
        if (upArrow != null) {
            DrawableCompat.setTint(upArrow, ContextCompat.getColor(this, R.color.black)); // Set your desired color
            getSupportActionBar().setHomeAsUpIndicator(upArrow);
        }

    }

    // for toolbar use to return to previous page
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void getAverageWaterIntake(String userId) {
        // Reference to the water intake records
        DatabaseReference userWaterRef = waterIntakeRef;

        userWaterRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int totalWaterIntake = 0;
                int count = 0;

                // Get the current month and year
                Calendar calendar = Calendar.getInstance();
                int currentMonth = calendar.get(Calendar.MONTH) + 1; // Months are 0-based
                int currentYear = calendar.get(Calendar.YEAR);

                // Log the current month and year for debugging
                Log.d("WaterIntake", "Current Month: " + currentMonth + ", Current Year: " + currentYear);

                if (!dataSnapshot.exists()) {
                    Log.d("WaterIntake", "No records found.");
                    return;
                }

                // Iterate through each waterintake_id
                for (DataSnapshot waterIntakeSnapshot : dataSnapshot.getChildren()) {
                    // Iterate through the user records under each waterintake_id
                    for (DataSnapshot userSnapshot : waterIntakeSnapshot.getChildren()) {
                        // Check if the userId matches
                        if (userSnapshot.getKey() != null && userSnapshot.getKey().equals(userId)) {
                            Integer waterIntake = userSnapshot.child("water_intake").getValue(Integer.class);
                            String waterDatetime = userSnapshot.child("water_datetime").getValue(String.class);

                            if (waterIntake != null && waterDatetime != null) {
                                try {
                                    // Parse the water_datetime field
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
                                    Date date = sdf.parse(waterDatetime);

                                    if (date != null) {
                                        Calendar recordCalendar = Calendar.getInstance();
                                        recordCalendar.setTime(date);

                                        int recordMonth = recordCalendar.get(Calendar.MONTH) + 1; // Months are 0-based
                                        int recordYear = recordCalendar.get(Calendar.YEAR);

                                        // Log the month and year of the record for debugging
                                        Log.d("WaterIntake", "Record Month: " + recordMonth + ", Record Year: " + recordYear);

                                        // Compare the month and year
                                        if (recordMonth == currentMonth && recordYear == currentYear) {
                                            totalWaterIntake += waterIntake;
                                            count++;
                                        }
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }

                // Log the total and count for debugging
                Log.d("WaterIntake", "Total: " + totalWaterIntake + ", Count: " + count);

                // Calculate and display the average if records exist for the current month
                if (count > 0) {
                    int averageWaterIntake = totalWaterIntake / count;
                    displayAverageWaterIntake(averageWaterIntake);
                } else {
                    displayAverageWaterIntake(0); // No records for the current month
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
                Log.e("Firebase", "Error retrieving water intake data", databaseError.toException());
            }
        });
    }

    private void displayAverageWaterIntake(int averageWaterIntake) {
        // Assuming you have a TextView to display the average water intake
        TextView averageWaterIntakeText = findViewById(R.id.TVMonthlyAverageWaterIntake);
        String averageText = averageWaterIntake + " ml";
        averageWaterIntakeText.setText(averageText);
    }

    private void getAverageSleepHours(String userId) {
        // Reference to the sleep record data
        DatabaseReference sleepRecordRef = FirebaseDatabase.getInstance().getReference("Sleep_Record");

        sleepRecordRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int totalSleepHours = 0;
                int count = 0;

                // Get the current month and year
                Calendar calendar = Calendar.getInstance();
                int currentMonth = calendar.get(Calendar.MONTH) + 1; // Months are 0-based
                int currentYear = calendar.get(Calendar.YEAR);

                // Log the current month and year for debugging
                Log.d("SleepRecord", "Current Month: " + currentMonth + ", Current Year: " + currentYear);

                if (!dataSnapshot.exists()) {
                    Log.d("SleepRecord", "No records found.");
                    return;
                }

                // Iterate through each sleeprecord_id
                for (DataSnapshot sleepRecordSnapshot : dataSnapshot.getChildren()) {
                    // Iterate through the user records under each sleeprecord_id
                    for (DataSnapshot userSnapshot : sleepRecordSnapshot.getChildren()) {
                        // Check if the userId matches
                        if (userSnapshot.getKey() != null && userSnapshot.getKey().equals(userId)) {
                            Integer sleepHours = userSnapshot.child("sleep_hours").getValue(Integer.class);
                            String sleepRecordDate = userSnapshot.child("sleeprecord_date").getValue(String.class);

                            if (sleepHours != null && sleepRecordDate != null) {
                                try {
                                    // Parse the sleeprecord_date field
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
                                    Date date = sdf.parse(sleepRecordDate);

                                    if (date != null) {
                                        Calendar recordCalendar = Calendar.getInstance();
                                        recordCalendar.setTime(date);

                                        int recordMonth = recordCalendar.get(Calendar.MONTH) + 1; // Months are 0-based
                                        int recordYear = recordCalendar.get(Calendar.YEAR);

                                        // Log the month and year of the record for debugging
                                        Log.d("SleepRecord", "Record Month: " + recordMonth + ", Record Year: " + recordYear);

                                        // Compare the month and year
                                        if (recordMonth == currentMonth && recordYear == currentYear) {
                                            totalSleepHours += sleepHours;
                                            count++;
                                        }
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }

                // Log the total and count for debugging
                Log.d("SleepRecord", "Total: " + totalSleepHours + ", Count: " + count);

                // Calculate and display the average if records exist for the current month
                if (count > 0) {
                    // Calculate the average as a float to preserve decimal places
                    float averageSleepHours = (float) totalSleepHours / count;

                    // Format and display the average sleep hours with one decimal place
                    displayAverageSleepHours(averageSleepHours);
                } else {
                    displayAverageSleepHours(0); // No records for the current month
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors
                Log.e("SleepRecord", "Error retrieving sleep data", databaseError.toException());
            }
        });
    }

    private void displayAverageSleepHours(float averageSleepHours) {
        // Format the average sleep hours to one decimal place
        DecimalFormat decimalFormat = new DecimalFormat("#.0");
        String formattedAverage = decimalFormat.format(averageSleepHours);

        // Assuming you have a TextView to display the average sleep hours
        TextView averageSleepHoursText = findViewById(R.id.TVMonthlyAverageSleepingHours);
        String averageText = formattedAverage + " hours";
        averageSleepHoursText.setText(averageText);
    }

    private void getTotalWeightReduction(String userId) {
        // Reference to the weight and BMI data
        DatabaseReference weightBmiRef = FirebaseDatabase.getInstance().getReference("User_Weight_BMI");

        weightBmiRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                float firstWeight = -1; // To store the first weight entry
                float lastWeight = -1;  // To store the last weight entry
                float totalWeightReduction = 0;

                // Get the current month and year
                Calendar calendar = Calendar.getInstance();
                int currentMonth = calendar.get(Calendar.MONTH) + 1; // Months are 0-based
                int currentYear = calendar.get(Calendar.YEAR);

                // Log the current month and year for debugging
                Log.d("WeightRecord", "Current Month: " + currentMonth + ", Current Year: " + currentYear);

                if (!dataSnapshot.exists()) {
                    Log.d("WeightRecord", "No records found.");
                    return;
                }

                // Iterate through each weight_bmi_id
                for (DataSnapshot weightBmiSnapshot : dataSnapshot.getChildren()) {
                    // Iterate through the user records under each weight_bmi_id
                    for (DataSnapshot userSnapshot : weightBmiSnapshot.getChildren()) {
                        // Check if the userId matches
                        if (userSnapshot.getKey() != null && userSnapshot.getKey().equals(userId)) {
                            Float userWeight = userSnapshot.child("user_weight").getValue(Float.class);
                            String recordedDate = userSnapshot.child("recorded_date").getValue(String.class);

                            if (userWeight != null && recordedDate != null) {
                                try {
                                    // Parse the recorded_date field
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
                                    Date date = sdf.parse(recordedDate);

                                    if (date != null) {
                                        Calendar recordCalendar = Calendar.getInstance();
                                        recordCalendar.setTime(date);

                                        int recordMonth = recordCalendar.get(Calendar.MONTH) + 1; // Months are 0-based
                                        int recordYear = recordCalendar.get(Calendar.YEAR);

                                        // Log the month and year of the record for debugging
                                        Log.d("WeightRecord", "Record Month: " + recordMonth + ", Record Year: " + recordYear);

                                        // Compare the month and year
                                        if (recordMonth == currentMonth && recordYear == currentYear) {
                                            // Track the first and most recent weight
                                            if (firstWeight == -1) {
                                                firstWeight = userWeight; // Set the first weight
                                            }
                                            lastWeight = userWeight; // Always update the last weight
                                        }
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }

                // Calculate total weight reduction
                if (firstWeight != -1 && lastWeight != -1) {
                    if (lastWeight < firstWeight) {
                        totalWeightReduction = firstWeight - lastWeight;
                        displayTotalWeightReduction(totalWeightReduction);
                    } else {
                        displayTotalWeightReduction(0); // No reduction
                    }
                } else {
                    displayTotalWeightReduction(0); // No records for the current month
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors
                Log.e("WeightRecord", "Error retrieving weight data", databaseError.toException());
            }
        });
    }

    private void displayTotalWeightReduction(float weightReduction) {
        // Display weight reduction as an integer (no decimal places)
        int weightReductionInt = (int) weightReduction;

        // Assuming you have a TextView to display the weight reduction
        TextView totalWeightReductionText = findViewById(R.id.TVMonthlyWeightReduce);
        String reductionText = "-"+weightReductionInt + " kg";
        totalWeightReductionText.setText(reductionText);
    }




}
