package com.example.jobmatch_frontend.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobmatch_frontend.R;
import com.example.jobmatch_frontend.adapters.RecruiterApplicantAdapter;
import com.example.jobmatch_frontend.api.ApiClient;
import com.example.jobmatch_frontend.api.RecruiterApplicantApi;
import com.example.jobmatch_frontend.models.RecruiterApplicantResponse;
import com.example.jobmatch_frontend.session.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CandidateMatchActivity
        extends AppCompatActivity {

    private TextView tvJobTitle;
    private TextView tvApplicantCount;
    private TextView tvNoApplicants;

    private ProgressBar progressBar;

    private RecyclerView recyclerViewApplicants;


    private SessionManager sessionManager;

    private RecruiterApplicantApi applicantApi;

    private RecruiterApplicantAdapter adapter;


    private final List<RecruiterApplicantResponse.Applicant>
            applicantList =
            new ArrayList<>();


    private int jobId = -1;


    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_candidate_match
        );


        sessionManager =
                new SessionManager(this);


        // =================================================
        // SESSION CHECK
        // =================================================

        if (!sessionManager.isLoggedIn()) {

            openLogin();

            return;
        }


        if (
                !"recruiter".equalsIgnoreCase(
                        sessionManager.getRole()
                )
        ) {

            Toast.makeText(
                    this,
                    "Recruiter account required.",
                    Toast.LENGTH_LONG
            ).show();

            finish();

            return;
        }


        // =================================================
        // JOB ID
        // =================================================

        jobId =
                getIntent().getIntExtra(
                        "job_id",
                        -1
                );


        if (jobId <= 0) {

            Toast.makeText(
                    this,
                    "Invalid job.",
                    Toast.LENGTH_LONG
            ).show();

            finish();

            return;
        }


        initializeViews();

        setupRecyclerView();


        applicantApi =
                ApiClient
                        .getClient(sessionManager)
                        .create(
                                RecruiterApplicantApi.class
                        );
    }


    // =====================================================
    // INITIALIZE
    // =====================================================

    private void initializeViews() {

        tvJobTitle =
                findViewById(
                        R.id.tvJobTitle
                );

        tvApplicantCount =
                findViewById(
                        R.id.tvApplicantCount
                );

        tvNoApplicants =
                findViewById(
                        R.id.tvNoApplicants
                );

        progressBar =
                findViewById(
                        R.id.progressBar
                );

        recyclerViewApplicants =
                findViewById(
                        R.id.recyclerViewApplicants
                );
    }


    // =====================================================
    // RECYCLER VIEW
    // =====================================================

    private void setupRecyclerView() {

        recyclerViewApplicants.setLayoutManager(
                new LinearLayoutManager(this)
        );


        adapter =
                new RecruiterApplicantAdapter(
                        applicantList,
                        this::openCandidate
                );


        recyclerViewApplicants.setAdapter(
                adapter
        );
    }


    // =====================================================
    // LOAD APPLICANTS
    // =====================================================

    private void loadApplicants() {

        showLoading(true);


        applicantApi
                .getJobApplicants(jobId)
                .enqueue(
                        new Callback<RecruiterApplicantResponse>() {

                            @Override
                            public void onResponse(
                                    Call<RecruiterApplicantResponse> call,
                                    Response<RecruiterApplicantResponse> response
                            ) {

                                showLoading(false);


                                if (
                                        response.isSuccessful()
                                                &&
                                                response.body() != null
                                                &&
                                                response.body().isSuccess()
                                ) {

                                    displayApplicants(
                                            response.body()
                                    );

                                } else {

                                    Toast.makeText(
                                            CandidateMatchActivity.this,
                                            "Unable to load applicants.",
                                            Toast.LENGTH_LONG
                                    ).show();
                                }
                            }


                            @Override
                            public void onFailure(
                                    Call<RecruiterApplicantResponse> call,
                                    Throwable t
                            ) {

                                showLoading(false);


                                android.util.Log.e(
                                        "RECRUITER_APPLICANTS",
                                        "Applicant loading error",
                                        t
                                );


                                Toast.makeText(
                                        CandidateMatchActivity.this,
                                        "Unable to connect to server.",
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                );
    }


    // =====================================================
    // DISPLAY
    // =====================================================

    private void displayApplicants(
            RecruiterApplicantResponse response
    ) {

        // JOB

        if (response.getJob() != null) {

            String title =
                    response.getJob()
                            .getJobTitle();


            if (
                    title == null
                            ||
                            title.trim().isEmpty()
            ) {

                title =
                        "Job #" + jobId;
            }


            tvJobTitle.setText(
                    title
            );

        } else {

            tvJobTitle.setText(
                    "Job #" + jobId
            );
        }


        // COUNT

        int count =
                response.getCount();


        tvApplicantCount.setText(
                count
                        + (
                        count == 1
                                ? " applicant"
                                : " applicants"
                )
        );


        // LIST

        applicantList.clear();


        List<RecruiterApplicantResponse.Applicant>
                applicants =
                response.getApplicants();


        if (
                applicants == null
                        ||
                        applicants.isEmpty()
        ) {

            tvNoApplicants.setVisibility(
                    View.VISIBLE
            );

            recyclerViewApplicants.setVisibility(
                    View.GONE
            );

        } else {

            applicantList.addAll(
                    applicants
            );


            tvNoApplicants.setVisibility(
                    View.GONE
            );

            recyclerViewApplicants.setVisibility(
                    View.VISIBLE
            );
        }


        adapter.notifyDataSetChanged();
    }


    // =====================================================
    // OPEN CANDIDATE
    // =====================================================

    private void openCandidate(
            RecruiterApplicantResponse.Applicant applicant
    ) {

        if (applicant == null) {
            return;
        }

        int applicationId =
                applicant.getApplicationId();

        if (applicationId <= 0) {

            Toast.makeText(
                    this,
                    "Application information is unavailable.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        Intent intent =
                new Intent(
                        CandidateMatchActivity.this,
                        CandidateDetailActivity.class
                );

        intent.putExtra(
                "application_id",
                applicationId
        );

        intent.putExtra(
                "job_id",
                applicant.getJobId()
        );

        intent.putExtra(
                "profile_id",
                applicant.getProfileId()
        );

        startActivity(intent);
    }

    // =====================================================
    // LOADING
    // =====================================================

    private void showLoading(
            boolean loading
    ) {

        progressBar.setVisibility(
                loading
                        ? View.VISIBLE
                        : View.GONE
        );
    }


    // =====================================================
    // LOGIN
    // =====================================================

    private void openLogin() {

        Intent intent =
                new Intent(
                        CandidateMatchActivity.this,
                        LoginActivity.class
                );


        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );


        startActivity(intent);

        finish();
    }


    // =====================================================
    // REFRESH
    // =====================================================

    @Override
    protected void onResume() {

        super.onResume();


        if (
                applicantApi != null
                        &&
                        jobId > 0
                        &&
                        sessionManager != null
                        &&
                        sessionManager.isLoggedIn()
        ) {

            loadApplicants();
        }
    }
}