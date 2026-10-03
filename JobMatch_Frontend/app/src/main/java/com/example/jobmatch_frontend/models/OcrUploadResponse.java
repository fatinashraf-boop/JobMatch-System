package com.example.jobmatch_frontend.models;
public class OcrUploadResponse {

    private boolean success;
    private String message;
    private Data data;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Data getData() {
        return data;
    }

    public static class Data {

        private int document_id;
        private String file_name;
        private String file_path;
        private String file_type;
        private String document_type;
        private String upload_status;
        private int ocr_id;
        private String extracted_text;
        private double confidence_score;

        public int getDocumentId() {
            return document_id;
        }

        public String getFileName() {
            return file_name;
        }

        public String getFilePath() {
            return file_path;
        }

        public String getFileType() {
            return file_type;
        }

        public String getDocumentType() {
            return document_type;
        }

        public String getUploadStatus() {
            return upload_status;
        }

        public int getOcrId() {
            return ocr_id;
        }

        public String getExtractedText() {
            return extracted_text;
        }

        public double getConfidenceScore() {
            return confidence_score;
        }
    }
}