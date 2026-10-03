package com.example.jobmatch_frontend.api;

import com.example.jobmatch_frontend.models.RecruiterResponse;
import com.example.jobmatch_frontend.models.RecruiterUpdateRequest;

import okhttp3.MultipartBody;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;

public interface RecruiterApi {

    @GET("recruiters/me")
    Call<RecruiterResponse> getMyProfile();

    @PUT("recruiters/me")
    Call<RecruiterResponse> updateMyProfile(
            @Body RecruiterUpdateRequest request
    );
    @Multipart
    @POST("recruiters/me/logo")
    Call<RecruiterResponse> uploadMyLogo(@Part MultipartBody.Part logo);
}