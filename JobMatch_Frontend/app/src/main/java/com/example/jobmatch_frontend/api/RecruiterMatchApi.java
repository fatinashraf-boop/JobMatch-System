package com.example.jobmatch_frontend.api;

import com.example.jobmatch_frontend.models.CandidateDetailResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface RecruiterMatchApi {

    @GET("recruiter/jobs/{jobId}/candidates/{profileId}")
    Call<CandidateDetailResponse> getCandidateDetail(
            @Path("jobId") int jobId,
            @Path("profileId") int profileId
    );
}