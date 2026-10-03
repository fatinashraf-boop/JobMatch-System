package com.example.jobmatch_frontend.api;

import com.example.jobmatch_frontend.models.ProfileResponse;
import com.example.jobmatch_frontend.models.ProfileUpdateRequest;


import okhttp3.MultipartBody;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;


public interface ProfileApi {

    // GET /api/profiles/me
    @GET("profiles/me")
    Call<ProfileResponse> getMyProfile();


    // PUT /api/profiles/me
    @PUT("profiles/me")
    Call<ProfileResponse> updateMyProfile(
            @Body ProfileUpdateRequest request
    );


    // POST /api/profiles/me/picture
    @Multipart
    @POST("profiles/me/picture")
    Call<ProfileResponse> uploadProfilePicture(
            @Part MultipartBody.Part profilePicture
    );
}