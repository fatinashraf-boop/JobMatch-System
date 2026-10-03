package com.example.jobmatch_frontend.models;

import com.google.gson.annotations.SerializedName;

public class Profile {

    @SerializedName("profile_id")
    private int profileId;

    @SerializedName("user_id")
    private int userId;

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

    @SerializedName("profile_picture")
    private String profilePicture;

    @SerializedName("profile_status")
    private String profileStatus;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    public Profile() {}

    public Profile(int userId, String headline, String skills, String education, String experience, Double expectedSalary, String location, String resumeSummary) {
        this.userId = userId;
        this.headline = headline;
        this.skills = skills;
        this.education = education;
        this.experience = experience;
        this.expectedSalary = expectedSalary;
        this.location = location;
        this.resumeSummary = resumeSummary;
    }

    public int getProfileId() { return profileId; }
    public void setProfileId(int profileId) { this.profileId = profileId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getHeadline() { return headline; }
    public void setHeadline(String headline) { this.headline = headline; }

    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }

    public String getEducation() { return education; }
    public void setEducation(String education) { this.education = education; }

    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }

    public Double getExpectedSalary() { return expectedSalary; }
    public void setExpectedSalary(Double expectedSalary) { this.expectedSalary = expectedSalary; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getResumeSummary() { return resumeSummary; }
    public void setResumeSummary(String resumeSummary) { this.resumeSummary = resumeSummary; }

    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }

    public String getProfileStatus() { return profileStatus; }
    public void setProfileStatus(String profileStatus) { this.profileStatus = profileStatus; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}