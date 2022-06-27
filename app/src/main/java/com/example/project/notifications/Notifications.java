package com.example.project.notifications;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.project.R;
import com.example.project.model.DatabasePage;
import com.example.project.model.Picturebook;
import com.example.project.model.PicturebookCoverImage;
import com.example.project.model.Review;
import com.example.project.showreviews.ReviewAdapter;
import com.example.project.showreviews.ShowReviews;
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

public class Notifications extends AppCompatActivity {

    private String userId;
    private RecyclerView notificationsRV;

    private FirebaseAuth firebaseAuth;
    DatabasePage dbPage;
    String pageId;
    FirebaseStorage storageIns;
    StorageReference storageRef;
    final long ONE_MEGABYTE = 1024 * 1024;
    private ArrayList<NotificationsRow> notificationsArrayList;
    private NotificationsAdapter notificationsAdapter;
    FirebaseUser loggedInUser;
    private static final String TAG = "Notifications";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        notificationsRV = findViewById(R.id.recyclerViewNotifications);

        firebaseAuth = FirebaseAuth.getInstance();
        loggedInUser = firebaseAuth.getCurrentUser();
        userId = loggedInUser.getUid();

        loadReviews();
    }


    private void loadReviews() {

        notificationsArrayList = new ArrayList<>();


        Log.i(TAG, "Notifications: before database");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("/picturebooks");
        ref.orderByChild("userId").equalTo(userId);
        ref.child("/ratings").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.i(TAG, "Notifications: in database");
                notificationsArrayList.clear();

                for(DataSnapshot ds: snapshot.getChildren()){
                    Log.i(TAG, "Notifications: in database - getChildren");
                    NotificationsRow modelNotifications = ds.getValue(NotificationsRow.class);
                    Log.i(TAG, "Notifications: " + modelNotifications);
                    notificationsArrayList.add(modelNotifications);
                }

                notificationsAdapter = new NotificationsAdapter(Notifications.this, notificationsArrayList);
                notificationsRV.setAdapter(notificationsAdapter);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

}