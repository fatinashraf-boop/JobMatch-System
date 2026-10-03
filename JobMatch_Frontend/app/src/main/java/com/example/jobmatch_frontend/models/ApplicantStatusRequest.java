package com.example.jobmatch_frontend.models;

import com.google.gson.annotations.SerializedName;

public class ApplicantStatusRequest {

    @SerializedName("status")
    private String status;

    public ApplicantStatusRequest(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
