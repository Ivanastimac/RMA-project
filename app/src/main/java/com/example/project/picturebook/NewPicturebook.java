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
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.project.MainMenu;
import com.example.project.R;
import com.example.project.archive.SinglePicturebook;
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
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class NewPicturebook extends AppCompatActivity {

    EditText title;
    EditText summary;
    Button savePicturebookBtn;
    Button discardPicturebookBtn;
    Button addPageBtn;
    String picturebookId;

    FirebaseAuth auth;
    FirebaseUser loggedInUser;
    DatabaseReference database;

    ArrayList<Uri> filePaths;
    FirebaseStorage storage;
    StorageReference storageRef;

    RecyclerView rv;
    ArrayList<Bitmap> images;
    PagesAdapter pAdapter;

    boolean editMode;
    int numOfPages;

    final long ONE_MEGABYTE = 1024 * 1024;

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
        editMode = false;
        numOfPages = 0;

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        auth = FirebaseAuth.getInstance();
        loggedInUser = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance().getReference("/picturebooks");
        storage = FirebaseStorage.getInstance();

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
            } else {
                Toast.makeText(NewPicturebook.this, "You have to provide title, summary and at least one page for picture book!", Toast.LENGTH_SHORT).show();
            }
        });

        discardPicturebookBtn.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.discard_picturebook_dialog)
                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
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

        picturebookId = getIntent().getStringExtra("picturebookId");
        if (picturebookId != null) {
            editMode = true;
        }
        rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        pAdapter = new PagesAdapter(this);
        rv.setAdapter(pAdapter);
        pAdapter.setImages(images);

        // if we are editing existing picturebook, load it from database
        if (editMode) {
            loadData();
        }
    }

    void loadData() {

        database.child(picturebookId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Picturebook picturebook = dataSnapshot.getValue(Picturebook.class);
                title.setText(picturebook.getTitle());
                summary.setText(picturebook.getSummary());
                database.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(NewPicturebook.this, "Failed to read value." + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        storageRef = storage.getReference().child("images/pages/" + picturebookId);

        storageRef.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                for (StorageReference file : listResult.getItems()) {
                    file.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            images.add(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                            numOfPages++;
                            pAdapter.notifyDataSetChanged();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Toast.makeText(NewPicturebook.this, "Failed to load image." + exception.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }
        });
    }

    // last image chosen from internal storage add to array of bitmaps
    public void AddImagesFromGalleryToRecyclerViewArrayList()
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
                AddImagesFromGalleryToRecyclerViewArrayList();
            });

    void savePicturebook() {

        Picturebook picturebook = new Picturebook(loggedInUser.getUid(), title.getText().toString(), summary.getText().toString());

        if (editMode) {
            database.child(picturebookId).setValue(picturebook);
        } else {
            database.push().setValue(picturebook);
        }

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
                int i = numOfPages;
                for (Uri filePath : filePaths) {
                    storageRef = storage.getReference().child("images/pages/" + picturebookId + "/" + i);
                    i++;
                    Bitmap bm = null;
                    try {
                        bm = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    bm.compress(Bitmap.CompressFormat.JPEG, 5, bytes);
                    byte[] reducedImage = bytes.toByteArray();
                    storageRef.putBytes(reducedImage)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    // when all pages are stored, redirect to main menu
                                    if (filePath.toString().equals(filePaths.get(filePaths.size() - 1).toString())) {
                                        Toast.makeText(NewPicturebook.this, "Picture Book successfully saved!", Toast.LENGTH_SHORT).show();
                                        if (editMode) {
                                            Intent in = new Intent(getApplicationContext(), SinglePicturebook.class);
                                            in.putExtra("picturebookId", picturebookId);
                                            startActivity(in);
                                        } else {
                                            Intent in = new Intent(getApplicationContext(), MainMenu.class);
                                            startActivity(in);
                                        }
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
    
}