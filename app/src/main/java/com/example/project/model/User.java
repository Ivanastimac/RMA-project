package com.example.project.model;

public class User {

    public String firstName;
    public String lastName;
    public String email;
    public String password; // mislim da nam tu nece trebat

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String first, String last, String email) {
        firstName = first;
        lastName = last;
        this.email = email;
    }
}
