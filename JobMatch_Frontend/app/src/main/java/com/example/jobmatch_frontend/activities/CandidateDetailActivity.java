package com.example.jobmatch_frontend.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.jobmatch_frontend.R;
import com.example.jobmatch_frontend.api.ApiClient;
import com.example.jobmatch_frontend.api.RecruiterMatchApi;
import com.example.jobmatch_frontend.models.CandidateDetailResponse;
import com.example.jobmatch_frontend.models.ShortlistResponse;
import com.example.jobmatch_frontend.session.SessionManager;
import com.example.jobmatch_frontend.api.RecruiterApplicantApi;
import com.example.jobmatch_frontend.models.ApplicantStatusRequest;
import com.example.jobmatch_frontend.models.RecruitmentStatusRequest;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CandidateDetailActivity
        extends AppCompatActivity {

    private Button btnMarkReviewed;
    private Button btnShortlist;
    private Button btnReject;

    private Button btnInterviewed;
    private Button btnOfferSent;
    private Button btnHired;
    private Button btnRemoved;

    private ImageButton btnBack;
    private TextView tvCandidateName;
    private TextView tvHeadline;
    private TextView tvLocation;
    private TextView tvExpectedSalary;
    private TextView tvEmail;
    private TextView tvPhone;
    private TextView tvMatchScore;
    private TextView tvMatchedJob;
    private TextView tvMatchReason;
    private TextView tvResumeSummary;
    private TextView tvSkills;
    private TextView tvEducation;
    private TextView tvExperience;
    private TextView tvApplicationStatus;
    private TextView tvShortlistStatus;
    private TextView tvDocuments;

    private TextView tvApplicationActionsTitle;
    private TextView tvRecruitmentActionsTitle;

    private ProgressBar progressBar;

    private SessionManager sessionManager;

    private RecruiterMatchApi recruiterMatchApi;
    private RecruiterApplicantApi recruiterApplicantApi;

    private int jobId = -1;
    private int profileId = -1;
    private int applicationId = -1;

    private boolean alreadyShortlisted = false;

    private String currentApplicationStatus = null;
    private String currentRecruitmentStatus = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_candidate_detail
        );


        // =================================================
        // SESSION
        // =================================================

        sessionManager =
                new SessionManager(this);


        if (!sessionManager.isLoggedIn()) {

            Toast.makeText(
                    this,
                    "Please login again.",
                    Toast.LENGTH_LONG
            ).show();

            finish();
            return;
        }


        if (!"recruiter".equalsIgnoreCase(
                sessionManager.getRole()
        )) {

            Toast.makeText(
                    this,
                    "Recruiter access only.",
                    Toast.LENGTH_LONG
            ).show();

            finish();
            return;
        }


        // =================================================
        // INTENT
        // =================================================

        applicationId =
                getIntent().getIntExtra(
                        "application_id",
                        -1
                );

        jobId =
                getIntent().getIntExtra(
                        "job_id",
                        -1
                );

        profileId =
                getIntent().getIntExtra(
                        "profile_id",
                        -1
                );


        if (jobId <= 0 || profileId <= 0) {

            Toast.makeText(
                    this,
                    "Candidate information is missing.",
                    Toast.LENGTH_LONG
            ).show();

            finish();
            return;
        }



        initializeViews();

        setupApis();

        setupButtons();

        loadCandidateDetail();
    }


    // =====================================================
    // INITIALIZE VIEWS
    // =====================================================

    private void initializeViews() {

        btnBack =
                findViewById(
                        R.id.btnBack
                );

        tvCandidateName =
                findViewById(
                        R.id.tvCandidateName
                );

        tvHeadline =
                findViewById(
                        R.id.tvHeadline
                );

        tvLocation =
                findViewById(
                        R.id.tvLocation
                );

        tvExpectedSalary =
                findViewById(
                        R.id.tvExpectedSalary
                );

        tvEmail =
                findViewById(
                        R.id.tvEmail
                );

        tvPhone =
                findViewById(
                        R.id.tvPhone
                );

        tvMatchScore =
                findViewById(
                        R.id.tvMatchScore
                );

        tvMatchedJob =
                findViewById(
                        R.id.tvMatchedJob
                );

        tvMatchReason =
                findViewById(
                        R.id.tvMatchReason
                );

        tvResumeSummary =
                findViewById(
                        R.id.tvResumeSummary
                );

        tvSkills =
                findViewById(
                        R.id.tvSkills
                );

        tvEducation =
                findViewById(
                        R.id.tvEducation
                );

        tvExperience =
                findViewById(
                        R.id.tvExperience
                );

        tvApplicationStatus =
                findViewById(
                        R.id.tvApplicationStatus
                );

        tvShortlistStatus =
                findViewById(
                        R.id.tvShortlistStatus
                );

        tvDocuments =
                findViewById(
                        R.id.tvDocuments
                );

        tvApplicationActionsTitle =
                findViewById(
                        R.id.tvApplicationActionsTitle
                );

        tvRecruitmentActionsTitle =
                findViewById(
                        R.id.tvRecruitmentActionsTitle
                );

        btnMarkReviewed =
                findViewById(
                        R.id.btnMarkReviewed
                );

        btnShortlist =
                findViewById(
                        R.id.btnShortlist
                );

        btnReject =
                findViewById(
                        R.id.btnReject
                );

        btnInterviewed =
                findViewById(
                        R.id.btnInterviewed
                );

        btnOfferSent =
                findViewById(
                        R.id.btnOfferSent
                );

        btnHired =
                findViewById(
                        R.id.btnHired
                );

        btnRemoved =
                findViewById(
                        R.id.btnRemoved
                );

        progressBar =
                findViewById(
                        R.id.progressBar
                );
    }


    // =====================================================
    // APIS
    // =====================================================

    private void setupApis() {

        recruiterMatchApi =
                ApiClient
                        .getClient(sessionManager)
                        .create(
                                RecruiterMatchApi.class
                        );

        recruiterApplicantApi =
                ApiClient
                        .getClient(sessionManager)
                        .create(
                                RecruiterApplicantApi.class
                        );
    }


    // =====================================================
    // BUTTONS
    // =====================================================

    private void setupButtons() {

        btnBack.setOnClickListener(
                v -> finish()
        );


        // =====================================================
        // APPLICATION ACTIONS
        // =====================================================

        btnMarkReviewed.setOnClickListener(
                v -> confirmApplicationStatus(
                        "reviewed",
                        "Mark as Reviewed",
                        "Mark this application as reviewed?"
                )
        );


        btnShortlist.setOnClickListener(
                v -> confirmShortlist()
        );


        btnReject.setOnClickListener(
                v -> confirmApplicationStatus(
                        "rejected",
                        "Reject Application",
                        "Are you sure you want to reject this application?"
                )
        );


        // =====================================================
        // RECRUITMENT ACTIONS
        // =====================================================

        btnInterviewed.setOnClickListener(
                v -> confirmRecruitmentStatus(
                        "interviewed",
                        "Mark as Interviewed",
                        "Has this candidate completed the interview stage?"
                )
        );


        btnOfferSent.setOnClickListener(
                v -> confirmRecruitmentStatus(
                        "offer_sent",
                        "Offer Sent",
                        "Mark this candidate as having received an offer?"
                )
        );


        btnHired.setOnClickListener(
                v -> confirmRecruitmentStatus(
                        "hired",
                        "Hire Candidate",
                        "Are you sure you want to mark this candidate as hired?"
                )
        );


        btnRemoved.setOnClickListener(
                v -> confirmRecruitmentStatus(
                        "removed",
                        "Remove from Shortlist",
                        "Remove this candidate from the recruitment shortlist?"
                )
        );
    }


    // =====================================================
    // LOAD CANDIDATE
    // =====================================================

    private void loadCandidateDetail() {

        setLoading(true);


        recruiterMatchApi
                .getCandidateDetail(
                        jobId,
                        profileId
                )
                .enqueue(
                        new Callback<CandidateDetailResponse>() {

                            @Override
                            public void onResponse(
                                    Call<CandidateDetailResponse> call,
                                    Response<CandidateDetailResponse> response
                            ) {

                                setLoading(false);


                                if (
                                        response.isSuccessful()
                                                &&
                                                response.body() != null
                                ) {

                                    CandidateDetailResponse result =
                                            response.body();


                                    if (
                                            result.isSuccess()
                                                    &&
                                                    result.getCandidate() != null
                                    ) {

                                        displayCandidate(
                                                result
                                        );

                                    } else {

                                        showMessage(
                                                result.getMessage() != null
                                                        ? result.getMessage()
                                                        : "Candidate not found."
                                        );
                                    }

                                } else {

                                    if (
                                            response.code() == 401
                                                    ||
                                                    response.code() == 403
                                    ) {

                                        showMessage(
                                                "You are not authorized to view this candidate."
                                        );

                                    } else if (
                                            response.code() == 404
                                    ) {

                                        showMessage(
                                                "Candidate match not found."
                                        );

                                    } else {

                                        showMessage(
                                                "Unable to load candidate details."
                                        );
                                    }
                                }
                            }


                            @Override
                            public void onFailure(
                                    Call<CandidateDetailResponse> call,
                                    Throwable t
                            ) {

                                setLoading(false);

                                showMessage(
                                        "Network error: "
                                                + t.getMessage()
                                );
                            }
                        }
                );
    }


    // =====================================================
    // DISPLAY CANDIDATE
    // =====================================================

    private void displayCandidate(
            CandidateDetailResponse result
    ) {

        CandidateDetailResponse.Candidate candidate =
                result.getCandidate();


        // -------------------------------------------------
        // PROFILE
        // -------------------------------------------------

        tvCandidateName.setText(
                valueOrDefault(
                        candidate.getFullName(),
                        "Candidate"
                )
        );


        tvHeadline.setText(
                valueOrDefault(
                        candidate.getHeadline(),
                        "No headline provided"
                )
        );


        tvLocation.setText(
                "Location: "
                        + valueOrDefault(
                        candidate.getLocation(),
                        "Not provided"
                )
        );


        if (candidate.getExpectedSalary() != null) {

            tvExpectedSalary.setText(
                    String.format(
                            Locale.getDefault(),
                            "Expected Salary: RM %,.0f",
                            candidate.getExpectedSalary()
                    )
            );

        } else {

            tvExpectedSalary.setText(
                    "Expected Salary: Not provided"
            );
        }


        tvEmail.setText(
                "Email: "
                        + valueOrDefault(
                        candidate.getEmail(),
                        "Not provided"
                )
        );


        tvPhone.setText(
                "Phone: "
                        + valueOrDefault(
                        candidate.getPhoneNumber(),
                        "Not provided"
                )
        );


        tvResumeSummary.setText(
                valueOrDefault(
                        candidate.getResumeSummary(),
                        "No resume summary provided."
                )
        );


        tvSkills.setText(
                valueOrDefault(
                        candidate.getSkills(),
                        "No skills provided."
                )
        );


        tvEducation.setText(
                valueOrDefault(
                        candidate.getEducation(),
                        "No education information provided."
                )
        );


        tvExperience.setText(
                valueOrDefault(
                        candidate.getExperience(),
                        "No experience information provided."
                )
        );


        // -------------------------------------------------
        // JOB
        // -------------------------------------------------

        if (result.getJob() != null) {

            tvMatchedJob.setText(
                    "Matched for: "
                            + valueOrDefault(
                            result
                                    .getJob()
                                    .getJobTitle(),
                            "Job"
                    )
            );

        } else {

            tvMatchedJob.setText(
                    "Matched Job"
            );
        }


        // -------------------------------------------------
        // MATCH
        // -------------------------------------------------

        if (result.getMatch() != null) {

            CandidateDetailResponse.Match match =
                    result.getMatch();


            tvMatchScore.setText(
                    String.format(
                            Locale.getDefault(),
                            "%.0f%% Match",
                            match.getSimilarityScore()
                    )
            );


            tvMatchReason.setText(
                    valueOrDefault(
                            match.getMatchReason(),
                            "No match explanation available."
                    )
            );

        } else {

            tvMatchScore.setText(
                    "Match score unavailable"
            );

            tvMatchReason.setText(
                    "No match explanation available."
            );
        }

        // -------------------------------------------------
        // APPLICATION
        // -------------------------------------------------

        if (result.getApplication() != null) {

            int responseApplicationId =
                    result
                            .getApplication()
                            .getApplicationId();

            if (responseApplicationId > 0) {

                applicationId =
                        responseApplicationId;
            }


            currentApplicationStatus =
                    result
                            .getApplication()
                            .getApplicationStatus();


            tvApplicationStatus.setText(
                    "Status: "
                            + formatStatus(
                            currentApplicationStatus
                    )
            );
        } else {

            applicationId = -1;

            currentApplicationStatus = null;

            tvApplicationStatus.setText(
                    "Status: Not applied"
            );
        }


        // -------------------------------------------------
        // SHORTLIST
        // -------------------------------------------------

        if (result.getShortlist() != null) {

            currentRecruitmentStatus =
                    result
                            .getShortlist()
                            .getShortlistStatus();


            alreadyShortlisted =
                    currentRecruitmentStatus != null
                            &&
                            !"removed".equalsIgnoreCase(
                                    currentRecruitmentStatus
                            );


            tvShortlistStatus.setText(
                    "Recruitment Status: "
                            + formatStatus(
                            currentRecruitmentStatus
                    )
            );

        } else {

            alreadyShortlisted = false;

            currentRecruitmentStatus = null;

            tvShortlistStatus.setText(
                    "Recruitment Status: Not shortlisted"
            );
        }


        updateActionButtons();

        // -------------------------------------------------
        // DOCUMENTS
        // -------------------------------------------------

        displayDocuments(
                result.getDocuments()
        );
    }


    // =====================================================
    // DOCUMENT / OCR DISPLAY
    // =====================================================

    private void displayDocuments(
            List<CandidateDetailResponse.Document> documents
    ) {

        if (
                documents == null
                        ||
                        documents.isEmpty()
        ) {

            tvDocuments.setText(
                    "No documents available."
            );

            return;
        }


        StringBuilder builder =
                new StringBuilder();


        for (
                CandidateDetailResponse.Document document
                :
                documents
        ) {

            // File name
            builder.append(
                    valueOrDefault(
                            document.getFileName(),
                            "Document"
                    )
            );


            // Document type
            builder.append("\n");

            builder.append(
                    "Type: "
                            + formatStatus(
                            document.getDocumentType()
                    )
            );


            CandidateDetailResponse.Ocr ocr =
                    document.getOcr();


            if (ocr != null) {

                // OCR confidence
                if (
                        ocr.getConfidenceScore()
                                != null
                ) {

                    builder.append("\n");

                    builder.append(
                            String.format(
                                    Locale.getDefault(),
                                    "OCR Confidence: %.1f%%",
                                    ocr.getConfidenceScore()
                            )
                    );
                }


                // Issuer - useful for certificates
                if (
                        ocr.getExtractedIssuer() != null
                                &&
                                !ocr.getExtractedIssuer()
                                        .trim()
                                        .isEmpty()
                ) {

                    builder.append("\n");

                    builder.append(
                            "Issuer: "
                                    + ocr.getExtractedIssuer()
                    );
                }

            } else {

                builder.append(
                        "\nOCR: Not available"
                );
            }


            builder.append(
                    "\n\n--------------------\n\n"
            );
        }


        tvDocuments.setText(
                builder.toString().trim()
        );
    }

    private void updateActionButtons() {

        // =====================================================
        // RESET
        // =====================================================

        btnMarkReviewed.setVisibility(
                View.GONE
        );

        btnShortlist.setVisibility(
                View.GONE
        );

        btnReject.setVisibility(
                View.GONE
        );

        btnInterviewed.setVisibility(
                View.GONE
        );

        btnOfferSent.setVisibility(
                View.GONE
        );

        btnHired.setVisibility(
                View.GONE
        );

        btnRemoved.setVisibility(
                View.GONE
        );

        tvApplicationActionsTitle.setVisibility(
                View.GONE
        );

        tvRecruitmentActionsTitle.setVisibility(
                View.GONE
        );


        // =====================================================
        // MATCHED CANDIDATE WHO HAS NOT APPLIED
        // =====================================================

        if (applicationId <= 0) {

            return;
        }


        String applicationStatus =
                currentApplicationStatus == null
                        ? ""
                        : currentApplicationStatus
                          .trim()
                          .toLowerCase();


        String recruitmentStatus =
                currentRecruitmentStatus == null
                        ? ""
                        : currentRecruitmentStatus
                          .trim()
                          .toLowerCase();

        // =====================================================
        // TERMINAL APPLICATION STATES
        // =====================================================

                if (
                        "withdrawn".equals(applicationStatus)
                                ||
                                "rejected".equals(applicationStatus)
                                ||
                                "hired".equals(applicationStatus)
                ) {

                    return;
                }

        // =====================================================
        // NOT YET SHORTLISTED
        // =====================================================

        if (!alreadyShortlisted) {

            tvApplicationActionsTitle.setVisibility(
                    View.VISIBLE
            );


            // Pending can be reviewed.

            if ("pending".equals(applicationStatus)) {

                btnMarkReviewed.setVisibility(
                        View.VISIBLE
                );
            }


            // Pending or reviewed can be shortlisted/rejected.

            if (
                    "pending".equals(applicationStatus)
                            ||
                            "reviewed".equals(applicationStatus)
            ) {

                btnShortlist.setVisibility(
                        View.VISIBLE
                );

                btnShortlist.setText(
                        "Shortlist Candidate"
                );

                btnShortlist.setEnabled(
                        true
                );


                btnReject.setVisibility(
                        View.VISIBLE
                );
            }


            return;
        }


        // =====================================================
        // SHORTLISTED CANDIDATE
        // =====================================================

        tvRecruitmentActionsTitle.setVisibility(
                View.VISIBLE
        );


        switch (recruitmentStatus) {

            case "active":

                btnInterviewed.setVisibility(
                        View.VISIBLE
                );

                btnRemoved.setVisibility(
                        View.VISIBLE
                );

                break;


            case "interviewed":

                btnOfferSent.setVisibility(
                        View.VISIBLE
                );

                btnRemoved.setVisibility(
                        View.VISIBLE
                );

                break;


            case "offer_sent":

                btnHired.setVisibility(
                        View.VISIBLE
                );

                btnRemoved.setVisibility(
                        View.VISIBLE
                );

                break;


            case "hired":

                // Final successful state.
                // No further action.

                break;


            case "removed":

                // No active recruitment action.
                break;


            default:

                btnRemoved.setVisibility(
                        View.VISIBLE
                );

                break;
        }
    }

    // =====================================================
    // CONFIRM SHORTLIST
    // =====================================================

    private void confirmShortlist() {

        if (alreadyShortlisted) {

            showMessage(
                    "This candidate is already shortlisted."
            );

            return;
        }


        if (applicationId == -1) {

            showMessage(
                    "Application information is unavailable."
            );

            return;
        }


        new AlertDialog.Builder(this)
                .setTitle(
                        "Shortlist Candidate"
                )
                .setMessage(
                        "Do you want to shortlist this candidate?"
                )
                .setPositiveButton(
                        "Shortlist",
                        (dialog, which) ->
                                shortlistCandidate()
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .show();
    }


    // =====================================================
    // SHORTLIST CANDIDATE
    // =====================================================

    private void shortlistCandidate() {

        if (applicationId <= 0) {

            showMessage(
                    "Application information is unavailable."
            );

            return;
        }

        updateApplicationStatus(
                "shortlisted"
        );
    }

    // =====================================================
    // LOADING
    // =====================================================

    private void setLoading(
            boolean loading
    ) {

        progressBar.setVisibility(
                loading
                        ? View.VISIBLE
                        : View.GONE
        );
    }


    // =====================================================
    // HELPERS
    // =====================================================

    private String valueOrDefault(
            String value,
            String defaultValue
    ) {

        if (
                value == null
                        ||
                        value.trim().isEmpty()
        ) {

            return defaultValue;
        }


        return value.trim();
    }


    private String formatStatus(
            String value
    ) {

        if (
                value == null
                        ||
                        value.trim().isEmpty()
        ) {

            return "Unknown";
        }


        String clean =
                value
                        .replace("_", " ")
                        .trim();


        if (clean.length() == 1) {

            return clean.toUpperCase(
                    Locale.getDefault()
            );
        }


        return clean
                .substring(0, 1)
                .toUpperCase(
                        Locale.getDefault()
                )
                +
                clean.substring(1)
                        .toLowerCase(
                                Locale.getDefault()
                        );
    }


    private void showMessage(
            String message
    ) {

        Toast.makeText(
                this,
                message,
                Toast.LENGTH_LONG
        ).show();
    }

    private void confirmApplicationStatus(
            String status,
            String title,
            String message
    ) {

        if (applicationId <= 0) {

            showMessage(
                    "Application information is unavailable."
            );

            return;
        }


        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(
                        "Confirm",
                        (dialog, which) ->
                                updateApplicationStatus(
                                        status
                                )
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .show();
    }

    private void updateApplicationStatus(
            String status
    ) {

        setLoading(true);

        setActionButtonsEnabled(false);


        ApplicantStatusRequest request =
                new ApplicantStatusRequest(
                        status
                );


        recruiterApplicantApi
                .updateApplicationStatus(
                        applicationId,
                        request
                )
                .enqueue(
                        new Callback<ShortlistResponse>() {

                            @Override
                            public void onResponse(
                                    Call<ShortlistResponse> call,
                                    Response<ShortlistResponse> response
                            ) {

                                setLoading(false);

                                setActionButtonsEnabled(true);


                                if (
                                        response.isSuccessful()
                                                &&
                                                response.body() != null
                                ) {

                                    ShortlistResponse result =
                                            response.body();


                                    if (result.isSuccess()) {

                                        Toast.makeText(
                                                CandidateDetailActivity.this,
                                                "Application updated to "
                                                        + formatStatus(status)
                                                        + ".",
                                                Toast.LENGTH_SHORT
                                        ).show();


                                        // Always reload authoritative DB state.

                                        loadCandidateDetail();

                                    } else {

                                        showMessage(
                                                result.getMessage() != null
                                                        ? result.getMessage()
                                                        : "Unable to update application."
                                        );
                                    }

                                } else if (
                                        response.code() == 403
                                ) {

                                    showMessage(
                                            "You are not allowed to update this application."
                                    );

                                } else if (
                                        response.code() == 409
                                ) {

                                    showMessage(
                                            "This application cannot be changed to that status."
                                    );

                                    loadCandidateDetail();

                                } else {

                                    showMessage(
                                            "Unable to update application."
                                    );
                                }
                            }


                            @Override
                            public void onFailure(
                                    Call<ShortlistResponse> call,
                                    Throwable t
                            ) {

                                setLoading(false);

                                setActionButtonsEnabled(true);


                                showMessage(
                                        "Network error: "
                                                + t.getMessage()
                                );
                            }
                        }
                );
    }

    private void confirmRecruitmentStatus(
            String status,
            String title,
            String message
    ) {

        if (applicationId <= 0) {

            showMessage(
                    "Application information is unavailable."
            );

            return;
        }


        if (!alreadyShortlisted) {

            showMessage(
                    "Candidate must be shortlisted first."
            );

            return;
        }


        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(
                        "Confirm",
                        (dialog, which) ->
                                updateRecruitmentStatus(
                                        status
                                )
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .show();
    }

    private void updateRecruitmentStatus(
            String status
    ) {

        setLoading(true);

        setActionButtonsEnabled(false);


        RecruitmentStatusRequest request =
                new RecruitmentStatusRequest(
                        status
                );


        recruiterApplicantApi
                .updateRecruitmentStatus(
                        applicationId,
                        request
                )
                .enqueue(
                        new Callback<ShortlistResponse>() {

                            @Override
                            public void onResponse(
                                    Call<ShortlistResponse> call,
                                    Response<ShortlistResponse> response
                            ) {

                                setLoading(false);

                                setActionButtonsEnabled(true);


                                if (
                                        response.isSuccessful()
                                                &&
                                                response.body() != null
                                ) {

                                    ShortlistResponse result =
                                            response.body();


                                    if (result.isSuccess()) {

                                        Toast.makeText(
                                                CandidateDetailActivity.this,
                                                "Recruitment status updated to "
                                                        + formatStatus(status)
                                                        + ".",
                                                Toast.LENGTH_SHORT
                                        ).show();


                                        loadCandidateDetail();

                                    } else {

                                        showMessage(
                                                result.getMessage() != null
                                                        ? result.getMessage()
                                                        : "Unable to update recruitment status."
                                        );
                                    }

                                } else if (
                                        response.code() == 403
                                ) {

                                    showMessage(
                                            "You are not allowed to update this candidate."
                                    );

                                } else if (
                                        response.code() == 404
                                ) {

                                    showMessage(
                                            "Shortlist record was not found."
                                    );

                                    loadCandidateDetail();

                                } else if (
                                        response.code() == 409
                                ) {

                                    showMessage(
                                            "This recruitment status change is not allowed."
                                    );

                                    loadCandidateDetail();

                                } else {

                                    showMessage(
                                            "Unable to update recruitment status."
                                    );
                                }
                            }


                            @Override
                            public void onFailure(
                                    Call<ShortlistResponse> call,
                                    Throwable t
                            ) {

                                setLoading(false);

                                setActionButtonsEnabled(true);


                                showMessage(
                                        "Network error: "
                                                + t.getMessage()
                                );
                            }
                        }
                );
    }

    private void setActionButtonsEnabled(boolean enabled) {
        btnMarkReviewed.setEnabled(enabled);
        btnShortlist.setEnabled(enabled);
        btnReject.setEnabled(enabled);
        btnInterviewed.setEnabled(enabled);
        btnOfferSent.setEnabled(enabled);
        btnHired.setEnabled(enabled);
        btnRemoved.setEnabled(enabled);
    }


}