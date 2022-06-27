package com.example.project.notifications;

import android.graphics.Bitmap;

public class NotificationsRow {
    String uid;
    String review;
    String timestamp;

    public NotificationsRow() {
    }

    public NotificationsRow(String uid, String review, String timestamp) {
        this.uid = uid;
        this.review = review;
        this.timestamp = timestamp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
