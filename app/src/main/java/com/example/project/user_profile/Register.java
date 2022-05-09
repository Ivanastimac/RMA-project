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
import com.example.project.MainMenu;
import com.example.project.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Pattern;

public class Register extends AppCompatActivity {

    Button redirectToLogin;
    Button register;
    EditText firstName;
    EditText lastName;
    EditText email;
    EditText password;
    EditText passwordConfirm;
    FirebaseAuth auth;

    // No whitespaces, minimum eight characters, at least one uppercase letter, one lowercase letter and one number
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("[a-zA-Z]\\d");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();

        redirectToLogin = findViewById(R.id.buttonRedirectToLogin);
        register = findViewById(R.id.buttonConfirm);
        firstName = findViewById(R.id.editTextFirstName);
        lastName = findViewById(R.id.editTextLastName);
        email = findViewById(R.id.editTextEmail);
        password = findViewById(R.id.editTextPassword);
        passwordConfirm = findViewById(R.id.editTextPassword2);


        redirectToLogin.setOnClickListener(view -> {
            Intent in = new Intent(view.getContext(), Login.class);
            view.getContext().startActivity(in);
        });

        register.setOnClickListener(view -> {

            if(checkEnteredData()) {
                // TODO save aditional user info to database
                // firebase auth - registers user only with email and password
                auth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                        .addOnCompleteListener(Register.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                Toast.makeText(Register.this, "createUserWithEmail:onComplete:" + task.isSuccessful(), Toast.LENGTH_SHORT).show();
                                if (!task.isSuccessful()) {
                                    Toast.makeText(Register.this, "Authentication failed." + task.getException(),
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    Intent in = new Intent(view.getContext(), MainMenu.class);
                                    view.getContext().startActivity(in);
                                }
                            }
                        });

            }

        });

    }

    boolean checkEnteredData() {
        boolean valid = true;

        if (firstName == null || firstName.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter your first name.", Toast.LENGTH_SHORT).show();
            valid = false;
        } else if (firstName.getText().length() < 3 || firstName.getText().length() > 20  /*|| !NAME_PATTERN.matcher(firstName.getText().toString()).matches()*/) {
            Toast.makeText(this, "Please enter valid first name.", Toast.LENGTH_SHORT).show();
            valid = false;
        } else if (lastName == null || lastName.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter your last name.", Toast.LENGTH_SHORT).show();
            valid = false;
        } else if (lastName.getText().length() < 3 || firstName.getText().length() > 20 /*|| !NAME_PATTERN.matcher(lastName.getText().toString()).matches()*/) {
            Toast.makeText(this, "Please enter valid last name.", Toast.LENGTH_SHORT).show();
            valid = false;
        } else if (email == null || email.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter your e-mail address.", Toast.LENGTH_SHORT).show();
            valid = false;
        } else if (password == null || password.getText().toString().isEmpty() || passwordConfirm == null || passwordConfirm.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter password two times.", Toast.LENGTH_SHORT).show();
            valid = false;
        } else if (!password.getText().toString().equals(passwordConfirm.getText().toString())) {
            Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
            valid = false;
        } else if (!PASSWORD_PATTERN.matcher(password.getText().toString()).matches()) {
            Toast.makeText(this, "Password is not valid! It should contain uppercase letter, lowercase letter, one number, minimum 8 characters without whitespaces.", Toast.LENGTH_SHORT).show();
            valid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()) {
            Toast.makeText(this, "E-mail address is not valid!", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        return valid;

    }
}