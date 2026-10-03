package com.example.jobmatch_frontend.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;


public class JobseekerApplicationsResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("applications")
    private List<ApplicationItem> applications;


    public boolean isSuccess() {
        return success;
    }


    public List<ApplicationItem> getApplications() {
        return applications;
    }


    public static class ApplicationItem {

        @SerializedName("application_id")
        private int applicationId;

        @SerializedName("job_id")
        private int jobId;

        @SerializedName("job_title")
        private String jobTitle;

        @SerializedName("company_name")
        private String companyName;

        @SerializedName("company_logo")
        private String companyLogo;

        @SerializedName("location")
        private String location;

        @SerializedName("salary_min")
        private Double salaryMin;

        @SerializedName("salary_max")
        private Double salaryMax;

        @SerializedName("job_type")
        private String jobType;

        @SerializedName("application_status")
        private String applicationStatus;

        @SerializedName("shortlist_status")
        private String shortlistStatus;

        @SerializedName("applied_at")
        private String appliedAt;

        @SerializedName("reviewed_at")
        private String reviewedAt;


        public int getApplicationId() {
            return applicationId;
        }

        public int getJobId() {
            return jobId;
        }

        public String getJobTitle() {
            return jobTitle;
        }

        public String getCompanyName() {
            return companyName;
        }

        public String getCompanyLogo() {
            return companyLogo;
        }

        public String getLocation() {
            return location;
        }

        public Double getSalaryMin() {
            return salaryMin;
        }

        public Double getSalaryMax() {
            return salaryMax;
        }

        public String getJobType() {
            return jobType;
        }

        public String getApplicationStatus() {
            return applicationStatus;
        }

        public String getShortlistStatus() {
            return shortlistStatus;
        }

        public String getAppliedAt() {
            return appliedAt;
        }

        public String getReviewedAt() {
            return reviewedAt;
        }
    }
}