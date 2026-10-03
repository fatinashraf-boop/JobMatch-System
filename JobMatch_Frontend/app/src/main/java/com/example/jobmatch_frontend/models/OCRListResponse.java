package com.example.jobmatch_frontend.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class OCRListResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("ocrResults")
    private List<OCRResult> ocrResults;

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public List<OCRResult> getOcrResults() { return ocrResults; }
}