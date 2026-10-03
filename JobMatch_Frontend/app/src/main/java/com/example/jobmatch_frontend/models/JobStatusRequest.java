package com.example.jobmatch_frontend.models;

import com.google.gson.annotations.SerializedName;


public class JobStatusRequest {

    @SerializedName("status")
    private String status;


    public JobStatusRequest(
            String status
    ) {

        this.status =
                status;
    }


    public String getStatus() {
        return status;
    }
}