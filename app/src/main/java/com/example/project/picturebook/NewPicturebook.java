package com.example.project.picturebook;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.project.MainMenu;
import com.example.project.R;
import com.example.project.model.Picturebook;
import com.example.project.user_profile.Login;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;

public class NewPicturebook extends AppCompatActivity {

    EditText title;
    EditText summary;
    Button savePicturebookBtn;
    Button discardPicturebookBtn;
    Button addPageBtn;
    String picturebookId;
    String titleSF;
    String summarySF;

    FirebaseAuth auth;
    FirebaseUser loggedInUser;
    DatabaseReference database;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    ArrayList<Uri> filePaths;
    FirebaseStorage storage;
    StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_picturebook);

        title = findViewById(R.id.editTextTitle);
        summary = findViewById(R.id.editTextSummary);
        savePicturebookBtn = findViewById(R.id.buttonSavePicturebook);
        discardPicturebookBtn = findViewById(R.id.buttonDiscardPicturebook);
        addPageBtn = findViewById(R.id.buttonAddPage);
        filePaths = new ArrayList<Uri>();

        auth = FirebaseAuth.getInstance();
        loggedInUser = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance().getReference("/picturebooks");

        // if user is not logged in, redirect to login page
        if (loggedInUser == null) {
            Intent in = new Intent(this, Login.class);
            startActivity(in);
        }

        init();

        addPageBtn.setOnClickListener(view -> {
            // save title and summary to shared preferences
            editor = sharedPref.edit();
            editor.putString(getString(R.string.title), title.getText().toString());
            editor.putString(getString(R.string.summary), summary.getText().toString());
            editor.apply();

            choosePages();
        });

        savePicturebookBtn.setOnClickListener(view -> {
            if (checkData()) {
                Picturebook picturebook = new Picturebook(loggedInUser.getUid(), title.getText().toString(), summary.getText().toString());
                database.push().setValue(picturebook);
                picturebookId = database.getKey();
                if (picturebookId != null) {
                    savePages();
                    Toast.makeText(NewPicturebook.this, "Picture Book successfully saved!", Toast.LENGTH_SHORT).show();
                    Intent in = new Intent(this, MainMenu.class);
                    startActivity(in);
                }
            }
        });

        discardPicturebookBtn.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.discard_picturebook_dialog)
                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            discardPicturebook();
                            Intent in = new Intent(view.getContext(), MainMenu.class);
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

    void init() {
        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        titleSF = sharedPref.getString(getString(R.string.title), null);
        summarySF = sharedPref.getString(getString(R.string.summary), null);

        if (titleSF != null) {
            title.setText(titleSF);
        }

        if (summarySF != null) {
            summary.setText(summarySF);
        }
    }

    boolean checkData() {
        boolean valid = true;
        if (title == null || title.getText().toString().isEmpty()) {
            valid = false;
        } else if (summary == null || summary.getText().toString().isEmpty()) {
            valid = false;
        }

        return valid;
    }

    void choosePages() {
        Intent i = new Intent();
        i.setType("image/*");
        i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        i.setAction(Intent.ACTION_GET_CONTENT);
        launchSomeActivity.launch(i);
    }

    ActivityResultLauncher<Intent> launchSomeActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null && data.getData() != null) {
                        filePaths.add(data.getData());
                    }
                }
            });

    void savePages() {
        storage = FirebaseStorage.getInstance();

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading...");
        progressDialog.show();

        int i = 0;
        for (Uri filePath : filePaths) {
            storageRef = storage.getReference().child("images/pages/" + loggedInUser.getUid() + "/" + i);
            i++;
            storageRef.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //progressDialog.dismiss();
                            Toast.makeText(NewPicturebook.this, "Page successfully uploaded!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //progressDialog.dismiss();
                            Toast.makeText(NewPicturebook.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
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
        progressDialog.dismiss();
    }

    void discardPicturebook() {
        // delete title and summary from shared preferences
        editor.remove(getString(R.string.title));
        editor.remove(getString(R.string.summary));
        editor.apply();
    }


}