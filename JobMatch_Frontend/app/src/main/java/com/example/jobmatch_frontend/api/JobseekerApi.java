package com.example.jobmatch_frontend.api;

import com.example.jobmatch_frontend.models.JobseekerDashboardResponse;

import retrofit2.Call;
import retrofit2.http.GET;

public interface JobseekerApi {

    @GET("jobseeker/dashboard")
    Call<JobseekerDashboardResponse> getDashboard();

}