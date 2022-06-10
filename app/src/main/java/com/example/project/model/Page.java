package com.example.project.model;

import android.graphics.Bitmap;

public class Page {

    private String id;
    private Bitmap image;

    public Page(String id, Bitmap bm) {
        this.id = id;
        image = bm;
    }

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public Bitmap getImage() { return image; }

    public void setImage(Bitmap bm) { image = bm; }
}
