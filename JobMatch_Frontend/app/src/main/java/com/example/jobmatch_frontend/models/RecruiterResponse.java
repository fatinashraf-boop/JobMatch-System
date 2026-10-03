package com.example.jobmatch_frontend.models;

import com.google.gson.annotations.SerializedName;

public class RecruiterResponse {

    private boolean success;
    private String message;

    private User user;
    private Recruiter recruiter;


    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public User getUser() {
        return user;
    }

    public Recruiter getRecruiter() {
        return recruiter;
    }


    // ==========================================
    // USER DATA
    // FROM users TABLE
    // ==========================================
    public static class User {

        @SerializedName("user_id")
        private int userId;

        @SerializedName("full_name")
        private String fullName;

        private String email;

        @SerializedName("phone_number")
        private String phoneNumber;

        private String role;


        public int getUserId() {
            return userId;
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

        public String getRole() {
            return role;
        }
    }


    // ==========================================
    // RECRUITER DATA
    // FROM recruiters TABLE
    // ==========================================
    public static class Recruiter {

        @SerializedName("recruiter_id")
        private int recruiterId;

        @SerializedName("user_id")
        private int userId;

        @SerializedName("company_name")
        private String companyName;

        @SerializedName("company_email")
        private String companyEmail;

        @SerializedName("company_description")
        private String companyDescription;

        private String website;

        @SerializedName("company_logo")
        private String companyLogo;

        private String address;

        @SerializedName("verification_status")
        private String verificationStatus;

        @SerializedName("recruiter_status")
        private String recruiterStatus;

        @SerializedName("created_at")
        private String createdAt;

        @SerializedName("updated_at")
        private String updatedAt;


        public int getRecruiterId() {
            return recruiterId;
        }

        public int getUserId() {
            return userId;
        }

        public String getCompanyName() {
            return companyName;
        }

        public String getCompanyEmail() {
            return companyEmail;
        }

        public String getCompanyDescription() {
            return companyDescription;
        }

        public String getWebsite() {
            return website;
        }

        public String getCompanyLogo() {
            return companyLogo;
        }

        public String getAddress() {
            return address;
        }

        public String getVerificationStatus() {
            return verificationStatus;
        }

        public String getRecruiterStatus() {
            return recruiterStatus;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }


    }
}