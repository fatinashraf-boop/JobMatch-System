package com.example.jobmatch_frontend.models;

import com.google.gson.annotations.SerializedName;

public class CandidateMatch {

    @SerializedName("profileId")
    private int profileId;

    @SerializedName("candidateName")
    private String candidateName;

    @SerializedName("headline")
    private String headline;

    @SerializedName("similarityScore")
    private double similarityScore;

    @SerializedName("skills")
    private String skills;

    @SerializedName("matchReason")
    private String matchReason;

    public CandidateMatch() {}

    public CandidateMatch(int profileId, String candidateName, String headline, double similarityScore, String skills, String matchReason) {
        this.profileId = profileId;
        this.candidateName = candidateName;
        this.headline = headline;
        this.similarityScore = similarityScore;
        this.skills = skills;
        this.matchReason = matchReason;
    }

    public int getProfileId() {
        return profileId;
    }

    public void setProfileId(int profileId) {
        this.profileId = profileId;
    }

    public String getCandidateName() {
        return candidateName;
    }

    public void setCandidateName(String candidateName) {
        this.candidateName = candidateName;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public double getSimilarityScore() {
        return similarityScore;
    }

    public void setSimilarityScore(double similarityScore) {
        this.similarityScore = similarityScore;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public String getMatchReason() {
        return matchReason;
    }

    public void setMatchReason(String matchReason) {
        this.matchReason = matchReason;
    }

    // Alias methods for compatibility if needed
    public int getCandidateId() {
        return profileId;
    }

    public double getMatchScore() {
        return similarityScore;
    }
}
