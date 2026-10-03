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

public class ShortlistedApplicantsActivity
        extends AppCompatActivity {

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


    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_shortlisted_applicants
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


        applicantApi =
                ApiClient
                        .getClient(sessionManager)
                        .create(
                                RecruiterApplicantApi.class
                        );
    }


    private void loadShortlistedApplicants() {

        progressBar.setVisibility(
                View.VISIBLE
        );


        applicantApi
                .getShortlistedApplicants()
                .enqueue(
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
                                        ShortlistedApplicantsActivity.this,
                                        "Unable to load shortlisted applicants.",
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
                                        ShortlistedApplicantsActivity.this,
                                        "Unable to connect to server.",
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                );
    }


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


        tvApplicantCount.setText(
                count
                        + (
                        count == 1
                                ? " shortlisted applicant"
                                : " shortlisted applicants"
                )
        );


        boolean empty =
                applicantList.isEmpty();


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


    private void openCandidate(
            RecruiterApplicantResponse.Applicant applicant
    ) {

        if (applicant == null) {
            return;
        }


        Intent intent =
                new Intent(
                        ShortlistedApplicantsActivity.this,
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

            loadShortlistedApplicants();
        }
    }
}