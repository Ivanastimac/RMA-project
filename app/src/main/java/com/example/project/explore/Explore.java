package com.example.project.explore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.project.R;
import com.example.project.model.DatabasePage;
import com.example.project.model.ExploreRow;
import com.example.project.model.Picturebook;
import com.example.project.model.User;
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
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;


public class Explore extends AppCompatActivity {

    ArrayList<ExploreRow> rows;
    ArrayList<Picturebook> picturebooks;
    RecyclerView rv;
    ExploreRow row;
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
    ExplorePicturebookAdapter pAdapter;
    Picturebook picturebook;
    final long ONE_MEGABYTE = 1024 * 1024;
    private static final String TAG = "Explore Activity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);

        searchView = findViewById(R.id.search_bar_explore);
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
        rv = findViewById(R.id.recyclerViewExplore);

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
        pAdapter = new ExplorePicturebookAdapter(this);
        rv.setAdapter(pAdapter);
        pAdapter.setPicturebooks(rows);

        database = databaseIns.getReference("/picturebooks");
        database.orderByChild("status").equalTo("PUBLISHED").addValueEventListener(new ValueEventListener() {
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
                Toast.makeText(Explore.this, "Failed to read picturebooks." + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterList(String text) {
        List<ExploreRow> filteredList = new ArrayList<>();
        for (ExploreRow pic : rows) {
            if (pic.getTitle().toLowerCase().contains(text.toLowerCase()) || pic.getAuthorName().toLowerCase().contains(text.toLowerCase())){
                filteredList.add(pic);
            }
        }

        if (filteredList.isEmpty()) {
            Toast.makeText(Explore.this, "No matches found", Toast.LENGTH_SHORT).show();
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
                                Log.i(TAG, "IME u on success: "+ authorName);
                                String userId = pc.getUserId();

                                following = false;
                                database = databaseIns.getReference("/users/" + loggedInUser.getUid() + "/following");
                                database.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                                if (ds.getValue().equals(userId)) {
                                                    following = true;
                                                }
                                            }
                                        }
                                        database.removeEventListener(this);
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

                                        row = new ExploreRow(pc.getId(), pc.getTitle(), BitmapFactory.decodeByteArray(bytes, 0, bytes.length), authorName, following);
                                        rows.add(row);
                                        Collections.sort(rows);
                                        pAdapter.notifyDataSetChanged();
                                        Log.i(TAG, "IME: "+ authorName);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                Toast.makeText(Explore.this, "Failed to load page.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(Explore.this, "Failed to load picturebook.", Toast.LENGTH_SHORT).show();
                }

            });
        }
    }

}
