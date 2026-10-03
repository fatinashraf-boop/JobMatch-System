package com.example.jobmatch_frontend.models;

import com.google.gson.annotations.SerializedName;

public class CreateJobResponse {

    private boolean success;
    private String message;
    private Data data;


    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Data getData() {
        return data;
    }


    public static class Data {

        @SerializedName("job_id")
        private int jobId;

        @SerializedName("recruiter_id")
        private int recruiterId;

        @SerializedName("job_title")
        private String jobTitle;

        private String status;


        public int getJobId() {
            return jobId;
        }

        public int getRecruiterId() {
            return recruiterId;
        }

        public String getJobTitle() {
            return jobTitle;
        }

        public String getStatus() {
            return status;
        }
    }
}