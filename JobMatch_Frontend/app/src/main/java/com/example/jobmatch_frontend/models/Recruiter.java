package com.example.jobmatch_frontend.models;

import com.google.gson.annotations.SerializedName;

public class Recruiter {

    @SerializedName("recruiter_id")
    private int recruiterId;

    @SerializedName("user_id")
    private int userId;

    @SerializedName("company_name")
    private String companyName;

    @SerializedName("verification_status")
    private String verificationStatus;

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

    @SerializedName("recruiter_status")
    private String recruiterStatus;

    public Recruiter() {}

    public int getRecruiterId() { return recruiterId; }
    public int getUserId() { return userId; }
    public String getCompanyName() { return companyName; }
    public String getVerificationStatus() { return verificationStatus; }
}