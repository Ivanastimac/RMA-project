package com.example.project.user_profile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.project.MainActivity;
import com.example.project.R;
import com.example.project.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
    FirebaseAuth.AuthStateListener authListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // called in onStart() method
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(Settings.this, Login.class));
                    finish();
                }
            }
        };

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
        changeEmailBtn = findViewById(R.id.buttonChangeEmail);
        deleteAccountBtn = findViewById(R.id.buttonDeleteAccount);
        signOutBtn = findViewById(R.id.buttonSignOut);

        DatabaseReference database = FirebaseDatabase.getInstance().getReference("users");
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

        changeEmailBtn.setOnClickListener(view -> {
            Intent in = new Intent(view.getContext(), ChangeEmail.class);
            view.getContext().startActivity(in);
        });

        deleteAccountBtn.setOnClickListener(view -> {
            loggedInUser.delete()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(Settings.this, "Your profile is deleted:( Create a account now!", Toast.LENGTH_SHORT).show();
                                Intent in = new Intent(view.getContext(), Login.class);
                                view.getContext().startActivity(in);
                            } else {
                                Toast.makeText(Settings.this, "Failed to delete your account!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        });

        signOutBtn.setOnClickListener(view -> {
            auth.signOut();
            Intent in = new Intent(view.getContext(), Login.class);
            view.getContext().startActivity(in);
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }
}