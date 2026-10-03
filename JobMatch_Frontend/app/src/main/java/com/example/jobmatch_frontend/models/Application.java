package com.example.jobmatch_frontend.models;

import com.google.gson.annotations.SerializedName;

public class Application {

    @SerializedName("application_id")
    private int applicationId;

    @SerializedName("job_id")
    private int jobId;

    @SerializedName("profile_id")
    private int profileId;

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

    @SerializedName("job_description")
    private String jobDescription;

    @SerializedName("required_skills")
    private String requiredSkills;

    @SerializedName("qualifications")
    private String qualifications;

    @SerializedName("company_email")
    private String companyEmail;

    @SerializedName("company_description")
    private String companyDescription;

    private String website;

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

    public String getJobDescription() {
        return jobDescription;
    }

    public String getRequiredSkills() {
        return requiredSkills;
    }

    public String getQualifications() {
        return qualifications;
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

    public String getNotes() {
        return notes;
    }

    public String getShortlistedAt() {
        return shortlistedAt;
    }
}