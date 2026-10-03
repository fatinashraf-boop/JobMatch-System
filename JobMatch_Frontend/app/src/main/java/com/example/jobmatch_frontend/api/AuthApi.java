package com.example.jobmatch_frontend.api;

import com.example.jobmatch_frontend.models.AuthResponse;
import com.example.jobmatch_frontend.models.LoginRequest;
import com.example.jobmatch_frontend.models.RegisterRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {

    @POST("auth/login")
    Call<AuthResponse> login(@Body LoginRequest loginRequest);

    @POST("auth/register")
    Call<AuthResponse> register(@Body RegisterRequest registerRequest);
}