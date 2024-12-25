package com.example.fitapp.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fitapp.R;
import com.example.fitapp.javahelperfile.profile.User;
import com.example.fitapp.ui.analytics.Analytics;
import com.example.fitapp.ui.profile.Profile;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.bottom_home);

        // Retrieve User object
        User user = getIntent().getParcelableExtra("user");

        if (user != null) {
            // Use the User object data
            TextView welcomeText = findViewById(R.id.welcomeText);
            welcomeText.setText("Welcome, " + user.getUsername());

        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.bottom_home) {
                return true;
            } else if (item.getItemId() == R.id.bottom_analytics) {
                Intent intent = new Intent(MainActivity.this, Analytics.class);
                intent.putExtra("user", user); // Attach the Parcelable User object
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (item.getItemId() == R.id.bottom_profile) {
                Intent intent = new Intent(MainActivity.this, Profile.class);
                intent.putExtra("user", user); // Attach the Parcelable User object
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            }
            return false;
        });


    }
}