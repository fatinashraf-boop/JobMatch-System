package com.example.jobmatch_frontend.models;

import com.google.gson.annotations.SerializedName;

public class AuthResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("token")
    private String token;

    @SerializedName("user")

    private User user;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getToken() {
        return token;
    }

    public User getUser() {
        return user;
    }

    public static class User {

        @SerializedName("user_id")
        private int userId;

        @SerializedName("full_name")
        private String fullName;

        @SerializedName("email")
        private String email;

        @SerializedName("role")
        private String role;

        @SerializedName("phone_number")
        private String phoneNumber;

        @SerializedName("onboarding_status")
        private String onboardingStatus;

        public int getId() {
            return userId;
        }

        public int getUserId() {
            return userId;
        }

        public String getFullName() {
            return fullName;
        }

        public String getEmail() {
            return email;
        }

        public String getRole() {
            return role;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public String getOnboardingStatus() {
            return onboardingStatus;
        }
    }
}