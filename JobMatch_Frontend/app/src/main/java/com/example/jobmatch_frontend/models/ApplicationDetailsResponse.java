package com.example.jobmatch_frontend.models;

public class ApplicationDetailsResponse {

    private boolean success;
    private Application data;


    public boolean isSuccess() {
        return success;
    }


    public Application getData() {
        return data;
    }
}