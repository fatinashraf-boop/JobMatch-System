package com.example.jobmatch_frontend.models;

import com.google.gson.annotations.SerializedName;

public class RecruiterDashboardResponse {

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


    // ==========================================
    // DASHBOARD DATA
    // ==========================================

    public static class Data {

        @SerializedName("company_name")
        private String companyName;

        @SerializedName("active_jobs")
        private int activeJobs;

        @SerializedName("total_applicants")
        private int totalApplicants;

        @SerializedName("shortlisted")
        private int shortlisted;


        public String getCompanyName() {
            return companyName;
        }

        public int getActiveJobs() {
            return activeJobs;
        }

        public int getTotalApplicants() {
            return totalApplicants;
        }

        public int getShortlisted() {
            return shortlisted;
        }
    }
}