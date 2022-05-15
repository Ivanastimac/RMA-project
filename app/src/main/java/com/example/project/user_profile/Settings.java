package com.example.project.user_profile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
    EditText firstNameEdit;
    EditText lastNameEdit;
    EditText emailEdit;
    Button changePasswordBtn;
    Button deleteAccountBtn;
    Button signOutBtn;
    Button saveEditedBtn;
    ImageView editBtn;
    User user;

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

        firstName = findViewById(R.id.textViewFirstName);
        lastName = findViewById(R.id.textViewLastName);
        email = findViewById(R.id.textViewEmail);
        changePasswordBtn = findViewById(R.id.buttonChangePassword);
        deleteAccountBtn = findViewById(R.id.buttonDeleteAccount);
        signOutBtn = findViewById(R.id.buttonSignOut);
        editBtn = findViewById(R.id.imageButtonEditInfo);

        firstNameEdit = findViewById(R.id.editTextFirstName);
        lastNameEdit = findViewById(R.id.editTextLastName);
        emailEdit = findViewById(R.id.editTextEmail);
        saveEditedBtn = findViewById(R.id.buttonSaveEdited);

        // editviews for updating info, visible after clicking edit button
        firstNameEdit.setVisibility(View.GONE);
        lastNameEdit.setVisibility(View.GONE);
        emailEdit.setVisibility(View.GONE);
        saveEditedBtn.setVisibility(View.GONE);

        DatabaseReference database = FirebaseDatabase.getInstance().getReference("users");

        auth = FirebaseAuth.getInstance();
        loggedInUser = auth.getCurrentUser();

        // if user is not logged in, redirect to login page
        if (loggedInUser == null) {
            Intent in = new Intent(this, Login.class);
            startActivity(in);
        }

        email.setText(loggedInUser.getEmail());

        // get logged in user info and display it on screen
        database.child(loggedInUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                firstName.setText(user.getFirstName());
                lastName.setText(user.getLastName());
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(Settings.this, "Failed to read value." + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        editBtn.setOnClickListener(view -> {
                firstName.setVisibility(View.GONE);
                lastName.setVisibility(View.GONE);
                email.setVisibility((View.GONE));
                firstNameEdit.setVisibility(View.VISIBLE);
                lastNameEdit.setVisibility(View.VISIBLE);
                emailEdit.setVisibility(View.VISIBLE);
                saveEditedBtn.setVisibility(View.VISIBLE);
                firstNameEdit.setText(user.getFirstName());
                lastNameEdit.setText(user.getLastName());
                emailEdit.setText(loggedInUser.getEmail());
        });

        saveEditedBtn.setOnClickListener(view -> {

            if (checkEnteredData()) {
                // first update email through firebase auth method
                loggedInUser.updateEmail(emailEdit.getText().toString().trim())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(Settings.this, "Email address is updated.", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(Settings.this, "Failed to update email!", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                // update additional user info
                database.child(loggedInUser.getUid()).child("firstName").setValue(firstNameEdit.getText().toString());
                database.child(loggedInUser.getUid()).child("lastName").setValue(lastNameEdit.getText().toString());

                // return to user info display, without edit functionality
                firstNameEdit.setVisibility(View.GONE);
                lastNameEdit.setVisibility(View.GONE);
                emailEdit.setVisibility(View.GONE);
                saveEditedBtn.setVisibility(View.GONE);
                firstName.setVisibility(View.VISIBLE);
                lastName.setVisibility(View.VISIBLE);
                email.setVisibility(View.VISIBLE);
            }

        });

        changePasswordBtn.setOnClickListener(view -> {
            Intent in = new Intent(view.getContext(), ChangePassword.class);
            view.getContext().startActivity(in);
        });

        deleteAccountBtn.setOnClickListener(view -> {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.delete_account_dialog)
                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            String uid = loggedInUser.getUid();
                            loggedInUser.delete()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                database.child(uid).removeValue();
                                                Intent in = new Intent(view.getContext(), Login.class);
                                                view.getContext().startActivity(in);
                                                Toast.makeText(Settings.this, "Your profile is deleted:( Create a account now!", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(Settings.this, "Failed to delete your account!", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // do nothing
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();

        });

        signOutBtn.setOnClickListener(view -> {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.sign_out_dialog)
                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            auth.signOut();
                            Intent in = new Intent(view.getContext(), Login.class);
                            view.getContext().startActivity(in);
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // do nothing
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();

        });

    }

    boolean checkEnteredData() {

        boolean valid = true;

        if (firstNameEdit == null || firstNameEdit.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter your first name.", Toast.LENGTH_SHORT).show();
            valid = false;
        } else if (firstNameEdit.getText().length() < 3 || firstNameEdit.getText().length() > 20  /*|| !NAME_PATTERN.matcher(firstName.getText().toString()).matches()*/) {
            Toast.makeText(this, "Please enter valid first name.", Toast.LENGTH_SHORT).show();
            valid = false;
        } else if (lastNameEdit == null || lastNameEdit.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter your last name.", Toast.LENGTH_SHORT).show();
            valid = false;
        } else if (lastNameEdit.getText().length() < 3 || firstNameEdit.getText().length() > 20 /*|| !NAME_PATTERN.matcher(lastName.getText().toString()).matches()*/) {
            Toast.makeText(this, "Please enter valid last name.", Toast.LENGTH_SHORT).show();
            valid = false;
        } else if (emailEdit == null || emailEdit.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter new e-mail address.", Toast.LENGTH_SHORT).show();
            valid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(emailEdit.getText().toString()).matches()) {
            Toast.makeText(this, "E-mail address is not valid!", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        return valid;

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