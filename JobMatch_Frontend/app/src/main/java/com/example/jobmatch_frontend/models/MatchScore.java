package com.example.jobmatch_frontend.models;

import com.google.gson.annotations.SerializedName;

public class MatchScore {

    @SerializedName("match_id")
    private int matchId;

    @SerializedName("job_id")
    private int jobId;

    @SerializedName("profile_id")
    private int profileId;

    @SerializedName("user_id")
    private int userId;

    @SerializedName("full_name")
    private String fullName;

    @SerializedName("similarity_score")
    private double similarityScore;

    @SerializedName("ranking_position")
    private Integer rankingPosition;

    @SerializedName("match_reason")
    private String matchReason;

    @SerializedName("matching_algorithm")
    private String matchingAlgorithm;

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

    @SerializedName("profile_picture")
    private String profilePicture;

    @SerializedName("resume_summary")
    private String resumeSummary;


    public int getMatchId() {
        return matchId;
    }

    public int getJobId() {
        return jobId;
    }

    public int getProfileId() {
        return profileId;
    }

    public int getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public double getSimilarityScore() {
        return similarityScore;
    }

    public Integer getRankingPosition() {
        return rankingPosition;
    }

    public String getMatchReason() {
        return matchReason;
    }

    public String getMatchingAlgorithm() {
        return matchingAlgorithm;
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
}