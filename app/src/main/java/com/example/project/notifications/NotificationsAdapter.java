package com.example.project.notifications;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Calendar;


public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.HolderReview>{

    private Context context;
    private ArrayList<NotificationsRow> reviewArrayList;
    StorageReference storageRef;
    Bitmap image;


    public NotificationsAdapter(Context context, ArrayList<NotificationsRow> reviewArrayList) {
        this.context = context;
        this.reviewArrayList = reviewArrayList;
    }

    @NonNull
    @Override
    public NotificationsAdapter.HolderReview onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.notifications_row_item, parent, false);
        return new NotificationsAdapter.HolderReview(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationsAdapter.HolderReview holder, int position) {
        NotificationsRow reviewModel = reviewArrayList.get(position);
        String uid = reviewModel.getUid();
        String timestamp = reviewModel.getTimestamp();
        String review = reviewModel.getReview();

        loadUserDetail(reviewModel, holder);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String dateFormat = DateFormat.format("dd/MM/yyyy", calendar).toString();

        holder.reviewText.setText(review);
        holder.dateTV.setText(dateFormat);


    }

    private void loadUserDetail(NotificationsRow notificationsModel, NotificationsAdapter.HolderReview holder) {
        String uid = notificationsModel.getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("/users");
        ref.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String firstName = ""+snapshot.child("firstName").getValue();
                String lastName = ""+snapshot.child("lastName").getValue();
                String name = firstName + " " + lastName;
                holder.nameProfile.setText(name);

                storageRef = FirebaseStorage.getInstance().getReference().child("images/profile_pictures/" + uid);

                final long ONE_MEGABYTE = 1024 * 1024;
                storageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        image = BitmapFactory.decodeByteArray(bytes,0, bytes.length);
                        holder.profilePicture.setImageBitmap(image);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        holder.profilePicture.setImageDrawable(context.getResources().getDrawable(R.drawable.profile));
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return reviewArrayList.size();
    }

    class HolderReview extends RecyclerView.ViewHolder{

        private ImageView profilePicture;
        private TextView nameProfile;
        private TextView dateTV;
        private TextView reviewText;

        public HolderReview(@NonNull View itemView) {
            super(itemView);

            profilePicture = itemView.findViewById(R.id.profilePictureNotifications);
            nameProfile = itemView.findViewById(R.id.nameProfileNotifications);
            dateTV = itemView.findViewById(R.id.dateTVNotfications);
            reviewText = itemView.findViewById(R.id.reviewTextNotifications);
        }
    }

}
