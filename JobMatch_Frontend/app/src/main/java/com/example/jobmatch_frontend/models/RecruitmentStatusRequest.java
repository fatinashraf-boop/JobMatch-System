package com.example.jobmatch_frontend.models;

import com.google.gson.annotations.SerializedName;

public class RecruitmentStatusRequest {

    @SerializedName("status")
    private String status;

    public RecruitmentStatusRequest(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
