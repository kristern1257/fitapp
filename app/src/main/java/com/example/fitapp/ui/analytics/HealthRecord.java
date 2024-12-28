package com.example.fitapp.ui.analytics;


import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.example.fitapp.ui.home.MainActivity;
import com.example.fitapp.R;
import com.example.fitapp.javahelperfile.analytics.SleepRecordManager;
import com.example.fitapp.javahelperfile.profile.User;
import com.example.fitapp.javahelperfile.analytics.UserWeightBMIManager;
import com.example.fitapp.javahelperfile.analytics.WaterIntakeManager;
import com.example.fitapp.ui.profile.Profile;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import com.google.firebase.FirebaseApp;

public class HealthRecord extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView progressText;
    private int currentIntake = 0; // Member variable to retain water intake
    private final int dailyGoal = 1600; // Daily water goal in ml
    Button buttonRecordHours,buttonRecordHeightAndWeight;
    TextView tvHoursRecord,tvheight, tvweight,tvUserBmi, username;

    private WaterIntakeManager waterIntakeManager;
    private SleepRecordManager sleepRecordManager;

    private UserWeightBMIManager userWeightBMIManager;

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_record);
        FirebaseApp.initializeApp(this);

        // Retrieve the User object
        user = getIntent().getParcelableExtra("user");
        // Water Tracking
        progressBar = findViewById(R.id.progress_bar);
        progressText = findViewById(R.id.progress_text);
        progressBar.setMax(dailyGoal);

        waterIntakeManager = new WaterIntakeManager(user,progressBar,progressText,HealthRecord.this);
        waterIntakeManager.getWaterIntakeFromDatabase();

        // Sleeping Hours
        buttonRecordHours = findViewById(R.id.BtnRecordHours);
        tvHoursRecord = findViewById(R.id.TVHoursRecord); // Assuming this is already defined in your layout

        sleepRecordManager = new SleepRecordManager(user,tvHoursRecord,HealthRecord.this);
        sleepRecordManager.getSleepDataFromDatabase();
        username = findViewById(R.id.TVUsername);
        username.setText(user.getUsername());

        // Weight, Height, BMI checking
        buttonRecordHeightAndWeight = findViewById(R.id.btnRecordWeightHeight);
        tvheight = findViewById(R.id.TVUserHeight);
        tvweight = findViewById(R.id.TVUserWeight);
        tvUserBmi = findViewById(R.id.TVUserBmi);
        userWeightBMIManager = new UserWeightBMIManager(user,tvheight,tvweight,tvUserBmi,HealthRecord.this);
        userWeightBMIManager.getWeightBMIFromDatabase();

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
        Toolbar toolbar = findViewById(R.id.toolbarHealthRecord);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Drawable upArrow = ContextCompat.getDrawable(this, R.drawable.baseline_arrow_back_ios_24);
        if (upArrow != null) {
            DrawableCompat.setTint(upArrow, ContextCompat.getColor(this, R.color.black)); // Set your desired color
            getSupportActionBar().setHomeAsUpIndicator(upArrow);
        }

        // Show Date
        TextView dateTextView = findViewById(R.id.TVTodayDate); // Replace with your TextView ID

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault());
        String todayDate = dateFormat.format(calendar.getTime());

        dateTextView.setText(todayDate);

        Button viewHealthReport = findViewById(R.id.BtnViewHealthReport);
        viewHealthReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to start the NextActivity
                Intent intent = new Intent(HealthRecord.this, ViewHealthReport.class);
                intent.putExtra("user", user); // Attach the Parcelable User object
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left); // Optional transition
            }
        });



        Button addWaterButton = findViewById(R.id.add_water_button);
        addWaterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Your logic to update the progress bar and water intake
                int intakeAmount = 200;
                currentIntake += intakeAmount;

                if (currentIntake > dailyGoal) {
                    currentIntake = dailyGoal;
                }

                progressText.setText(currentIntake + " / " + dailyGoal);
                progressBar.setProgress(currentIntake);

                if (currentIntake == dailyGoal) {
                    Toast.makeText(getApplicationContext(), "Congratulations! You've reached your daily water goal!", Toast.LENGTH_SHORT).show();
                }

                waterIntakeManager.addWaterIntake(intakeAmount);
            }
        });


        buttonRecordHours.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSleepingHoursDialog(); // Call the method to show the dialog
            }
        });

        buttonRecordHeightAndWeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHeightAndWeightDialog(); // Call the method to show the dialog
            }
        });

    }

    private void showSleepingHoursDialog() {
        // Create and set up the dialog
        Dialog dialog = new Dialog(HealthRecord.this);
        dialog.setContentView(R.layout.record_sleeping_hours); // Your dialog layout
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialog_background));

        // Initialize dialog elements
        EditText editTextDialogSleepingHours = dialog.findViewById(R.id.ETSleepingHours);
        Button buttonDialogRecordSleepingHours = dialog.findViewById(R.id.BtnRecord);

        // Set up the button to record hours
        buttonDialogRecordSleepingHours.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sleepingHoursInput = editTextDialogSleepingHours.getText().toString().trim();

                // Validate the input
                if (!sleepingHoursInput.isEmpty()) {
                    try {
                        // Try to parse the input to an integer
                        int sleepingHours = Integer.parseInt(sleepingHoursInput);

                        // Check if the entered hours are within a reasonable range
                        if (sleepingHours < 0 || sleepingHours > 24) {
                            Toast.makeText(HealthRecord.this, "Please enter a valid number of hours (0-24)", Toast.LENGTH_SHORT).show();
                        } else {
                            // Update the main layout TextView
                            tvHoursRecord.setText(String.valueOf(sleepingHours));
                            sleepRecordManager.storeSleepData(sleepingHours);
                            dialog.dismiss(); // Close the dialog
                        }
                    } catch (NumberFormatException e) {
                        // Show a prompt if input is not a valid number
                        Toast.makeText(HealthRecord.this, "Invalid input. Please enter a valid number.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Prompt if the input field is empty
                    Toast.makeText(HealthRecord.this, "Please enter your sleeping hours", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialog.show(); // Show the dialog
    }

    private void showHeightAndWeightDialog() {
        // Create and set up the dialog
        Dialog dialog = new Dialog(HealthRecord.this);
        dialog.setContentView(R.layout.record_weight_and_height); // Your dialog layout
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialog_background));

        // Initialize dialog elements
        EditText editTextHeight = dialog.findViewById(R.id.ETHeight);
        EditText editTextWeight = dialog.findViewById(R.id.ETWeight);
        Button buttonDialogRecordHeightAndWeight = dialog.findViewById(R.id.BtnRecordHeightAndWeight);

        buttonDialogRecordHeightAndWeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String heightStr = editTextHeight.getText().toString().trim();
                String weightStr = editTextWeight.getText().toString().trim();

                if (heightStr.isEmpty() || weightStr.isEmpty()) {
                    Toast.makeText(HealthRecord.this, "Please enter both height and weight", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    // Parse height and weight as integers
                    int height = Integer.parseInt(heightStr);
                    int weight = Integer.parseInt(weightStr);

                    // Validate height (in cm) and weight (in kg)
                    if (height < 50 || height > 300) {
                        Toast.makeText(HealthRecord.this, "Please enter a valid height between 50 and 300 cm", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (weight < 2 || weight > 500) {
                        Toast.makeText(HealthRecord.this, "Please enter a valid weight between 2 and 500 kg", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Display the height and weight with or without decimals based on input
                    tvheight.setText(heightStr.contains(".") ? heightStr : String.valueOf(height));
                    tvweight.setText(weightStr.contains(".") ? weightStr : String.valueOf(weight));

                    // Convert height to meters for BMI calculation
                    double heightInMeters = height / 100.0;

                    // Calculate BMI
                    double bmi = weight / (heightInMeters * heightInMeters);

                    // Display the BMI in TVUserBmi, rounded to one decimal place
                    tvUserBmi.setText(String.format("%.1f", bmi));

                    // Save the data using UserWeightBMIManager
                    userWeightBMIManager.storeUserWeightBMI(height, weight);

                    // Dismiss the dialog
                    dialog.dismiss();
                } catch (NumberFormatException e) {
                    // Show an error if either input is not a valid number
                    Toast.makeText(HealthRecord.this, "Please enter valid integers for height and weight", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialog.show(); // Show the dialog
    }



    // for toolbar use to return to previous page
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}