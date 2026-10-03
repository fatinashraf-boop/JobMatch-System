package com.example.jobmatch_frontend.api;

import com.example.jobmatch_frontend.models.AuthResponse;
import com.example.jobmatch_frontend.models.LoginRequest;
import com.example.jobmatch_frontend.models.RegisterRequest;
import com.example.jobmatch_frontend.models.OcrUploadResponse;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {

    @POST("auth/login")
    Call<AuthResponse> login(
            @Body LoginRequest request
    );

    @POST("auth/register")
    Call<AuthResponse> register(
            @Body RegisterRequest request
    );

    // OCR document upload
    @Multipart
    @POST("documents")
    Call<OcrUploadResponse> uploadDocument(
            @Part("user_id") RequestBody userId,
            @Part("document_type") RequestBody documentType,
            @Part MultipartBody.Part document
    );
}