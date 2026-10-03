package com.example.jobmatch_frontend.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class JobseekerDashboardResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("dashboard")
    private Dashboard dashboard;


    public boolean isSuccess() {
        return success;
    }

    public Dashboard getDashboard() {
        return dashboard;
    }


    // =========================================
    // DASHBOARD
    // =========================================

    public static class Dashboard {

        @SerializedName("candidate_name")
        private String candidateName;

        @SerializedName("jobs_applied")
        private int jobsApplied;

        @SerializedName("interviews")
        private int interviews;

        @SerializedName("best_matches")
        private List<BestMatch> bestMatches;


        public String getCandidateName() {
            return candidateName;
        }

        public int getJobsApplied() {
            return jobsApplied;
        }

        public int getInterviews() {
            return interviews;
        }

        public List<BestMatch> getBestMatches() {
            return bestMatches;
        }
    }


    // =========================================
    // BEST MATCH
    // =========================================

    public static class BestMatch {

        @SerializedName("match_id")
        private int matchId;

        @SerializedName("job_id")
        private int jobId;

        @SerializedName("recruiter_id")
        private int recruiterId;

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

        @SerializedName("similarity_score")
        private double similarityScore;

        @SerializedName("ranking_position")
        private Integer rankingPosition;

        @SerializedName("match_reason")
        private String matchReason;


        public int getMatchId() {
            return matchId;
        }

        public int getJobId() {
            return jobId;
        }

        public int getRecruiterId() {
            return recruiterId;
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

        public double getSimilarityScore() {
            return similarityScore;
        }

        public Integer getRankingPosition() {
            return rankingPosition;
        }

        public String getMatchReason() {
            return matchReason;
        }
    }
}