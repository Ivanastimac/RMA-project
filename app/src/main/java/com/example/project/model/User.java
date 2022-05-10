package com.example.project.model;

public class User {

    public String firstName;
    public String lastName;
    public boolean admin;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String first, String last) {
        firstName = first;
        lastName = last;
        admin = false;
    }
}
