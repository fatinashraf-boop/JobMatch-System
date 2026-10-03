package com.example.jobmatch_frontend.api;

import com.example.jobmatch_frontend.models.MatchScoreResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface MatchScoreApi {

    @GET("match-scores/job/{job_id}")
    Call<MatchScoreResponse> getJobMatches(
            @Path("job_id") int jobId
    );
}