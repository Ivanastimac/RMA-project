package com.example.project.user_profile;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.project.R;

public class Login extends AppCompatActivity {

    Button redirectToRegister;
    Button signin;
    EditText username;
    EditText password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        redirectToRegister = findViewById(R.id.buttonRedirectToRegister);
        username = findViewById(R.id.editTextUsername);
        password = findViewById(R.id.editTextPassword);
        signin = findViewById(R.id.buttonConfirm);

        redirectToRegister.setOnClickListener(view -> {
            Intent in = new Intent(view.getContext(), Register.class);
            view.getContext().startActivity(in);
        });

        signin.setOnClickListener(view -> {

            if(checkEnteredData()) {

                // TODO check login info from database

                // TODO redirect to new activity
                /*
                Intent in = new Intent(view.getContext(),);
                view.getContext().startActivity(in);*/

            }
        });
    }

    boolean checkEnteredData() {

        boolean valid = true;

        if (username == null || username.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter your username.", Toast.LENGTH_SHORT).show();
            valid = false;
        } else if (password == null || password.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter your password.", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        return valid;
    }

}