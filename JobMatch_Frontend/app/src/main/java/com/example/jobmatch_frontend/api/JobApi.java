package com.example.jobmatch_frontend.api;

import com.example.jobmatch_frontend.models.ApplicationRequest;
import com.example.jobmatch_frontend.models.ApplicationResponse;
import com.example.jobmatch_frontend.models.Job;
import com.example.jobmatch_frontend.models.JobDetailResponse;
import com.example.jobmatch_frontend.models.JobSearchResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;


public interface JobApi {

    @GET("jobs")
    Call<List<Job>> getAllJobs();

    @GET("jobs/recommended")
    Call<List<Job>> getRecommendedJobs();

    @GET("jobs/search")
    Call<JobSearchResponse> searchJobs(
            @Query("keyword") String keyword,
            @Query("location") String location,
            @Query("job_type") String jobType
    );

    @GET("jobs/{id}")
    Call<JobDetailResponse> getJobById(
            @Path("id") int jobId
    );

    @POST("applications")
    Call<ApplicationResponse> applyForJob(
            @Body ApplicationRequest request
    );
}