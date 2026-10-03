package com.example.jobmatch_frontend.api;

import com.example.jobmatch_frontend.models.CreateJobRequest;
import com.example.jobmatch_frontend.models.CreateJobResponse;
import com.example.jobmatch_frontend.models.JobStatusRequest;
import com.example.jobmatch_frontend.models.RecruiterJobListResponse;
import com.example.jobmatch_frontend.models.RecruiterJobResponse;
import com.example.jobmatch_frontend.models.ShortlistResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;


public interface RecruiterJobApi {

    @POST("recruiter/jobs")
    Call<CreateJobResponse> createJob(
            @Body CreateJobRequest request
    );


    @GET("recruiter/jobs")
    Call<RecruiterJobListResponse> getMyJobs();


    @GET("recruiter/jobs/{jobId}")
    Call<RecruiterJobResponse> getMyJob(
            @Path("jobId") int jobId
    );


    @PUT("recruiter/jobs/{jobId}")
    Call<RecruiterJobResponse> updateJob(
            @Path("jobId") int jobId,
            @Body CreateJobRequest request
    );


    @PATCH("recruiter/jobs/{jobId}/status")
    Call<ShortlistResponse> updateJobStatus(
            @Path("jobId") int jobId,
            @Body JobStatusRequest request
    );
}