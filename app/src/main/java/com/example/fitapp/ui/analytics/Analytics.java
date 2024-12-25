package com.example.fitapp.ui.analytics;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fitapp.ui.home.MainActivity;
import com.example.fitapp.R;
import com.example.fitapp.javahelperfile.profile.User;
import com.example.fitapp.ui.profile.Profile;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Analytics extends AppCompatActivity {

    Button healthRecordButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);
        // Retrieve the User object
        User user = getIntent().getParcelableExtra("user");

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
            }else if (item.getItemId() == R.id.bottom_analytics) {
                return true;
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

        healthRecordButton = findViewById(R.id.BtnHealthReport);
        healthRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to start the NextActivity
                Intent intent = new Intent(Analytics.this, HealthRecord.class);
                intent.putExtra("user", user);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left); // Optional transition
            }
        });


    }
}