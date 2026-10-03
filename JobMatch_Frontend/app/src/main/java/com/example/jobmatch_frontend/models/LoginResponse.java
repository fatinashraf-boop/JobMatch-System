package com.example.jobmatch_frontend.models;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("token")
    private String token;

    @SerializedName("userId")
    private int userId;

    @SerializedName("role")
    private String role;

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public String getToken() { return token; }
    public int getUserId() { return userId; }
    public String getRole() { return role; }
}