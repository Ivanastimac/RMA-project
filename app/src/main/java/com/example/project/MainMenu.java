package com.example.project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import com.example.project.picturebook.NewPicturebook;
import com.example.project.user_profile.Login;
import com.example.project.user_profile.Settings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainMenu extends AppCompatActivity {

    ImageView profileButton;
    Button newPicturebook;
    FirebaseAuth auth;
    FirebaseUser loggedInUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        auth = FirebaseAuth.getInstance();
        loggedInUser = auth.getCurrentUser();

        // if user is not logged in, redirect to login page
        if (loggedInUser == null) {
            Intent in = new Intent(this, Login.class);
            startActivity(in);
        }

        profileButton = findViewById(R.id.imageButtonProfile);
        newPicturebook = findViewById(R.id.buttonNew);

        profileButton.setOnClickListener(view -> {
            Intent in = new Intent(view.getContext(), Settings.class);
            view.getContext().startActivity(in);
        });

        newPicturebook.setOnClickListener(view -> {
            Intent in = new Intent(view.getContext(), NewPicturebook.class);
            view.getContext().startActivity(in);
        });
    }
}