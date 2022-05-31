package com.example.project.picturebook;

import androidx.activity.ComponentActivity;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
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

    RecyclerView rv;
    ArrayList<Bitmap> images;
    PagesAdapter pAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_picturebook);

        title = findViewById(R.id.editTextTitle);
        summary = findViewById(R.id.editTextSummary);
        savePicturebookBtn = findViewById(R.id.buttonSavePicturebook);
        discardPicturebookBtn = findViewById(R.id.buttonDiscardPicturebook);
        addPageBtn = findViewById(R.id.buttonAddPage);
        rv = findViewById(R.id.recyclerViewPages);
        filePaths = new ArrayList<>();
        images = new ArrayList<>();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
            choosePages();
        });

        savePicturebookBtn.setOnClickListener(view -> {
            if (checkData()) {
                savePicturebook();
                // TODO
                // deleteFromSP();
            } else {
                Toast.makeText(NewPicturebook.this, "You have to provide title, summary and at least one page for picture book!", Toast.LENGTH_SHORT).show();
            }
        });

        discardPicturebookBtn.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.discard_picturebook_dialog)
                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // TODO
                            //deleteFromSP();
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
        // TODO save to SF on back button
        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        titleSF = sharedPref.getString(getString(R.string.title), null);
        summarySF = sharedPref.getString(getString(R.string.summary), null);

        if (titleSF != null) {
            title.setText(titleSF);
        }

        if (summarySF != null) {
            summary.setText(summarySF);
        }

        rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        pAdapter = new PagesAdapter(this);
        rv.setAdapter(pAdapter);
        pAdapter.setImages(images);
    }

    // last image chosen from gallery add to array of bitmaps
    public void AddItemsToRecyclerViewArrayList()
    {
        try {
            images.add(MediaStore.Images.Media.getBitmap(
                    this.getContentResolver(), filePaths.get(filePaths.size() - 1)
            ));
        } catch (IOException e) {
            e.printStackTrace();
        }
        // refresh recyclerview
        pAdapter.notifyDataSetChanged();
    }

    boolean checkData() {
        boolean valid = true;
        if (title == null || title.getText().toString().isEmpty()) {
            valid = false;
        } else if (summary == null || summary.getText().toString().isEmpty()) {
            valid = false;
        } else if (filePaths == null || filePaths.isEmpty()) {
            valid = false;
        }

        return valid;
    }

    // choose image from gallery
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
                AddItemsToRecyclerViewArrayList();
            });

    void savePicturebook() {

        Picturebook picturebook = new Picturebook(loggedInUser.getUid(), title.getText().toString(), summary.getText().toString());
        database.push().setValue(picturebook);

        // wait for creating new picturebook in database and than save pages to storage
        database.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                picturebookId = dataSnapshot.getKey();
                if (picturebookId != null) {
                    savePages();
                    // remove event listener so it wouldn't be called again after adding each page to storage
                    database.removeEventListener(this);
                }
            }

            void savePages() {
                storage = FirebaseStorage.getInstance();
                int i = 0;
                for (Uri filePath : filePaths) {
                    storageRef = storage.getReference().child("images/pages/" + picturebookId + "/" + i);
                    i++;
                    storageRef.putFile(filePath)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    // when all pages are stored, redirect to main menu
                                    if (filePath.toString().equals(filePaths.get(filePaths.size() - 1).toString())) {
                                        Toast.makeText(NewPicturebook.this, "Picture Book successfully saved!", Toast.LENGTH_SHORT).show();
                                        Intent in = new Intent(getApplicationContext(), MainMenu.class);
                                        startActivity(in);
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(NewPicturebook.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                    }

                }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {            }

        });
    }

    void saveToSP() {
        editor = sharedPref.edit();
        editor.putString(getString(R.string.title), title.getText().toString());
        editor.putString(getString(R.string.summary), summary.getText().toString());
        editor.apply();
    }

    void deleteFromSP() {
        editor.remove(getString(R.string.title));
        editor.remove(getString(R.string.summary));
        editor.apply();
    }
    
}