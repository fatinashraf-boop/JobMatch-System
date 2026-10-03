package com.example.jobmatch_frontend.models;

import java.util.List;

public class ApplicationListResponse {

    private boolean success;

    private int count;

    private List<Application> applications;


    public boolean isSuccess() {
        return success;
    }


    public int getCount() {
        return count;
    }


    public List<Application> getApplications() {
        return applications;
    }
}