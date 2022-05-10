package com.example.project.user_profile;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        firstName = findViewById(R.id.textViewFirstName);
        lastName = findViewById(R.id.textViewLastName);
        email = findViewById(R.id.textViewEmail);

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

    }
}