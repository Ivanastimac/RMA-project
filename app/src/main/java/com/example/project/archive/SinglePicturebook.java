package com.example.project.archive;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.project.R;
import com.example.project.model.DatabasePage;
import com.example.project.model.Page;
import com.example.project.model.Picturebook;
import com.example.project.model.Status;
import com.example.project.picturebook.NewPicturebook;
import com.example.project.picturebook.PagesAdapter;
import com.example.project.user_profile.Login;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
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
    ArrayList<Page> pages;
    Picturebook picturebook;
    String picturebookId;
    DatabasePage dbPage;
    ArrayList<DatabasePage> dbPages;

    FirebaseAuth auth;
    FirebaseUser loggedInUser;
    DatabaseReference database;
    FirebaseDatabase databaseIns;
    FirebaseStorage storageIns;
    StorageReference storageRef;

    final long ONE_MEGABYTE = 1024 * 1024;

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
        pages = new ArrayList<>();
        dbPages = new ArrayList<>();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        auth = FirebaseAuth.getInstance();
        loggedInUser = auth.getCurrentUser();
        databaseIns = FirebaseDatabase.getInstance();
        storageIns = FirebaseStorage.getInstance();

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

        editBtn.setOnClickListener(view -> {
            Intent in = new Intent(this, NewPicturebook.class);
            in.putExtra("picturebookId", picturebookId);
            startActivity(in);
        });
    }

    void init() {

        picturebookId = getIntent().getStringExtra("picturebookId");

        database = databaseIns.getReference("/picturebooks");
        database.child(picturebookId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                picturebook = dataSnapshot.getValue(Picturebook.class);
                title.setText(picturebook.getTitle());
                summary.setText(picturebook.getSummary());
                status.setText(picturebook.getStatus().toString());
                if (picturebook.getStatus().toString().equals("PRIVATE")) {
                    // if picturebook is private, hide button for making it private
                    privateBtn.setVisibility(View.GONE);
                } else {
                    // if picturebook is published, hide buttons for making it public and edit button
                    publishBtn.setVisibility(View.GONE);
                    editBtn.setVisibility(View.GONE);
                    deleteBtn.setVisibility(View.GONE);
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
        pAdapter.setImages(pages);

        database = databaseIns.getReference("/pages");
        database.orderByChild("picturebookId").equalTo(picturebookId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    pages.clear();
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        dbPage = ds.getValue(DatabasePage.class);
                        dbPage.setId(ds.getKey());
                        dbPages.add(dbPage);
                    }
                }
                getPages();
                database.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(SinglePicturebook.this, "Failed to read value." + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    void getPages() {
        for (DatabasePage page : dbPages) {
            storageRef = storageIns.getReference().child("images/pages/" + picturebookId + "/" + page.getId());
            storageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    pages.add(new Page(page.getId(), BitmapFactory.decodeByteArray(bytes,0, bytes.length)));
                    pAdapter.notifyDataSetChanged();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(SinglePicturebook.this, "Failed to load image.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    void publish() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.publish_picturebook_dialog)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        database = databaseIns.getReference("/picturebooks");
                        database.child("/" + picturebookId).child("status").setValue(Status.PUBLISHED);
                        editBtn.setVisibility(View.GONE);
                        publishBtn.setVisibility(View.GONE);
                        privateBtn.setVisibility(View.VISIBLE);
                        deleteBtn.setVisibility(View.GONE);
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
                        database = databaseIns.getReference("/picturebooks");
                        database.child("/" + picturebookId).child("status").setValue(Status.PRIVATE);
                        editBtn.setVisibility(View.VISIBLE);
                        publishBtn.setVisibility(View.VISIBLE);
                        privateBtn.setVisibility(View.GONE);
                        deleteBtn.setVisibility(View.VISIBLE);
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
                        database = databaseIns.getReference("/pages");
                        database.orderByChild("picturebookId").equalTo(picturebookId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                        dbPage = ds.getValue(DatabasePage.class);
                                        dbPage.setId(ds.getKey());
                                        dbPages.add(dbPage);
                                        ds.getRef().removeValue();
                                    }
                                }
                                removePages(view);
                                database.removeEventListener(this);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) { }
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

    void removePages(View view) {

        for (DatabasePage page : dbPages) {
            storageRef = storageIns.getReference().child("images/pages/" + picturebookId + "/" + page.getId());
            storageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(SinglePicturebook.this, "Deleted!", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(SinglePicturebook.this, "Unsuccessful!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        database = databaseIns.getReference("/picturebooks");
        database.child("/" + picturebookId).removeValue();
        Toast.makeText(SinglePicturebook.this, "Picturebook is deleted!", Toast.LENGTH_SHORT).show();
        Intent in = new Intent(view.getContext(), MyArchive.class);
        view.getContext().startActivity(in);

    }

}