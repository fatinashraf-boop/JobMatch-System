package com.example.jobmatch_frontend.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MatchScoreResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("count")
    private int count;

    @SerializedName("data")
    private List<MatchScore> data;


    public boolean isSuccess() {
        return success;
    }

    public int getCount() {
        return count;
    }

    public List<MatchScore> getData() {
        return data;
    }
}