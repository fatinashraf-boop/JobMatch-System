package com.example.jobmatch_frontend.models;

public class ApplicationResponse {

    private boolean success;
    private String message;
    private Application data;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Application getData() {
        return data;
    }
}