package com.example.project.archive;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Movie;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.R;
import com.example.project.model.ArchiveRow;
import com.example.project.model.Picturebook;
import com.example.project.picturebook.PagesAdapter;

import java.util.ArrayList;
import java.util.List;

public class PicturebookAdapter extends RecyclerView.Adapter<PicturebookAdapter.MyView>{

    private List<ArchiveRow> rows;
    private LayoutInflater mInflater;

    public void setPicturebooks(ArrayList<ArchiveRow> rows) {
        this.rows = rows;
    }

    public class MyView extends RecyclerView.ViewHolder {

        ImageView image;
        TextView title;

        public MyView(View view) {
            super(view);
            image = view.findViewById(R.id.imageViewArhive);
            title = view.findViewById(R.id.textViewTitle);
        }
    }

    public PicturebookAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public PicturebookAdapter.MyView onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.archive_row_item,
                        parent,
                        false);

        return new PicturebookAdapter.MyView(itemView);
    }

    @Override
    public void onBindViewHolder(final PicturebookAdapter.MyView holder, final int position) {
        if (rows != null) {
            holder.title.setText(rows.get(position).getTitle());
            holder.image.setImageBitmap(rows.get(position).getFirstPage());
        }
    }

    @Override
    public int getItemCount() {
        if (rows != null) {
            return rows.size();
        } else {
            return 0;
        }
    }

    }
