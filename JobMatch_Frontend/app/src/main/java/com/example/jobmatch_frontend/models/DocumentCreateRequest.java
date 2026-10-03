package com.example.jobmatch_frontend.models;

import com.google.gson.annotations.SerializedName;

public class DocumentCreateRequest {

    @SerializedName("userId")
    private int userId;

    @SerializedName("documentType")
    private String documentType;

    @SerializedName("fileName")
    private String fileName;

    @SerializedName("filePath")
    private String filePath;

    @SerializedName("mimeType")
    private String mimeType;

    public DocumentCreateRequest(int userId, String documentType, String fileName, String filePath, String mimeType) {
        this.userId = userId;
        this.documentType = documentType;
        this.fileName = fileName;
        this.filePath = filePath;
        this.mimeType = mimeType;
    }

    public int getUserId() { return userId; }
    public String getDocumentType() { return documentType; }
    public String getFileName() { return fileName; }
    public String getFilePath() { return filePath; }
    public String getMimeType() { return mimeType; }
}