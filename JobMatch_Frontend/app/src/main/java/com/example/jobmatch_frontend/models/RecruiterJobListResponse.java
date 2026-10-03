package com.example.jobmatch_frontend.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;


public class RecruiterJobListResponse {

    private boolean success;

    private String message;

    private int count;


    @SerializedName(value = "jobs", alternate = {"data"})
    private List<RecruiterJob> jobs;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public int getCount() {
        return count;
    }

    public List<RecruiterJob> getJobs() {
        return jobs;
    }


    public static class RecruiterJob {

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

        private String qualifications;

        private String location;

        @SerializedName("salary_min")
        private Double salaryMin;

        @SerializedName("salary_max")
        private Double salaryMax;

        @SerializedName("job_type")
        private String jobType;

        private String status;

        @SerializedName("applicant_count")
        private int applicantCount;

        @SerializedName("shortlisted_count")
        private int shortlistedCount;

        @SerializedName("created_at")
        private String createdAt;

        @SerializedName("updated_at")
        private String updatedAt;


        public int getJobId() {
            return jobId;
        }

        public int getRecruiterId() {
            return recruiterId;
        }

        public String getJobTitle() {
            return jobTitle;
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

        public String getStatus() {
            return status;
        }

        public int getApplicantCount() {
            return applicantCount;
        }

        public int getShortlistedCount() {
            return shortlistedCount;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }
    }
}