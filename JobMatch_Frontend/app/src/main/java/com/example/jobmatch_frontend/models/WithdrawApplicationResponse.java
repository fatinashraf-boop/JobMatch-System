package com.example.jobmatch_frontend.models;

import com.google.gson.annotations.SerializedName;

public class WithdrawApplicationResponse {

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

        @SerializedName("application_id")
        private int applicationId;

        @SerializedName("application_status")
        private String applicationStatus;


        public int getApplicationId() {
            return applicationId;
        }


        public String getApplicationStatus() {
            return applicationStatus;
        }
    }
}