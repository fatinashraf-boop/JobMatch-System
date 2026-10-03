package com.example.jobmatch_frontend.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;


public class DocumentListResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("count")
    private int count;

    @SerializedName("data")
    private List<DocumentItem> data;


    public boolean isSuccess() {
        return success;
    }

    public int getCount() {
        return count;
    }

    public List<DocumentItem> getData() {
        return data;
    }


    public static class DocumentItem {

        @SerializedName("document_id")
        private int documentId;

        @SerializedName("file_name")
        private String fileName;

        @SerializedName("file_path")
        private String filePath;

        @SerializedName("file_type")
        private String fileType;

        @SerializedName("document_type")
        private String documentType;

        @SerializedName("upload_status")
        private String uploadStatus;

        @SerializedName("created_at")
        private String createdAt;

        @SerializedName("updated_at")
        private String updatedAt;

        @SerializedName("ocr")
        private OcrInfo ocr;


        public int getDocumentId() {
            return documentId;
        }

        public String getFileName() {
            return fileName;
        }

        public String getFilePath() {
            return filePath;
        }

        public String getFileType() {
            return fileType;
        }

        public String getDocumentType() {
            return documentType;
        }

        public String getUploadStatus() {
            return uploadStatus;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public OcrInfo getOcr() {
            return ocr;
        }
    }


    public static class OcrInfo {

        @SerializedName("ocr_id")
        private int ocrId;

        @SerializedName("extracted_text")
        private String extractedText;

        @SerializedName("extracted_name")
        private String extractedName;

        @SerializedName("extracted_issuer")
        private String extractedIssuer;

        @SerializedName("extracted_date")
        private String extractedDate;

        @SerializedName("confidence_score")
        private Double confidenceScore;

        @SerializedName("processed_at")
        private String processedAt;


        public int getOcrId() {
            return ocrId;
        }

        public String getExtractedText() {
            return extractedText;
        }

        public String getExtractedName() {
            return extractedName;
        }

        public String getExtractedIssuer() {
            return extractedIssuer;
        }

        public String getExtractedDate() {
            return extractedDate;
        }

        public Double getConfidenceScore() {
            return confidenceScore;
        }

        public String getProcessedAt() {
            return processedAt;
        }
    }
}