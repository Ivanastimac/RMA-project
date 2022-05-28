package com.example.project.picturebook;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.project.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class PagesAdapter extends RecyclerView.Adapter<PagesAdapter.MyView> {

    // novi entitet umjesto Bitmap
    private List<Bitmap> images;
    private LayoutInflater mInflater;

    public void setImages(ArrayList<Bitmap> images) {
        this.images = images;
    }

    public class MyView extends RecyclerView.ViewHolder {

        ImageView image;

        public MyView(View view) {
            super(view);
            image = (ImageView) view.findViewById(R.id.imageViewPage);
        }
    }

    public PagesAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public MyView onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.picturebook_row_item,
                        parent,
                        false);

        return new MyView(itemView);
    }

    @Override
    public void onBindViewHolder(final MyView holder, final int position) {
        if (images != null) {
            holder.image.setImageBitmap(images.get(position));
        }
    }

    @Override
    public int getItemCount() {
        if (images != null) {
            return images.size();
        } else {
            return 0;
        }

    }

}