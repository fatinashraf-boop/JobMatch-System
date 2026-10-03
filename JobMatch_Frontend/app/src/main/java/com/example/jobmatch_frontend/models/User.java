package com.example.jobmatch_frontend.models;

import com.google.gson.annotations.SerializedName;

public class User {

    @SerializedName("user_id")
    private int userId;

    @SerializedName("full_name")
    private String fullName;

    @SerializedName("email")
    private String email;

    @SerializedName("role")
    private String role;

    @SerializedName("phone_number")
    private String phoneNumber;

    public User() {}

    public User(int userId, String fullName, String email, String role, String phoneNumber) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.phoneNumber = phoneNumber;
    }

    public int getUserId() { return userId; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getPhoneNumber() { return phoneNumber; }
}