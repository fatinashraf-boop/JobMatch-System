package com.example.jobmatch_frontend.models;

import com.google.gson.annotations.SerializedName;

public class ApplicationRequest {

    @SerializedName("job_id")
    private int jobId;


    public ApplicationRequest(
            int jobId
    ) {

        this.jobId =
                jobId;
    }


    public int getJobId() {
        return jobId;
    }
}