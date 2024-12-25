package com.example.fitapp.ui.generalmodule;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fitapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class SignUp extends AppCompatActivity {
    private Button signIn;

    private EditText usernameField, emailField, passwordField, confirmPasswordField;
    private Button signUpButton;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        signIn = findViewById(R.id.signIn_signuppage);
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUp.this, Login.class);
                startActivity(intent);
            }
        });

        FirebaseApp.initializeApp(this);

        // Initialize Firebase Auth and Database Reference
        mAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference("User");

        usernameField = findViewById(R.id.usernameField);
        emailField = findViewById(R.id.emailField);
        passwordField = findViewById(R.id.passwordField);
        confirmPasswordField = findViewById(R.id.confirmPasswordField);
        signUpButton = findViewById(R.id.signUpButton);


        // Set up sign up button functionality
        signUpButton.setOnClickListener(v -> registerUser());
    }

    private boolean isValidEmail(String email) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        return email.matches(emailPattern);
    }

    private void registerUser() {
        String username = usernameField.getText().toString().trim();
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();
        String confirmPassword = confirmPasswordField.getText().toString().trim();

        // Validate input
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidEmail(email)) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 8) {
            Toast.makeText(this, "Password must be at least 8 characters long", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check for duplicate username or email
        checkForExistingUsernameOrEmail(username, email, password);
    }

    // Check if username or email already exists
    private void checkForExistingUsernameOrEmail(final String username, final String email, final String password) {
        // Check if username exists
        databaseRef.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot usernameSnapshot) {
                if (usernameSnapshot.exists()) {
                    Toast.makeText(SignUp.this, "Username already taken. Please choose another.", Toast.LENGTH_SHORT).show();
                } else {
                    // Check if email exists
                    databaseRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot emailSnapshot) {
                            if (emailSnapshot.exists()) {
                                Toast.makeText(SignUp.this, "Email already registered. Please use another email.", Toast.LENGTH_SHORT).show();
                            } else {
                                // Add user to the database
                                addUserToDB(username, email, password);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(SignUp.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(SignUp.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Add user data to Firebase Database
    private void addUserToDB(String username, String email, String password) {
        // Generate a unique userID using username and current timestamp
        String userID = username + "_" + System.currentTimeMillis();

        // Create a HashMap to store the user data
        HashMap<String, Object> userHashMap = new HashMap<>();
        userHashMap.put("username", username);
        userHashMap.put("user_email", email);
        userHashMap.put("user_password", password); // You might want to hash the password before storing
        userHashMap.put("user_birthday", "Prefer Not to Say"); // Default DOB
        userHashMap.put("user_phonenumber", "Prefer Not to Say"); // Default phone number
        userHashMap.put("user_points", 0); // Initial points
        userHashMap.put("profile_pic_path", null); // Default: No profile picture
        userHashMap.put("user_sign_up_date", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(new Date())); // Current date and time
        userHashMap.put("user_types", "Bronze");

        // Instantiate Firebase Database connection
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference usersRef = database.getReference("User");

        // Save the user data to the Firebase Realtime Database
        usersRef.child(userID).setValue(userHashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(SignUp.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SignUp.this, Login.class)); // Navigate to Login page
                    finish(); // Close the sign-up activity
                } else {
                    Toast.makeText(SignUp.this, "Failed to register user", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void createFirebaseUser(String email, String password, HashMap<String, Object> userMap) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            saveUserToRealtimeDatabase(firebaseUser.getUid(), userMap);
                        }
                    } else {
                        Toast.makeText(SignUp.this, "Sign Up Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserToRealtimeDatabase(String uid, HashMap<String, Object> userMap) {
        // Save user data to the "users" node in Firebase Realtime Database
        databaseRef.child(uid).setValue(userMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(SignUp.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SignUp.this, Login.class));
                        finish();
                    } else {
                        Toast.makeText(SignUp.this, "Failed to save user: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}