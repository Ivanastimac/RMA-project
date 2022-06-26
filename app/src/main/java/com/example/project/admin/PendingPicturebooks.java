package com.example.project.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.project.R;
import com.example.project.model.DatabasePage;
import com.example.project.model.Picturebook;
import com.example.project.model.User;
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
import java.util.List;

public class PendingPicturebooks extends AppCompatActivity {

    ArrayList<AdminRow> rows;
    ArrayList<Picturebook> picturebooks;
    RecyclerView rv;
    AdminRow row;
    SearchView searchView;
    String picturebookId;
    String picturebookAuthorId;
    String authorName;
    User user;
    boolean following;

    FirebaseAuth auth;
    FirebaseUser loggedInUser;
    DatabaseReference database;
    DatabaseReference database2;
    DatabasePage dbPage;
    String pageId;
    FirebaseDatabase databaseIns;
    FirebaseStorage storageIns;
    StorageReference storageRef;
    PendingPicturebooksAdapter pAdapter;
    Picturebook picturebook;
    final long ONE_MEGABYTE = 1024 * 1024;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_picturebooks);

        searchView = findViewById(R.id.search_bar_admin);
        searchView.clearFocus();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterList(newText);
                return true;
            }
        });

        rows = new ArrayList<>();
        picturebooks = new ArrayList<>();
        rv = findViewById(R.id.recyclerViewAdmin);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        auth = FirebaseAuth.getInstance();
        loggedInUser = auth.getCurrentUser();
        databaseIns = FirebaseDatabase.getInstance();
        storageIns = FirebaseStorage.getInstance();

        if (loggedInUser == null) {
            Intent in = new Intent(this, Login.class);
            startActivity(in);
        }

        rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        pAdapter = new PendingPicturebooksAdapter(this);
        rv.setAdapter(pAdapter);
        pAdapter.setPicturebooks(rows);

        database = databaseIns.getReference("/picturebooks");
        database.orderByChild("status").equalTo("PENDING").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    rows.clear();
                    for (DataSnapshot pc : dataSnapshot.getChildren()) {
                        picturebook = pc.getValue(Picturebook.class);
                        picturebook.setId(pc.getKey());
                        picturebooks.add(picturebook);
                    }
                    getFirstPage(picturebooks);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PendingPicturebooks.this, "Failed to read picturebooks." + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterList(String text) {
        List<AdminRow> filteredList = new ArrayList<>();
        for (AdminRow pic : rows) {
            if (pic.getTitle().toLowerCase().contains(text.toLowerCase()) || pic.getAuthorName().toLowerCase().contains(text.toLowerCase())){
                filteredList.add(pic);
            }
        }

        if (filteredList.isEmpty()) {
            Toast.makeText(PendingPicturebooks.this, "No matches found", Toast.LENGTH_SHORT).show();
        } else {
            pAdapter.setFilteredList(filteredList);
        }
    }

    void getFirstPage(ArrayList<Picturebook> picturebooks) {

        for (Picturebook pc : picturebooks) {
            database = databaseIns.getReference("/pages");
            database.orderByChild("picturebookId").equalTo(pc.getId()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot pc : dataSnapshot.getChildren()) {
                            dbPage = pc.getValue(DatabasePage.class);
                            // if pages have page numbers, find first page, else - display random page as first page
                            if (dbPage.getNum() == 1) {
                                pageId = pc.getKey();
                                break;
                            }
                            pageId = pc.getKey();
                        }

                        storageRef = storageIns.getReference().child("images/pages/" + pc.getId() + "/" + pageId);
                        storageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                            @Override
                            public void onSuccess(byte[] bytes) {
                                String userId = pc.getUserId();

                                following = false;
                                database = databaseIns.getReference("/users/" + loggedInUser.getUid() + "/following");
                                database.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                                if (ds.getValue().equals(picturebookAuthorId)) {
                                                    following = true;
                                                }
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) { }
                                });

                                database2 = FirebaseDatabase.getInstance().getReference("/users");

                                //database = FirebaseDatabase.getInstance().getReference("/picturebooks");
                                database2.child(userId).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        user = dataSnapshot.getValue(User.class);
                                        authorName = user.getFirstName() + ' ' + user.getLastName();

                                        row = new AdminRow(pc.getId(), pc.getTitle(), BitmapFactory.decodeByteArray(bytes, 0, bytes.length), authorName, pc.getStatus().toString());
                                        rows.add(row);
                                        //pAdapter.notifyDataSetChanged();
                                        pAdapter.notifyItemRangeChanged(0, rows.size());
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                Toast.makeText(PendingPicturebooks.this, "Failed to load page.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(PendingPicturebooks.this, "Failed to load picturebook.", Toast.LENGTH_SHORT).show();
                }

            });
        }
    }

}