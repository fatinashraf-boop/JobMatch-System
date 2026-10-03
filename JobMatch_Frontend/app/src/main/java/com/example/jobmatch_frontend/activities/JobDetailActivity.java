package com.example.jobmatch_frontend.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import com.example.jobmatch_frontend.R;
import com.example.jobmatch_frontend.api.ApiClient;
import com.example.jobmatch_frontend.api.JobApi;
import com.example.jobmatch_frontend.models.Application;
import com.example.jobmatch_frontend.models.ApplicationRequest;
import com.example.jobmatch_frontend.models.ApplicationResponse;
import com.example.jobmatch_frontend.models.Job;
import com.example.jobmatch_frontend.models.JobDetailResponse;
import com.example.jobmatch_frontend.session.SessionManager;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class JobDetailActivity
        extends AppCompatActivity {

    // ==========================================
    // DATA
    // ==========================================

    private int currentJobId = -1;

    private Job currentJob;


    // ==========================================
    // UI
    // ==========================================

    private TextView tvJobTitle;
    private TextView tvMatchBadge;
    private TextView tvLocationSalary;
    private TextView tvDescription;
    private TextView tvSkills;
    private TextView tvQualifications;

    private Button btnApply;


    // ==========================================
    // API / SESSION
    // ==========================================

    private SessionManager sessionManager;

    private JobApi jobApi;


    // ==========================================
    // ON CREATE
    // ==========================================

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_job_detail
        );


        // ======================================
        // SESSION
        // ======================================

        sessionManager =
                new SessionManager(this);


        // ======================================
        // GET JOB ID
        // ======================================

        currentJobId =
                getIntent().getIntExtra(
                        "job_id",
                        -1
                );


        // ======================================
        // INITIALIZE VIEWS
        // ======================================

        tvJobTitle =
                findViewById(
                        R.id.tvJobTitle
                );

        tvMatchBadge =
                findViewById(
                        R.id.tvMatchBadge
                );

        tvLocationSalary =
                findViewById(
                        R.id.tvLocationSalary
                );

        tvDescription =
                findViewById(
                        R.id.tvDescription
                );

        tvSkills =
                findViewById(
                        R.id.tvSkills
                );

        tvQualifications =
                findViewById(
                        R.id.tvQualifications
                );

        btnApply =
                findViewById(
                        R.id.btnApply
                );


        // ======================================
        // CREATE AUTHENTICATED API
        // ======================================

        jobApi =
                ApiClient
                        .getClient(
                                sessionManager
                        )
                        .create(
                                JobApi.class
                        );


        // ======================================
        // VALIDATE JOB ID
        // ======================================

        if (currentJobId <= 0) {

            Toast.makeText(
                    this,
                    "Invalid job selected.",
                    Toast.LENGTH_LONG
            ).show();

            finish();

            return;
        }


        // ======================================
        // MATCH BADGE
        // ======================================

        setupMatchBadge();


        // ======================================
        // DISABLE APPLY WHILE LOADING
        // ======================================

        btnApply.setEnabled(false);

        btnApply.setText(
                "Loading..."
        );


        // ======================================
        // LOAD JOB
        // ======================================

        loadJobDetails();


        // ======================================
        // APPLY BUTTON
        // ======================================

        btnApply.setOnClickListener(
                v ->
                        handleApplicationSubmit()
        );
    }


    // ==========================================
    // LOAD JOB DETAIL
    // ==========================================

    private void loadJobDetails() {

        jobApi
                .getJobById(
                        currentJobId
                )
                .enqueue(
                        new Callback<JobDetailResponse>() {

                            @Override
                            public void onResponse(
                                    Call<JobDetailResponse> call,
                                    Response<JobDetailResponse> response
                            ) {

                                if (
                                        response.isSuccessful()
                                                &&
                                                response.body() != null
                                                &&
                                                response.body().isSuccess()
                                                &&
                                                response.body().getData() != null
                                ) {

                                    currentJob =
                                            response
                                                    .body()
                                                    .getData();


                                    displayJobDetails(
                                            currentJob
                                    );

                                } else {

                                    btnApply.setEnabled(
                                            false
                                    );

                                    btnApply.setText(
                                            "Unavailable"
                                    );


                                    Toast.makeText(
                                            JobDetailActivity.this,
                                            "Unable to load job details.",
                                            Toast.LENGTH_LONG
                                    ).show();
                                }
                            }


                            @Override
                            public void onFailure(
                                    Call<JobDetailResponse> call,
                                    Throwable t
                            ) {

                                btnApply.setEnabled(
                                        false
                                );

                                btnApply.setText(
                                        "Unavailable"
                                );


                                Toast.makeText(
                                        JobDetailActivity.this,
                                        "Connection error: "
                                                +
                                                t.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                );
    }


    // ==========================================
    // DISPLAY JOB DETAILS
    // ==========================================

    private void displayJobDetails(
            Job job
    ) {

        // ======================================
        // TITLE
        // ======================================

        tvJobTitle.setText(
                safeText(
                        job.getJobTitle(),
                        "Job"
                )
        );


        // ======================================
        // LOCATION + SALARY
        // ======================================

        String location =
                safeText(
                        job.getLocation(),
                        "Location not specified"
                );


        String salary =
                formatSalary(
                        job.getSalaryMin(),
                        job.getSalaryMax()
                );


        tvLocationSalary.setText(
                location
                        +
                        "  •  "
                        +
                        salary
        );


        // ======================================
        // DESCRIPTION
        // ======================================

        tvDescription.setText(
                safeText(
                        job.getJobDescription(),
                        "No job description provided."
                )
        );


        // ======================================
        // SKILLS
        // ======================================

        tvSkills.setText(
                safeText(
                        job.getRequiredSkills(),
                        "Not specified"
                )
        );


        // ======================================
        // QUALIFICATIONS
        // ======================================

        tvQualifications.setText(
                safeText(
                        job.getQualifications(),
                        "Not specified"
                )
        );


        // ======================================
        // JOB STATUS / APPLY
        // ======================================

        String status =
                job.getStatus();


        if (
                status != null
                        &&
                        "open".equalsIgnoreCase(
                                status.trim()
                        )
        ) {

            btnApply.setEnabled(
                    true
            );

            btnApply.setText(
                    "Apply Now"
            );

        } else {

            btnApply.setEnabled(
                    false
            );

            btnApply.setText(
                    "Job Closed"
            );
        }
    }


    // ==========================================
    // MATCH BADGE
    // ==========================================

    private void setupMatchBadge() {

        double matchScore =
                getIntent().getDoubleExtra(
                        "match_score",
                        -1
                );


        if (matchScore < 0) {

            tvMatchBadge.setVisibility(
                    View.GONE
            );

            return;
        }


        // If score is stored as 0.85,
        // convert to 85%.
        if (
                matchScore >= 0
                        &&
                        matchScore <= 1
        ) {

            matchScore =
                    matchScore * 100;
        }


        tvMatchBadge.setVisibility(
                View.VISIBLE
        );


        tvMatchBadge.setText(
                String.format(
                        Locale.getDefault(),
                        "Match: %.0f%%",
                        matchScore
                )
        );
    }


    // ==========================================
    // FORMAT SALARY
    // ==========================================

    private String formatSalary(
            double min,
            double max
    ) {

        if (
                min > 0
                        &&
                        max > 0
        ) {

            return String.format(
                    Locale.getDefault(),
                    "RM %,.0f - RM %,.0f",
                    min,
                    max
            );
        }


        if (min > 0) {

            return String.format(
                    Locale.getDefault(),
                    "From RM %,.0f",
                    min
            );
        }


        if (max > 0) {

            return String.format(
                    Locale.getDefault(),
                    "Up to RM %,.0f",
                    max
            );
        }


        return "Salary not specified";
    }


    // ==========================================
    // SAFE TEXT
    // ==========================================

    private String safeText(
            String value,
            String fallback
    ) {

        if (
                value == null
                        ||
                        value.trim().isEmpty()
        ) {

            return fallback;
        }

        return value.trim();
    }


// ==========================================
// APPLY FOR JOB
// ==========================================

    private void handleApplicationSubmit() {

        // ======================================
        // VALIDATE JOB
        // ======================================

        if (currentJobId <= 0) {

            Toast.makeText(
                    this,
                    "Invalid job selected.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        if (currentJob == null) {

            Toast.makeText(
                    this,
                    "Job details are still loading.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // ======================================
        // MAKE SURE JOB IS OPEN
        // ======================================

        if (
                currentJob.getStatus() == null
                        ||
                        !"open".equalsIgnoreCase(
                                currentJob
                                        .getStatus()
                                        .trim()
                        )
        ) {

            Toast.makeText(
                    this,
                    "This job is no longer open.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // ======================================
        // REQUEST
        //
        // ONLY job_id is sent.
        //
        // profile_id is resolved by backend
        // from the JWT user_id.
        // ======================================

        ApplicationRequest request =
                new ApplicationRequest(
                        currentJobId
                );


        // ======================================
        // PREVENT DOUBLE TAP
        // ======================================

        btnApply.setEnabled(false);

        btnApply.setText(
                "Applying..."
        );


        // ======================================
        // SUBMIT APPLICATION
        // ======================================

        jobApi
                .applyForJob(
                        request
                )
                .enqueue(
                        new Callback<ApplicationResponse>() {

                            @Override
                            public void onResponse(
                                    Call<ApplicationResponse> call,
                                    Response<ApplicationResponse> response
                            ) {

                                // ==============================
                                // SUCCESS
                                // ==============================

                                if (
                                        response.isSuccessful()
                                                &&
                                                response.body() != null
                                                &&
                                                response.body().isSuccess()
                                ) {

                                    Toast.makeText(
                                            JobDetailActivity.this,
                                            "Application submitted successfully",
                                            Toast.LENGTH_SHORT
                                    ).show();


                                    Intent intent =
                                            new Intent(
                                                    JobDetailActivity.this,
                                                    MyApplicationsActivity.class
                                            );

                                    startActivity(intent);

                                    finish();

                                    return;
                                }

                                // ==============================
                                // DUPLICATE APPLICATION
                                // HTTP 409
                                // ==============================

                                if (
                                        response.code() == 409
                                ) {

                                    btnApply.setEnabled(false);

                                    btnApply.setText(
                                            "Already Applied"
                                    );


                                    Toast.makeText(
                                            JobDetailActivity.this,
                                            "You have already applied for this job.",
                                            Toast.LENGTH_LONG
                                    ).show();


                                    return;
                                }


                                // ==============================
                                // OTHER ERROR
                                // ==============================

                                btnApply.setEnabled(true);

                                btnApply.setText(
                                        "Apply Now"
                                );


                                Toast.makeText(
                                        JobDetailActivity.this,
                                        "Unable to submit application.",
                                        Toast.LENGTH_LONG
                                ).show();
                            }


                            @Override
                            public void onFailure(
                                    Call<ApplicationResponse> call,
                                    Throwable t
                            ) {

                                btnApply.setEnabled(true);

                                btnApply.setText(
                                        "Apply Now"
                                );


                                Toast.makeText(
                                        JobDetailActivity.this,
                                        "Connection error: "
                                                +
                                                t.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                );
    }
}