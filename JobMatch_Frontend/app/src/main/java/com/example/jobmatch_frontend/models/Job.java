package com.example.jobmatch_frontend.models;

import com.google.gson.annotations.SerializedName;

public class Job {

    // ==========================================
    // JOB
    // ==========================================

    @SerializedName("job_id")
    private int jobId;

    @SerializedName("recruiter_id")
    private int recruiterId;

    @SerializedName("job_title")
    private String jobTitle;

    @SerializedName("job_description")
    private String jobDescription;

    @SerializedName("required_skills")
    private String requiredSkills;

    @SerializedName("qualifications")
    private String qualifications;

    @SerializedName("location")
    private String location;

    @SerializedName("salary_min")
    private double salaryMin;

    @SerializedName("salary_max")
    private double salaryMax;

    @SerializedName("job_type")
    private String jobType;

    @SerializedName("status")
    private String status;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;


    // ==========================================
    // COMPANY / RECRUITER
    // ==========================================

    @SerializedName("company_name")
    private String companyName;

    @SerializedName("company_email")
    private String companyEmail;

    @SerializedName("company_description")
    private String companyDescription;

    @SerializedName("website")
    private String website;

    @SerializedName("company_logo")
    private String companyLogo;

    @SerializedName("address")
    private String address;

    @SerializedName("verification_status")
    private String verificationStatus;


    // ==========================================
    // CONSTRUCTORS
    // ==========================================

    public Job() {
    }


    public Job(
            int recruiterId,
            String jobTitle,
            String jobDescription,
            String requiredSkills,
            String qualifications,
            String location,
            double salaryMin,
            double salaryMax,
            String jobType,
            String status
    ) {

        this.recruiterId = recruiterId;
        this.jobTitle = jobTitle;
        this.jobDescription = jobDescription;
        this.requiredSkills = requiredSkills;
        this.qualifications = qualifications;
        this.location = location;
        this.salaryMin = salaryMin;
        this.salaryMax = salaryMax;
        this.jobType = jobType;
        this.status = status;
    }


    public Job(
            int jobId,
            int recruiterId,
            String jobTitle,
            String jobDescription,
            String requiredSkills,
            String qualifications,
            String location,
            double salaryMin,
            double salaryMax,
            String jobType,
            String status
    ) {

        this.jobId = jobId;
        this.recruiterId = recruiterId;
        this.jobTitle = jobTitle;
        this.jobDescription = jobDescription;
        this.requiredSkills = requiredSkills;
        this.qualifications = qualifications;
        this.location = location;
        this.salaryMin = salaryMin;
        this.salaryMax = salaryMax;
        this.jobType = jobType;
        this.status = status;
    }


    // ==========================================
    // GETTERS / SETTERS
    // ==========================================

    public int getJobId() {
        return jobId;
    }

    public int getId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }


    public int getRecruiterId() {
        return recruiterId;
    }

    public void setRecruiterId(int recruiterId) {
        this.recruiterId = recruiterId;
    }


    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }


    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }


    public String getRequiredSkills() {
        return requiredSkills;
    }

    public void setRequiredSkills(String requiredSkills) {
        this.requiredSkills = requiredSkills;
    }


    public String getQualifications() {
        return qualifications;
    }

    public void setQualifications(String qualifications) {
        this.qualifications = qualifications;
    }


    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }


    public double getSalaryMin() {
        return salaryMin;
    }

    public void setSalaryMin(double salaryMin) {
        this.salaryMin = salaryMin;
    }


    public double getSalaryMax() {
        return salaryMax;
    }

    public void setSalaryMax(double salaryMax) {
        this.salaryMax = salaryMax;
    }


    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public String getCreatedAt() {
        return createdAt;
    }


    public String getUpdatedAt() {
        return updatedAt;
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
}