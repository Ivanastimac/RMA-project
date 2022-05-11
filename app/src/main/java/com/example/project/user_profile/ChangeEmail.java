package com.example.project.user_profile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.project.MainActivity;
import com.example.project.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangeEmail extends AppCompatActivity {

    EditText newEmail;
    Button changeEmailBtn;
    FirebaseAuth auth;
    FirebaseUser loggedInUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_email);

        newEmail = findViewById(R.id.inputEmailChange);
        changeEmailBtn = findViewById(R.id.buttonChangeEmailConfirm);

        auth = FirebaseAuth.getInstance();
        loggedInUser = auth.getCurrentUser();

        // if user is not logged in, redirect to login page
        if (loggedInUser == null) {
            Intent in = new Intent(this, Login.class);
            startActivity(in);
        }

        changeEmailBtn.setOnClickListener(view -> {

            if (checkEnteredData()) {
                loggedInUser.updateEmail(newEmail.getText().toString().trim())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ChangeEmail.this, "Email address is updated.", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(ChangeEmail.this, "Failed to update email!", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });

    }

    boolean checkEnteredData() {

        boolean valid = true;

        if (newEmail == null || newEmail.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter new e-mail address.", Toast.LENGTH_SHORT).show();
            valid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(newEmail.getText().toString()).matches()) {
            Toast.makeText(this, "E-mail address is not valid!", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        return valid;

    }
}