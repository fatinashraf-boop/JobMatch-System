package com.example.jobmatch_frontend.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CandidateDetailResponse {

    private boolean success;
    private String message;

    private Job job;
    private Candidate candidate;
    private Match match;
    private Application application;
    private Shortlist shortlist;
    private List<Document> documents;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Job getJob() {
        return job;
    }

    public Candidate getCandidate() {
        return candidate;
    }

    public Match getMatch() {
        return match;
    }

    public Application getApplication() {
        return application;
    }

    public Shortlist getShortlist() {
        return shortlist;
    }

    public List<Document> getDocuments() {
        return documents;
    }


    // =====================================================
    // JOB
    // =====================================================

    public static class Job {

        @SerializedName("job_id")
        private int jobId;

        @SerializedName("job_title")
        private String jobTitle;

        private String status;

        public int getJobId() {
            return jobId;
        }

        public String getJobTitle() {
            return jobTitle;
        }

        public String getStatus() {
            return status;
        }
    }


    // =====================================================
    // CANDIDATE
    // =====================================================

    public static class Candidate {

        @SerializedName("user_id")
        private int userId;

        @SerializedName("profile_id")
        private int profileId;

        @SerializedName("full_name")
        private String fullName;

        private String email;

        @SerializedName("phone_number")
        private String phoneNumber;

        private String headline;
        private String skills;
        private String education;
        private String experience;

        @SerializedName("expected_salary")
        private Double expectedSalary;

        private String location;

        @SerializedName("profile_picture")
        private String profilePicture;

        @SerializedName("resume_summary")
        private String resumeSummary;

        public int getUserId() {
            return userId;
        }

        public int getProfileId() {
            return profileId;
        }

        public String getFullName() {
            return fullName;
        }

        public String getEmail() {
            return email;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public String getHeadline() {
            return headline;
        }

        public String getSkills() {
            return skills;
        }

        public String getEducation() {
            return education;
        }

        public String getExperience() {
            return experience;
        }

        public Double getExpectedSalary() {
            return expectedSalary;
        }

        public String getLocation() {
            return location;
        }

        public String getProfilePicture() {
            return profilePicture;
        }

        public String getResumeSummary() {
            return resumeSummary;
        }
    }


    // =====================================================
    // MATCH
    // =====================================================

    public static class Match {

        @SerializedName("match_id")
        private int matchId;

        @SerializedName("similarity_score")
        private double similarityScore;

        @SerializedName("ranking_position")
        private int rankingPosition;

        @SerializedName("match_reason")
        private String matchReason;

        @SerializedName("matching_algorithm")
        private String matchingAlgorithm;

        public int getMatchId() {
            return matchId;
        }

        public double getSimilarityScore() {
            return similarityScore;
        }

        public int getRankingPosition() {
            return rankingPosition;
        }

        public String getMatchReason() {
            return matchReason;
        }

        public String getMatchingAlgorithm() {
            return matchingAlgorithm;
        }
    }


    // =====================================================
    // APPLICATION
    // =====================================================

    public static class Application {

        @SerializedName("application_id")
        private int applicationId;

        @SerializedName("application_status")
        private String applicationStatus;

        @SerializedName("applied_at")
        private String appliedAt;

        @SerializedName("reviewed_at")
        private String reviewedAt;

        public int getApplicationId() {
            return applicationId;
        }

        public String getApplicationStatus() {
            return applicationStatus;
        }

        public String getAppliedAt() {
            return appliedAt;
        }

        public String getReviewedAt() {
            return reviewedAt;
        }
    }


    // =====================================================
    // SHORTLIST
    // =====================================================

    public static class Shortlist {

        @SerializedName("shortlist_id")
        private int shortlistId;

        @SerializedName("shortlist_status")
        private String shortlistStatus;

        private String notes;

        @SerializedName("shortlisted_at")
        private String shortlistedAt;

        public int getShortlistId() {
            return shortlistId;
        }

        public String getShortlistStatus() {
            return shortlistStatus;
        }

        public String getNotes() {
            return notes;
        }

        public String getShortlistedAt() {
            return shortlistedAt;
        }
    }


    // =====================================================
    // DOCUMENT
    // =====================================================

    public static class Document {

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

        private Ocr ocr;

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

        public Ocr getOcr() {
            return ocr;
        }
    }


    // =====================================================
    // OCR
    // =====================================================

    public static class Ocr {

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