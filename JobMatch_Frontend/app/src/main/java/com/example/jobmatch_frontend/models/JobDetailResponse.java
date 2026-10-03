package com.example.jobmatch_frontend.models;

public class JobDetailResponse {

    private boolean success;
    private Job data;
    private String message;

    public boolean isSuccess() {
        return success;
    }

    public Job getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }
}