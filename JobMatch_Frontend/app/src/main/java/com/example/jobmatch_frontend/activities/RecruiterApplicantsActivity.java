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

public class RecruiterApplicantsActivity
        extends AppCompatActivity {

    private TextView tvApplicantCount;
    private TextView tvApplicantSubtitle;
    private TextView tvNoApplicants;

    private TextView btnActive;
    private TextView btnHistory;

    private ProgressBar progressBar;

    private RecyclerView recyclerViewApplicants;

    private SessionManager sessionManager;
    private RecruiterApplicantApi applicantApi;

    private RecruiterApplicantAdapter adapter;

    private final List<RecruiterApplicantResponse.Applicant>
            applicantList =
            new ArrayList<>();


    // true = Active
    // false = History
    private boolean showingActive = true;


    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_recruiter_applicants
        );


        sessionManager =
                new SessionManager(this);


        if (!sessionManager.isLoggedIn()) {

            finish();
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


        // -----------------------------------------
        // VIEWS
        // -----------------------------------------

        tvApplicantCount =
                findViewById(
                        R.id.tvApplicantCount
                );

        tvApplicantSubtitle =
                findViewById(
                        R.id.tvApplicantSubtitle
                );

        tvNoApplicants =
                findViewById(
                        R.id.tvNoApplicants
                );

        btnActive =
                findViewById(
                        R.id.btnActive
                );

        btnHistory =
                findViewById(
                        R.id.btnHistory
                );

        progressBar =
                findViewById(
                        R.id.progressBar
                );

        recyclerViewApplicants =
                findViewById(
                        R.id.recyclerViewApplicants
                );


        // -----------------------------------------
        // RECYCLER VIEW
        // -----------------------------------------

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


        // -----------------------------------------
        // API
        // -----------------------------------------

        applicantApi =
                ApiClient
                        .getClient(sessionManager)
                        .create(
                                RecruiterApplicantApi.class
                        );


        // -----------------------------------------
        // ACTIVE TAB
        // -----------------------------------------

        btnActive.setOnClickListener(v -> {

            if (!showingActive) {

                showingActive = true;

                updateTabAppearance();

                loadApplicants();
            }
        });


        // -----------------------------------------
        // HISTORY TAB
        // -----------------------------------------

        btnHistory.setOnClickListener(v -> {

            if (showingActive) {

                showingActive = false;

                updateTabAppearance();

                loadApplicants();
            }
        });


        updateTabAppearance();
    }


    // =====================================================
    // UPDATE TAB APPEARANCE
    // =====================================================

    private void updateTabAppearance() {

        if (showingActive) {

            btnActive.setBackgroundColor(
                    0xFF00A7C4
            );

            btnActive.setTextColor(
                    0xFFFFFFFF
            );


            btnHistory.setBackgroundColor(
                    0xFFEEEEEE
            );

            btnHistory.setTextColor(
                    0xFF555555
            );


            tvApplicantSubtitle.setText(
                    "Active applicants ranked by AI job-match score"
            );

        } else {

            btnHistory.setBackgroundColor(
                    0xFF00A7C4
            );

            btnHistory.setTextColor(
                    0xFFFFFFFF
            );


            btnActive.setBackgroundColor(
                    0xFFEEEEEE
            );

            btnActive.setTextColor(
                    0xFF555555
            );


            tvApplicantSubtitle.setText(
                    "Completed and inactive applications"
            );
        }
    }


    // =====================================================
    // LOAD ACTIVE OR HISTORY
    // =====================================================

    private void loadApplicants() {

        progressBar.setVisibility(
                View.VISIBLE
        );


        tvNoApplicants.setVisibility(
                View.GONE
        );


        Call<RecruiterApplicantResponse> call;


        if (showingActive) {

            call =
                    applicantApi
                            .getAllApplicants();

        } else {

            call =
                    applicantApi
                            .getApplicantHistory();
        }


        call.enqueue(
                new Callback<RecruiterApplicantResponse>() {

                    @Override
                    public void onResponse(
                            Call<RecruiterApplicantResponse> call,
                            Response<RecruiterApplicantResponse> response
                    ) {

                        progressBar.setVisibility(
                                View.GONE
                        );


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

                            return;
                        }


                        Toast.makeText(
                                RecruiterApplicantsActivity.this,
                                showingActive
                                        ? "Unable to load active applicants."
                                        : "Unable to load applicant history.",
                                Toast.LENGTH_LONG
                        ).show();
                    }


                    @Override
                    public void onFailure(
                            Call<RecruiterApplicantResponse> call,
                            Throwable t
                    ) {

                        progressBar.setVisibility(
                                View.GONE
                        );


                        Toast.makeText(
                                RecruiterApplicantsActivity.this,
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

        applicantList.clear();


        List<RecruiterApplicantResponse.Applicant>
                applicants =
                response.getApplicants();


        if (applicants != null) {

            applicantList.addAll(
                    applicants
            );
        }


        int count =
                applicantList.size();


        if (showingActive) {

            tvApplicantCount.setText(
                    count
                            + (
                            count == 1
                                    ? " active applicant"
                                    : " active applicants"
                    )
            );

        } else {

            tvApplicantCount.setText(
                    count
                            + (
                            count == 1
                                    ? " historical application"
                                    : " historical applications"
                    )
            );
        }


        boolean empty =
                applicantList.isEmpty();


        if (empty) {

            tvNoApplicants.setText(
                    showingActive
                            ? "No active applicants."
                            : "No applicant history yet."
            );
        }


        tvNoApplicants.setVisibility(
                empty
                        ? View.VISIBLE
                        : View.GONE
        );


        recyclerViewApplicants.setVisibility(
                empty
                        ? View.GONE
                        : View.VISIBLE
        );


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


        Intent intent =
                new Intent(
                        RecruiterApplicantsActivity.this,
                        CandidateDetailActivity.class
                );


        intent.putExtra(
                "application_id",
                applicant.getApplicationId()
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
    // REFRESH CURRENT TAB
    // =====================================================

    @Override
    protected void onResume() {

        super.onResume();


        if (
                applicantApi != null
                        &&
                        sessionManager != null
                        &&
                        sessionManager.isLoggedIn()
        ) {

            loadApplicants();
        }
    }
}