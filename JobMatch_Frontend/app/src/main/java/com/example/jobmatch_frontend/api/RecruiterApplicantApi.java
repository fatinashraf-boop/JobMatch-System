package com.example.jobmatch_frontend.api;

import com.example.jobmatch_frontend.models.ApplicantStatusRequest;
import com.example.jobmatch_frontend.models.RecruiterApplicantResponse;
import com.example.jobmatch_frontend.models.RecruitmentStatusRequest;
import com.example.jobmatch_frontend.models.ShortlistResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.Path;

public interface RecruiterApplicantApi {

    // =====================================================
    // GET ALL APPLICANTS FOR LOGGED-IN RECRUITER
    // =====================================================

    @GET("recruiter/applicants")
    Call<RecruiterApplicantResponse> getAllApplicants();

    @GET("recruiter/applicants/history")
    Call<RecruiterApplicantResponse> getApplicantHistory();

    // =====================================================
    // GET APPLICANTS FOR ONE RECRUITER-OWNED JOB
    // =====================================================

    @GET("recruiter/jobs/{jobId}/applicants")
    Call<RecruiterApplicantResponse> getJobApplicants(
            @Path("jobId") int jobId
    );

    // =====================================================
    // GET SHORTLISTED APPLICANTS
    // =====================================================

    @GET("recruiter/shortlisted")
    Call<RecruiterApplicantResponse> getShortlistedApplicants();


    // =====================================================
    // UPDATE APPLICATION STATUS
    //
    // pending
    // reviewed
    // shortlisted
    // rejected
    // =====================================================

    @PATCH("recruiter/applications/{applicationId}/status")
    Call<ShortlistResponse> updateApplicationStatus(
            @Path("applicationId") int applicationId,
            @Body ApplicantStatusRequest request
    );


    // =====================================================
    // UPDATE RECRUITMENT STATUS
    //
    // active
    // interviewed
    // offer_sent
    // hired
    // removed
    // =====================================================

    @PATCH("recruiter/applications/{applicationId}/recruitment-status")
    Call<ShortlistResponse> updateRecruitmentStatus(
            @Path("applicationId") int applicationId,
            @Body RecruitmentStatusRequest request
    );


}