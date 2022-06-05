package com.example.project.archive;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.ContentResolver;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.project.R;
import com.example.project.model.Picturebook;
import com.example.project.model.Status;
import com.example.project.model.User;
import com.example.project.picturebook.PagesAdapter;
import com.example.project.user_profile.ChangeProfilePicture;
import com.example.project.user_profile.Login;
import com.example.project.user_profile.Settings;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class SinglePicturebook extends AppCompatActivity {

    TextView title;
    TextView summary;
    TextView status;
    Button publishBtn;
    Button privateBtn;
    Button deleteBtn;
    ImageView editBtn;
    RecyclerView rv;
    PagesAdapter pAdapter;
    ArrayList<Bitmap> images;
    Picturebook picturebook;
    String id;

    FirebaseAuth auth;
    FirebaseUser loggedInUser;
    DatabaseReference database;
    FirebaseStorage storage;
    StorageReference storageRef;

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_picturebook);

        title = findViewById(R.id.textViewTitleArchive);
        summary = findViewById(R.id.textViewSummaryArchive);
        status = findViewById(R.id.textViewStatus);
        publishBtn = findViewById(R.id.buttonPublish);
        privateBtn = findViewById(R.id.buttonPrivate);
        deleteBtn = findViewById(R.id.buttonDeletePicturebook);
        editBtn = findViewById(R.id.imageButtonEditPicturebook);
        rv = findViewById(R.id.recyclerViewPagesArchive);
        images = new ArrayList<>();

        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        storage = FirebaseStorage.getInstance();

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

        publishBtn.setOnClickListener(view -> {
            publish();
        });

        privateBtn.setOnClickListener(view -> {
            makePrivate();
        });

        deleteBtn.setOnClickListener(view -> {
            deletePicturebook(view);
        });
    }

    void init() {

        id = getIntent().getStringExtra("picturebookId");
        if (id != null || !id.isEmpty()) {
            editor = sharedPref.edit();
            editor.putString(getString(R.string.picturebook_id), getIntent().getStringExtra("picturebookId"));
            editor.apply();
        }

        database.child(sharedPref.getString(getString(R.string.picturebook_id), null)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                picturebook = dataSnapshot.getValue(Picturebook.class);
                title.setText(picturebook.getTitle());
                summary.setText(picturebook.getSummary());
                status.setText(picturebook.getStatus().toString());
                if (picturebook.getStatus().toString().equals("PRIVATE")) {
                    privateBtn.setVisibility(View.GONE);
                } else {
                    publishBtn.setVisibility(View.GONE);
                    editBtn.setVisibility(View.GONE);
                }
                database.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(SinglePicturebook.this, "Failed to read value." + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        pAdapter = new PagesAdapter(this);
        rv.setAdapter(pAdapter);
        pAdapter.setImages(images);

        storageRef = storage.getReference().child("images/pages/" + sharedPref.getString(getString(R.string.picturebook_id), null));

        final long ONE_MEGABYTE = 1024 * 1024;
        storageRef.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                for (StorageReference file : listResult.getItems()) {
                    file.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            images.add(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                            pAdapter.notifyDataSetChanged();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Toast.makeText(SinglePicturebook.this, "Failed to load image." + exception.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }
        });
    }

    void publish() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.publish_picturebook_dialog)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        database.child("/" + sharedPref.getString(getString(R.string.picturebook_id), null)).child("status").setValue(Status.PUBLISHED);
                        editBtn.setVisibility(View.GONE);
                        publishBtn.setVisibility(View.GONE);
                        privateBtn.setVisibility(View.VISIBLE);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // do nothing
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    void makePrivate() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.private_picturebook_dialog)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        database.child("/" + sharedPref.getString(getString(R.string.picturebook_id), null)).child("status").setValue(Status.PRIVATE);
                        editBtn.setVisibility(View.VISIBLE);
                        publishBtn.setVisibility(View.VISIBLE);
                        privateBtn.setVisibility(View.GONE);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // do nothing
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    void deletePicturebook(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_single_picturebook_dialog)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        storage = FirebaseStorage.getInstance();
                        storageRef = storage.getReference().child("images/pages/" + sharedPref.getString(getString(R.string.picturebook_id), null));
                        storageRef.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
                            @Override
                            public void onSuccess(ListResult listResult) {
                                for (StorageReference file : listResult.getItems()) {
                                    file.delete();
                                }
                                database.child("/" + sharedPref.getString(getString(R.string.picturebook_id), null)).removeValue();
                                editor.remove(getString(R.string.picturebook_id));
                                editor.apply();
                                Toast.makeText(SinglePicturebook.this, "Picturebook is deleted!", Toast.LENGTH_SHORT).show();
                                Intent in = new Intent(view.getContext(), MyArchive.class);
                                view.getContext().startActivity(in);
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
    }
}