package com.example.project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.example.project.user_profile.Login;
import com.example.project.user_profile.Settings;

public class MainMenu extends AppCompatActivity {

    ImageView profileButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        profileButton = findViewById(R.id.imageButtonProfile);

        profileButton.setOnClickListener(view -> {
            Intent in = new Intent(view.getContext(), Settings.class);
            view.getContext().startActivity(in);
        });
    }
}