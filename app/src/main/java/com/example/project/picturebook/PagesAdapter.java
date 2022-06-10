package com.example.project.picturebook;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.project.R;
import com.example.project.model.Page;

import java.util.ArrayList;
import java.util.List;

public class PagesAdapter extends RecyclerView.Adapter<PagesAdapter.MyView> {

    private List<Page> pages;
    private LayoutInflater mInflater;

    public void setImages(ArrayList<Page> pages) {
        this.pages = pages;
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
        if (pages != null) {
            holder.image.setImageBitmap(pages.get(position).getImage());
        }
    }

    @Override
    public int getItemCount() {
        if (pages != null) {
            return pages.size();
        } else {
            return 0;
        }

    }

}