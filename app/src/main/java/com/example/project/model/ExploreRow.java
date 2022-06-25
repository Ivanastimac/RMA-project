package com.example.project.model;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.project.explore.ExploreSinglePicturebook;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.sql.Struct;
import java.util.ArrayList;

public class ExploreRow {
    private String id;
    private String title;
    private Bitmap firstPage;
    private String authorName;
    Picturebook picturebook;
    User user;
    String picturebookId;
    String picturebookAuthorId;
    DatabaseReference database;
    DatabaseReference database2;
    private static final String TAG = "Explore Activity";

    public ExploreRow(String id, String title, Bitmap image, String author) {
        this.id = id;
        this.title = title;
        this.firstPage = image;
        this.authorName = author;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) { this.id = id; }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Bitmap getFirstPage() { return firstPage; }

    public void setFirstPage(Bitmap image) { firstPage = image; }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }
}