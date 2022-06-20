package com.example.project.model;

import android.graphics.Bitmap;

public class Page {

    private String id;
    private Bitmap image;
    private String caption;
    private int num;

    public Page(String id, Bitmap bm, String caption, int num) {
        this.id = id;
        image = bm;
        this.caption = caption;
        this.num = num;
    }

    public Page(String id, Bitmap bm) {
        this.id = id;
        image = bm;
    }

    public Page(DatabasePage page) {
        this.caption = page.getCaption();
        this.num = page.getNum();
    }

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public Bitmap getImage() { return image; }

    public void setImage(Bitmap bm) { image = bm; }

    public String getCaption() { return caption; }

    public void setCaption(String caption) { this.caption = caption; }

    public int getNum() { return num; }

    public void setNum(int num) { this.num = num; }
}
