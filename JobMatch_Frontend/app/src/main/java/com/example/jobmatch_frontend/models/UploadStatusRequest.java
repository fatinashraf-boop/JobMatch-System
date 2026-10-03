package com.example.jobmatch_frontend.models;

import com.google.gson.annotations.SerializedName;

public class UploadStatusRequest {

    @SerializedName("upload_status")
    private String uploadStatus;

    public UploadStatusRequest() {}

    public UploadStatusRequest(String uploadStatus) {
        this.uploadStatus = uploadStatus;
    }

    public String getUpload_status() { return uploadStatus; }
}