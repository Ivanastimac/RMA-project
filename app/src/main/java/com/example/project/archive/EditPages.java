package com.example.project.archive;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.example.project.R;
import com.example.project.model.DatabasePage;
import com.example.project.model.Page;
import com.example.project.model.Picturebook;
import com.example.project.picturebook.NewPicturebook;
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

public class EditPages extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseUser loggedInUser;
    FirebaseDatabase databaseIns;
    DatabaseReference database;
    FirebaseStorage storageIns;
    StorageReference storageRef;

    ArrayList<Page> pages;
    RecyclerView rv;
    PagesDetailsAdapter pdAdapter;
    String picturebookId;
    DatabasePage dbPage;
    Page page;
    ArrayList<DatabasePage> dbPages;
    Button saveBtn;

    final long ONE_MEGABYTE = 1024 * 1024;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_pages);

        rv = findViewById(R.id.recyclerViewEditPages);
        saveBtn = findViewById(R.id.buttonSavePagesDetails);
        pages = new ArrayList<>();
        dbPages = new ArrayList<>();

        auth = FirebaseAuth.getInstance();
        loggedInUser = auth.getCurrentUser();
        databaseIns = FirebaseDatabase.getInstance();
        storageIns = FirebaseStorage.getInstance();

        // if user is not logged in, redirect to login page
        if (loggedInUser == null) {
            Intent in = new Intent(this, Login.class);
            startActivity(in);
        }

        rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        pdAdapter = new PagesDetailsAdapter(this);
        rv.setAdapter(pdAdapter);
        pdAdapter.setPages(pages);

        picturebookId = getIntent().getStringExtra("picturebookId");

        database = databaseIns.getReference("/pages");
        database.orderByChild("picturebookId").equalTo(picturebookId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    pages.clear();
                    for (DataSnapshot pc : dataSnapshot.getChildren()) {
                        dbPage = pc.getValue(DatabasePage.class);
                        dbPage.setId(pc.getKey());
                        dbPages.add(dbPage);
                    }
                    pdAdapter.setNumPages(dbPages.size());
                    getPagesFromStorage();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(EditPages.this, "Failed to read pages." + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        saveBtn.setOnClickListener(view -> {
            saveEdited();
        });

    }

    void getPagesFromStorage() {
        for (DatabasePage page : dbPages) {
            storageRef = storageIns.getReference().child("images/pages/" + picturebookId + "/" + page.getId());
            storageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    pages.add(new Page(page.getId(), BitmapFactory.decodeByteArray(bytes,0, bytes.length)));
                    pdAdapter.notifyDataSetChanged();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(EditPages.this, "Failed to load image.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    void saveEdited() {
        database = databaseIns.getReference("/pages");
        for (Page page : pages) {
            DatabasePage dbPage = new DatabasePage(picturebookId, page.getCaption(), page.getNum());
            database.child(page.getId()).setValue(dbPage);
        }
        Intent in = new Intent(this, SinglePicturebook.class);
        in.putExtra("picturebookId", picturebookId);
        startActivity(in);
    }
}