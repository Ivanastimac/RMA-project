package com.example.project.model;

public class Picturebook {

    private String userId;
    private String title;
    private String summary;
    private Status status;
    // array of pages ids
    private String[] pages;

    public Picturebook() {
        // Default constructor required for calls to DataSnapshot.getValue(Picturebook.class)
    }

    public Picturebook(String userId, String title, String summary) {
        this.userId = userId;
        this.title = title;
        this.summary = summary;
        status = Status.PRIVATE;
        // TODO
        // pages = new String[];
    }

    public String getUserId() {
        return userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

}
