package com.example.jobmatch_frontend.models;

import com.google.gson.annotations.SerializedName;

public class RegisterRequest {

    @SerializedName("full_name")
    private String fullName;

    @SerializedName("email")
    private String email;

    @SerializedName("phone_number")
    private String phoneNumber;

    @SerializedName("role")
    private String role;

    @SerializedName("password")
    private String password;


    public RegisterRequest() {}


    public RegisterRequest(
            String fullName,
            String email,
            String phoneNumber,
            String role,
            String password
    ) {
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.password = password;
    }


    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getRole() {
        return role;
    }

    public String getPassword() {
        return password;
    }
}