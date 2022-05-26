package com.example.project.user_profile;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.project.MainActivity;
import com.example.project.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.UUID;

public class ChangeProfilePicture extends AppCompatActivity {

    Button choosePictureBtn;
    Button savePictureBtn;
    ImageView profilePicture;
    Bitmap image;

    Uri filePath;
    Bitmap selectedImageBitmap;

    FirebaseAuth auth;
    FirebaseUser loggedInUser;
    FirebaseStorage storage;
    StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_profile_picture);

        choosePictureBtn = findViewById(R.id.buttonChoosePicture);
        savePictureBtn = findViewById(R.id.buttonSavePicture);
        profilePicture = findViewById(R.id.imageProfilePicture);

        auth = FirebaseAuth.getInstance();
        loggedInUser = auth.getCurrentUser();

        // if user is not logged in, redirect to login page
        if (loggedInUser == null) {
            Intent in = new Intent(this, Login.class);
            startActivity(in);
        }

        checkProfilePicture();

        choosePictureBtn.setOnClickListener(view -> {
            chooseProfilePicture();
        });

        savePictureBtn.setOnClickListener(view -> {
            if (selectedImageBitmap == null) {
                Toast.makeText(ChangeProfilePicture.this, "Please choose picture.", Toast.LENGTH_SHORT).show();
            } else {
                uploadProfilePicture();
            }
        });
    }

    // choose profile picture from gallery
    void chooseProfilePicture() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);

        launchSomeActivity.launch(i);
    }

    // get result from gallery and display image
    ActivityResultLauncher<Intent> launchSomeActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null && data.getData() != null) {
                        filePath = data.getData();
                        try {
                            selectedImageBitmap = MediaStore.Images.Media.getBitmap(
                                    this.getContentResolver(), filePath
                            );
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                        profilePicture.setImageBitmap(selectedImageBitmap);
                    }
                }
            });

    // save profile picture in storage
    void uploadProfilePicture() {
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading...");
        progressDialog.show();

        storageRef = storageRef.child("images/"+ loggedInUser.getUid());
        storageRef.putFile(filePath)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressDialog.dismiss();
                        Toast.makeText(ChangeProfilePicture.this, "Profile picture successfully uploaded!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(ChangeProfilePicture.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                .getTotalByteCount());
                        progressDialog.setMessage("Uploaded "+(int)progress+"%");
                    }
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
                profilePicture.setImageBitmap(image);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                profilePicture.setImageDrawable(getResources().getDrawable(R.drawable.profile));
            }
        });

    }
}