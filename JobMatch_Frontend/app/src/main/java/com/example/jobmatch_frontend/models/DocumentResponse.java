package com.example.jobmatch_frontend.models;

import com.google.gson.annotations.SerializedName;

public class DocumentResponse {

    @SerializedName("id")
    private int id;

    @SerializedName("userId")
    private int userId;

    @SerializedName("documentType")
    private String documentType;

    @SerializedName("fileName")
    private String fileName;

    @SerializedName("uploadStatus")
    private String uploadStatus;

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getDocumentType() { return documentType; }
    public String getFileName() { return fileName; }
    public String getUploadStatus() { return uploadStatus; }
}