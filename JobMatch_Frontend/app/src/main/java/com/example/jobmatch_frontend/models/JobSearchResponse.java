package com.example.jobmatch_frontend.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class JobSearchResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("count")
    private int count;

    @SerializedName("data")
    private List<Job> data;

    @SerializedName("message")
    private String message;


    public boolean isSuccess() {
        return success;
    }

    public int getCount() {
        return count;
    }

    public List<Job> getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }
}