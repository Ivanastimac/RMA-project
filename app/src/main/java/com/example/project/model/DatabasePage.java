package com.example.project.model;

public class DatabasePage {

    private String id;
    private String picturebookId;

    public DatabasePage() {
        // Default constructor required for calls to DataSnapshot.getValue(DatabasePage.class)
    }

    public DatabasePage(String id, String picturebookId) {
        this.id = id;
        this.picturebookId = picturebookId;
    }

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public String getPicturebookId() { return picturebookId; }

    public void setPicturebookId(String id) { picturebookId = id; }
}
