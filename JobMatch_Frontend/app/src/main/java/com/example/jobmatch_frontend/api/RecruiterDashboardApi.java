package com.example.jobmatch_frontend.api;

import com.example.jobmatch_frontend.models.RecruiterDashboardResponse;

import retrofit2.Call;
import retrofit2.http.GET;

public interface RecruiterDashboardApi {

    @GET("recruiter/dashboard")
    Call<RecruiterDashboardResponse> getDashboard();
}