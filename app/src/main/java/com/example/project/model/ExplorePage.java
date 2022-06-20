package com.example.project.model;

import android.graphics.Bitmap;


public class ExplorePage {

    private String id;
    private Bitmap image;
    private String caption;

    public ExplorePage(String id, Bitmap bm, String text) {
        this.id = id;
        image = bm;
        caption = text;
    }

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public Bitmap getImage() { return image; }

    public void setImage(Bitmap bm) { image = bm; }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }
}
