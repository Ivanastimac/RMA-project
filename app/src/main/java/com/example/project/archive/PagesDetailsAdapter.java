package com.example.project.archive;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.R;
import com.example.project.model.Page;

import java.util.ArrayList;
import java.util.List;

public class PagesDetailsAdapter extends RecyclerView.Adapter<PagesDetailsAdapter.MyView> {

    private List<Page> pages;
    private LayoutInflater mInflater;

    public void setPages(ArrayList<Page> pages) {
        this.pages = pages;
    }

    public PagesDetailsAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    public class MyView extends RecyclerView.ViewHolder {

        ImageView image;

        public MyView(@NonNull View view) {
            super(view);
            image = view.findViewById(R.id.imageViewSinglePage);
        }
    }

    @NonNull
    @Override
    public PagesDetailsAdapter.MyView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.edit_pages_row_item,
                        parent,
                        false);

        return new PagesDetailsAdapter.MyView(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PagesDetailsAdapter.MyView holder, int position) {
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
