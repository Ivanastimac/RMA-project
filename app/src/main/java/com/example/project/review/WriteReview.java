package com.example.project.review;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.project.R;
import com.example.project.model.DatabasePage;
import com.example.project.model.PicturebookCoverImage;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

public class WriteReview extends AppCompatActivity {

    private String picturebooksId;
    ImageView picturebookCover;
    TextView picturebookName;
    RatingBar ratingBar;
    EditText reviewText;
    FloatingActionButton submitReview;
    FirebaseAuth firebaseAuth;

    DatabasePage dbPage;
    String pageId;
    FirebaseStorage storageIns;
    StorageReference storageRef;
    private static final String TAG = "WriteReview";

    final long ONE_MEGABYTE = 1024 * 1024;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_review);

        picturebookCover = findViewById(R.id.picturebookCover);
        picturebookName = findViewById(R.id.picturebookName);
        ratingBar = findViewById(R.id.ratingBar);
        reviewText = findViewById(R.id.reviewEt);
        submitReview = findViewById(R.id.submitReview);

        picturebooksId = getIntent().getStringExtra("picturebooksId");

        firebaseAuth = FirebaseAuth.getInstance();
        loadPicturebookInfo();
        loadMyReview();

        submitReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputData();
            }
        });

    }

    private void loadPicturebookInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("/users");
        DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference("/picturebooks");
        DatabaseReference ref3 = FirebaseDatabase.getInstance().getReference("/pages");
        ref.child(picturebooksId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                ref2.child(picturebooksId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String picturebookNames = ""+snapshot.child("title").getValue();
                        picturebookName.setText(picturebookNames);

                        //TODO get first page (cover page to display)
                        ref3.orderByChild("picturebookId").equalTo(picturebooksId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String pageNumber = ""+snapshot.child("num").getValue();
                                if(snapshot.exists()) {
                                    for (DataSnapshot pc : snapshot.getChildren()) {
                                        dbPage = pc.getValue(DatabasePage.class);
                                        // if pages have page numbers, find first page, else - display random page as first page
                                        if (dbPage.getNum() == 1) {
                                            pageId = pc.getKey();
                                            break;
                                        }
                                        pageId = pc.getKey();
                                    }
                                    Log.i(TAG, "index=" + picturebooksId);
                                    Log.i(TAG, "Page id = " + pageId);
                                    storageRef = FirebaseStorage.getInstance().getReference().child("images/pages/" + picturebooksId + "/" + pageId);
                                    Log.i(TAG, "index=" + storageRef);
                                    storageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                        @Override
                                        public void onSuccess(byte[] bytes) {
                                            PicturebookCoverImage picturebookImage;
                                            picturebookImage = new PicturebookCoverImage(picturebooksId, BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                                            picturebookCover.setImageBitmap(picturebookImage.getFirstPage());
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception exception) {
                                            Toast.makeText(WriteReview.this, "Failed to load page.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


                /*try {
                    Picasso.get().load(picturebookImage).placeholder(R.drawable.ic_baseline_menu_book_24).into(picturebookCover);
                }catch (Exception e){
                    picturebookCover.setImageResource(R.drawable.ic_baseline_menu_book_24);
                }*/
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadMyReview() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("/picturebooks");
        ref.child(picturebooksId).child("/ratings").child(firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String uid = ""+snapshot.child("uid").getValue();
                    String ratings = ""+snapshot.child("ratings").getValue();
                    String review = ""+snapshot.child("review").getValue();
                    String timestamp = ""+snapshot.child("timestamp").getValue();

                    float myRating = Float.parseFloat(ratings);
                    ratingBar.setRating(myRating);
                    reviewText.setText(review);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void inputData() {
        String ratings = ""+ratingBar.getRating();
        String review = reviewText.getText().toString().trim();
        String timestamp = ""+System.currentTimeMillis();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", ""+ firebaseAuth.getUid());
        hashMap.put("ratings", ""+ ratings);
        hashMap.put("review", ""+ review);
        hashMap.put("timestamp", ""+ timestamp);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("/picturebooks");
        ref.child(picturebooksId).child("/ratings").child(firebaseAuth.getUid()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(WriteReview.this,"Review published successfully...", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(WriteReview.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }
}