package com.example.jobmatch_frontend.models;

import com.google.gson.annotations.SerializedName;

public class CreateJobRequest {

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


    public CreateJobRequest(
            String jobTitle,
            String jobDescription,
            String requiredSkills,
            String qualifications,
            String location,
            Double salaryMin,
            Double salaryMax,
            String jobType,
            String status
    ) {

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
}