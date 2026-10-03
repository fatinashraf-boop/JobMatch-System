package com.example.jobmatch_frontend.models;

public class RecruiterUpdateRequest {

    private String company_name;
    private String company_email;
    private String company_description;
    private String website;
    private String company_logo;
    private String address;

    public RecruiterUpdateRequest(
            String companyName,
            String companyEmail,
            String companyDescription,
            String website,
            String companyLogo,
            String address
    ) {
        this.company_name = companyName;
        this.company_email = companyEmail;
        this.company_description = companyDescription;
        this.website = website;
        this.company_logo = companyLogo;
        this.address = address;
    }
}