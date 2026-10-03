package com.example.jobmatch_frontend.api;

import com.example.jobmatch_frontend.models.ApplicationDetailsResponse;
import com.example.jobmatch_frontend.models.ApplicationListResponse;
import com.example.jobmatch_frontend.models.WithdrawApplicationResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.Path;

public interface ApplicationApi {

    @GET("applications/my")
    Call<ApplicationListResponse> getMyApplications();

    @PATCH("applications/{id}/withdraw")
    Call<WithdrawApplicationResponse> withdrawApplication(
            @Path("id") int applicationId
    );

    @GET("applications/{id}/details")
    Call<ApplicationDetailsResponse> getApplicationDetails(
            @Path("id") int applicationId
    );
}
