package com.example.jobmatch_frontend.models;

import com.google.gson.annotations.SerializedName;

public class Document {

    @SerializedName("id")
    private int id;

    @SerializedName("userId")
    private int userId;

    @SerializedName("documentType")
    private String documentType;

    @SerializedName("file_name")
    private String file_name;

    @SerializedName("file_path")
    private String file_path;

    @SerializedName("upload_status")
    private String upload_status;

    public Document() {}

    public Document(int id, int userId, String documentType, String file_name, String file_path, String upload_status) {
        this.id = id;
        this.userId = userId;
        this.documentType = documentType;
        this.file_name = file_name;
        this.file_path = file_path;
        this.upload_status = upload_status;
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getDocumentType() { return documentType; }
    public String getFile_name() { return file_name; }
    public String getFile_path() { return file_path; }
    public String getUpload_status() { return upload_status; }
}