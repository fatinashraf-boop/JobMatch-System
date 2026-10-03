package com.example.jobmatch_frontend.models;

import com.google.gson.annotations.SerializedName;

public class ProfileUpdateRequest {

    @SerializedName("full_name")
    private String fullName;

    @SerializedName("phone_number")
    private String phoneNumber;

    @SerializedName("headline")
    private String headline;

    @SerializedName("skills")
    private String skills;

    @SerializedName("education")
    private String education;

    @SerializedName("experience")
    private String experience;

    @SerializedName("expected_salary")
    private Double expectedSalary;

    @SerializedName("location")
    private String location;

    @SerializedName("resume_summary")
    private String resumeSummary;


    public ProfileUpdateRequest(
            String fullName,
            String phoneNumber,
            String headline,
            String skills,
            String education,
            String experience,
            Double expectedSalary,
            String location,
            String resumeSummary
    ) {

        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.headline = headline;
        this.skills = skills;
        this.education = education;
        this.experience = experience;
        this.expectedSalary = expectedSalary;
        this.location = location;
        this.resumeSummary = resumeSummary;
    }
}