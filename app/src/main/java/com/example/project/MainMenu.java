package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import com.example.project.picturebook.NewPicturebook;
import com.example.project.user_profile.Login;
import com.example.project.user_profile.Settings;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class MainMenu extends AppCompatActivity {


    Button newPicturebook;
    ImageView profileBtn;
    Bitmap image;

    FirebaseAuth auth;
    FirebaseUser loggedInUser;
    FirebaseStorage storage;
    StorageReference storageRef;

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

        newPicturebook = findViewById(R.id.buttonNew);
        profileBtn = findViewById(R.id.imageButtonProfile);

        checkProfilePicture();


        profileBtn.setOnClickListener(view -> {
            Intent in = new Intent(view.getContext(), Settings.class);
            view.getContext().startActivity(in);
        });

        newPicturebook.setOnClickListener(view -> {
            Intent in = new Intent(view.getContext(), NewPicturebook.class);
            view.getContext().startActivity(in);
        });
    }

    void checkProfilePicture() {
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference().child("images/" + loggedInUser.getUid());
        //Toast.makeText(MainMenu.this, storageRef.toString(), Toast.LENGTH_LONG).show();

        final long ONE_MEGABYTE = 1024 * 1024;
        storageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                image = BitmapFactory.decodeByteArray(bytes,0, bytes.length);
                profileBtn.setImageBitmap(image);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                profileBtn.setImageDrawable(getResources().getDrawable(R.drawable.profile));
            }
        });

    }
}