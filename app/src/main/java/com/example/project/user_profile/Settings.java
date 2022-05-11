package com.example.project.user_profile;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.project.R;
import com.example.project.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Settings extends AppCompatActivity {

    TextView firstName;
    TextView lastName;
    TextView email;
    Button changePasswordBtn;
    Button changeEmailBtn;
    Button deleteAccountBtn;
    Button signOutBtn;
    FirebaseAuth auth;
    FirebaseUser loggedInUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        auth = FirebaseAuth.getInstance();
        loggedInUser = auth.getCurrentUser();

        // if user is not logged in, redirect to login page
        if (loggedInUser == null) {
            Intent in = new Intent(this, Login.class);
            startActivity(in);
        }

        firstName = findViewById(R.id.textViewFirstName);
        lastName = findViewById(R.id.textViewLastName);
        email = findViewById(R.id.textViewEmail);
        changePasswordBtn = findViewById(R.id.buttonChangePassword);

        DatabaseReference database = FirebaseDatabase.getInstance().getReference("users");
        FirebaseUser loggedInUser = FirebaseAuth.getInstance().getCurrentUser();
        email.setText(loggedInUser.getEmail());

        // get logged in user info and display it on screen
        database.child(loggedInUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                firstName.setText(user.getFirstName());
                lastName.setText(user.getLastName());
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(Settings.this, "Failed to read value." + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        changePasswordBtn.setOnClickListener(view -> {
            Intent in = new Intent(view.getContext(), ChangePassword.class);
            view.getContext().startActivity(in);
        });

    }
}