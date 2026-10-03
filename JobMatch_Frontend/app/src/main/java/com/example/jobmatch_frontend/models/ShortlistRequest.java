package com.example.jobmatch_frontend.models;

import com.google.gson.annotations.SerializedName;

public class ShortlistRequest {

    @SerializedName("application_id")
    private int applicationId;

    private String notes;

    public ShortlistRequest() {
    }

    public ShortlistRequest(
            int applicationId,
            String notes
    ) {
        this.applicationId = applicationId;
        this.notes = notes;
    }

    public int getApplicationId() {
        return applicationId;
    }

    public String getNotes() {
        return notes;
    }
}