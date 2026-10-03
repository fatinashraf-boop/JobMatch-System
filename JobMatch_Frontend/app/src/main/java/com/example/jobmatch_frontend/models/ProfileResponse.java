package com.example.jobmatch_frontend.models;

import com.google.gson.annotations.SerializedName;

public class ProfileResponse {

    private boolean success;

    private String message;

    private UserData user;

    private ProfileData profile;


    // ==========================================
    // MAIN RESPONSE GETTERS
    // ==========================================

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public UserData getUser() {
        return user;
    }

    public ProfileData getProfile() {
        return profile;
    }


    // ==========================================
    // USERS TABLE DATA
    // ==========================================

    public static class UserData {

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
    // PROFILES TABLE DATA
    // ==========================================

    public static class ProfileData {

        @SerializedName("profile_id")
        private int profileId;

        @SerializedName("user_id")
        private int userId;

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

        @SerializedName("profile_status")
        private String profileStatus;


        public int getProfileId() {
            return profileId;
        }

        public int getUserId() {
            return userId;
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

        public String getProfileStatus() {
            return profileStatus;
        }
    }
}