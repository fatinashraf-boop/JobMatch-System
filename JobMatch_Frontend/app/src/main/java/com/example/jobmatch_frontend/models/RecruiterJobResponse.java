package com.example.jobmatch_frontend.models;


public class RecruiterJobResponse {

    private boolean success;

    private String message;

    private RecruiterJobListResponse.RecruiterJob data;


    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public RecruiterJobListResponse.RecruiterJob getData() {
        return data;
    }
}