package com.example.fitapp.ui.ratingsystem;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fitapp.ui.analytics.Analytics;
import com.example.fitapp.ui.home.MainActivity;
import com.example.fitapp.R;
import com.example.fitapp.javahelperfile.profile.User;
import com.example.fitapp.ui.profile.Profile;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class RateUs extends AppCompatActivity {

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_us);
        // Enable the Up button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Retrieve the User object
        user = getIntent().getParcelableExtra("user");

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.bottom_profile);

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
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Handle the Up button action
            finish(); // Closes the current activity and returns to the parent
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}