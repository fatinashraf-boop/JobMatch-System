package com.example.jobmatch_frontend.models;

import com.google.gson.annotations.SerializedName;

public class OCRResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("result")
    private OCRResult result;

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public OCRResult getResult() { return result; }
}