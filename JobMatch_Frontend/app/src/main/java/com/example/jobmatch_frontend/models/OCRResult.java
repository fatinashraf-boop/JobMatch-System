package com.example.jobmatch_frontend.models;

import com.google.gson.annotations.SerializedName;

public class OCRResult {

    @SerializedName("documentId")
    private int documentId;

    @SerializedName("extractedText")
    private String extractedText;

    @SerializedName("status")
    private String status;

    public OCRResult() {}

    public OCRResult(int documentId, String extractedText, String status) {
        this.documentId = documentId;
        this.extractedText = extractedText;
        this.status = status;
    }

    public int getDocumentId() { return documentId; }
    public String getExtractedText() { return extractedText; }
    public String getStatus() { return status; }
}