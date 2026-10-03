package com.example.jobmatch_frontend.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RecruiterApplicantResponse {

    private boolean success;
    private String message;
    private Job job;
    private int count;
    private List<Applicant> applicants;


    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Job getJob() {
        return job;
    }

    public int getCount() {
        return count;
    }

    public List<Applicant> getApplicants() {
        return applicants;
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
    // APPLICANT
    // =====================================================

    public static class Applicant {

        @SerializedName("application_id")
        private int applicationId;

        @SerializedName("job_id")
        private int jobId;

        @SerializedName("profile_id")
        private int profileId;

        @SerializedName("full_name")
        private String candidateName;

        private String email;

        @SerializedName("phone_number")
        private String phoneNumber;

        private String headline;

        private String location;

        @SerializedName("expected_salary")
        private Double expectedSalary;

        @SerializedName("profile_picture")
        private String profilePicture;

        private String skills;


        // APPLICATION

        @SerializedName("application_status")
        private String applicationStatus;

        @SerializedName("applied_at")
        private String appliedAt;

        @SerializedName("reviewed_at")
        private String reviewedAt;


        // MATCH

        @SerializedName("similarity_score")
        private Double similarityScore;

        @SerializedName("ranking_position")
        private Integer rankingPosition;

        @SerializedName("match_reason")
        private String matchReason;


        // SHORTLIST

        @SerializedName("shortlist_id")
        private Integer shortlistId;

        @SerializedName("shortlist_status")
        private String shortlistStatus;

        private String notes;

        @SerializedName("shortlisted_at")
        private String shortlistedAt;


        public int getApplicationId() {
            return applicationId;
        }

        public int getJobId() {
            return jobId;
        }

        public int getProfileId() {
            return profileId;
        }

        public String getCandidateName() {
            return candidateName;
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

        public String getLocation() {
            return location;
        }

        public Double getExpectedSalary() {
            return expectedSalary;
        }

        public String getProfilePicture() {
            return profilePicture;
        }

        public String getSkills() {
            return skills;
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

        public Double getSimilarityScore() {
            return similarityScore;
        }

        public Integer getRankingPosition() {
            return rankingPosition;
        }

        public String getMatchReason() {
            return matchReason;
        }

        public Integer getShortlistId() {
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

        public boolean hasMatchScore() {
            return similarityScore != null;
        }

        public boolean isShortlisted() {
            return shortlistId != null
                    && shortlistId > 0
                    && shortlistStatus != null
                    && !"removed".equalsIgnoreCase(shortlistStatus);
        }
    }
}