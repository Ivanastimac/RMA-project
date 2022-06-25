package com.example.project.explore;

import androidx.appcompat.app.AppCompatActivity;
import com.example.project.R;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.project.archive.MyArchive;
import com.example.project.model.DatabasePage;
import com.example.project.model.Page;
import com.example.project.model.Picturebook;
import com.example.project.model.Review;
import com.example.project.model.Status;
import com.example.project.model.User;
import com.example.project.picturebook.NewPicturebook;
import com.example.project.review.WriteReview;
import com.example.project.showreviews.ReviewAdapter;
import com.example.project.showreviews.ShowReviews;
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

public class ExploreSinglePicturebook extends AppCompatActivity {

    TextView title;
    TextView summary;
    TextView status;
    TextView caption;
    Button follow;
    Button deleteBtn;
    RecyclerView rv;
    ExplorePagesAdapter pAdapter;
    ArrayList<Page> pages;
    Picturebook picturebook;
    User user;
    String picturebookId;
    DatabasePage dbPage;
    ArrayList<DatabasePage> dbPages;
    Boolean clickedFollow = false;
    String authorName;
    String picturebookAuthorId;
    private static final String TAG = "Explore Activity";
    ImageButton reviewBtn;
    ImageButton viewReviewsBtn;
    RatingBar ratingBar;

    FirebaseAuth auth;
    FirebaseUser loggedInUser;
    DatabaseReference database;
    DatabaseReference database2;
    FirebaseDatabase databaseIns;
    FirebaseDatabase databaseIns2;
    FirebaseStorage storageIns;
    FirebaseStorage storageIns2;
    StorageReference storageRef;
    StorageReference storageRef2;

    final long ONE_MEGABYTE = 1024 * 1024;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore_single_picturebook);

        title = findViewById(R.id.textViewTitleExplore);
        summary = findViewById(R.id.textViewSummaryExplore);
        status = findViewById(R.id.textViewStatusExplore);
        caption = findViewById(R.id.textCaption);
        deleteBtn = findViewById(R.id.buttonDeletePicturebook);
        follow = findViewById(R.id.follow_button);
        rv = findViewById(R.id.recyclerViewPagesExplore);
        reviewBtn = findViewById(R.id.addReviews);
        viewReviewsBtn = findViewById(R.id.reviewBtn);
        ratingBar =findViewById(R.id.ratingBar4);
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

        follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!clickedFollow){
                    follow.setText("Following");
                    clickedFollow = true;
                }else{
                    follow.setText("Follow");
                    clickedFollow = false;
                }
            }
        });

        picturebookId = getIntent().getStringExtra("picturebookId");

        reviewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ExploreSinglePicturebook.this, WriteReview.class);
                intent.putExtra("picturebooksId", picturebookId);
                startActivity(intent);
            }
        });

        viewReviewsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ExploreSinglePicturebook.this, ShowReviews.class);
                intent.putExtra("picturebookId", picturebookId);
                startActivity(intent);
            }
        });


        deleteBtn.setOnClickListener(view -> {
            deletePicturebook(view);
        });

        loadReviews();

    }

    private float ratingsSum = 0;
    private void loadReviews() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("/picturebooks");
        ref.child(picturebookId).child("/ratings").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ratingsSum = 0;

                for(DataSnapshot ds: snapshot.getChildren()){
                    float rating = Float.parseFloat(""+ds.child("ratings").getValue());

                    ratingsSum = ratingsSum + rating;
                }

                long numberOfReviews = snapshot.getChildrenCount();
                float avgRating = ratingsSum/numberOfReviews;

                ratingBar.setRating(avgRating);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    void init() {

        picturebookId = getIntent().getStringExtra("picturebookId");
        database2 = databaseIns.getReference("/users");

        database = databaseIns.getReference("/picturebooks");
        database.child(picturebookId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                picturebook = dataSnapshot.getValue(Picturebook.class);
                picturebookAuthorId = picturebook.getUserId();
                database2.child(picturebookAuthorId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        user = snapshot.getValue(User.class);
                        authorName = user.getFirstName() + ' ' + user.getLastName();
                        status.setText(authorName);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ExploreSinglePicturebook.this, "Failed to read value." + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

                title.setText(picturebook.getTitle());
                summary.setText(picturebook.getSummary());

                database.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ExploreSinglePicturebook.this, "Failed to read value." + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        pAdapter = new ExplorePagesAdapter(this);
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
                Toast.makeText(ExploreSinglePicturebook.this, "Failed to read value." + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    void getPages() {
        for (DatabasePage page : dbPages) {
            storageRef = storageIns.getReference().child("images/pages/" + picturebookId + "/" + page.getId());
            storageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    pages.add(new Page(page.getId(), BitmapFactory.decodeByteArray(bytes,0, bytes.length), page.getCaption(), page.getNum()));
                    pAdapter.notifyDataSetChanged();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(ExploreSinglePicturebook.this, "Failed to load image.", Toast.LENGTH_SHORT).show();
                }
            });
        }
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
                    Toast.makeText(ExploreSinglePicturebook.this, "Deleted!", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(ExploreSinglePicturebook.this, "Unsuccessful!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        database = databaseIns.getReference("/picturebooks");
        database.child("/" + picturebookId).removeValue();
        Toast.makeText(ExploreSinglePicturebook.this, "Picturebook is deleted!", Toast.LENGTH_SHORT).show();
        Intent in = new Intent(view.getContext(), MyArchive.class);
        view.getContext().startActivity(in);

    }
}