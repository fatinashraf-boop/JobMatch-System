package com.example.jobmatch_frontend.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobmatch_frontend.R;
import com.example.jobmatch_frontend.adapters.BestMatchAdapter;
import com.example.jobmatch_frontend.api.ApiClient;
import com.example.jobmatch_frontend.api.JobseekerApi;
import com.example.jobmatch_frontend.models.JobseekerDashboardResponse;
import com.example.jobmatch_frontend.session.SessionManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecommendedJobsActivity extends AppCompatActivity {

    private RecyclerView recyclerViewRecommended;
    private TextView tvNoRecommendations;

    private BestMatchAdapter bestMatchAdapter;

    private final List<JobseekerDashboardResponse.BestMatch>
            recommendedJobs = new ArrayList<>();

    private JobseekerApi jobseekerApi;
    private SessionManager sessionManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_recommended_jobs
        );

        sessionManager =
                new SessionManager(this);

        jobseekerApi =
                ApiClient
                        .getClient(sessionManager)
                        .create(JobseekerApi.class);


        initializeViews();

        setupRecyclerView();
    }


    // =====================================================
    // INITIALIZE
    // =====================================================

    private void initializeViews() {

        recyclerViewRecommended =
                findViewById(
                        R.id.recyclerViewRecommended
                );

        tvNoRecommendations =
                findViewById(
                        R.id.tvNoRecommendations
                );
    }


    // =====================================================
    // RECYCLER VIEW
    // =====================================================

    private void setupRecyclerView() {

        recyclerViewRecommended.setLayoutManager(
                new LinearLayoutManager(this)
        );


        bestMatchAdapter =
                new BestMatchAdapter(
                        recommendedJobs,
                        match -> {

                            Intent intent =
                                    new Intent(
                                            RecommendedJobsActivity.this,
                                            JobDetailActivity.class
                                    );

                            intent.putExtra(
                                    "job_id",
                                    match.getJobId()
                            );

                            intent.putExtra(
                                    "match_score",
                                    match.getSimilarityScore()
                            );

                            startActivity(intent);
                        }
                );


        recyclerViewRecommended.setAdapter(
                bestMatchAdapter
        );
    }


    // =====================================================
    // LOAD REAL AI RECOMMENDATIONS
    // =====================================================

    private void loadRecommendedJobs() {

        jobseekerApi
                .getDashboard()
                .enqueue(
                        new Callback<JobseekerDashboardResponse>() {

                            @Override
                            public void onResponse(
                                    Call<JobseekerDashboardResponse> call,
                                    Response<JobseekerDashboardResponse> response
                            ) {

                                if (
                                        response.isSuccessful()
                                                &&
                                                response.body() != null
                                                &&
                                                response.body().isSuccess()
                                                &&
                                                response.body().getDashboard() != null
                                ) {

                                    List<JobseekerDashboardResponse.BestMatch>
                                            matches =
                                            response
                                                    .body()
                                                    .getDashboard()
                                                    .getBestMatches();


                                    recommendedJobs.clear();


                                    if (
                                            matches != null
                                                    &&
                                                    !matches.isEmpty()
                                    ) {

                                        recommendedJobs.addAll(
                                                matches
                                        );


                                        // Sort recommended jobs from highest AI score to lowest.
                                        Collections.sort(
                                                recommendedJobs,
                                                (first, second) -> Double.compare(
                                                        second.getSimilarityScore(),
                                                        first.getSimilarityScore()
                                                )
                                        );


                                        recyclerViewRecommended
                                                .setVisibility(
                                                        View.VISIBLE
                                                );

                                        tvNoRecommendations
                                                .setVisibility(
                                                        View.GONE
                                                );

                                    } else {

                                        recyclerViewRecommended
                                                .setVisibility(
                                                        View.GONE
                                                );

                                        tvNoRecommendations
                                                .setVisibility(
                                                        View.VISIBLE
                                                );
                                    }


                                    bestMatchAdapter
                                            .notifyDataSetChanged();

                                } else {

                                    showLoadError();
                                }
                            }


                            @Override
                            public void onFailure(
                                    Call<JobseekerDashboardResponse> call,
                                    Throwable t
                            ) {

                                android.util.Log.e(
                                        "RECOMMENDED_JOBS",
                                        "Failed to load recommendations",
                                        t
                                );

                                showLoadError();
                            }
                        }
                );
    }


    // =====================================================
    // ERROR
    // =====================================================

    private void showLoadError() {

        recyclerViewRecommended.setVisibility(
                View.GONE
        );

        tvNoRecommendations.setVisibility(
                View.VISIBLE
        );

        tvNoRecommendations.setText(
                "Unable to load recommended jobs."
        );

        Toast.makeText(
                RecommendedJobsActivity.this,
                "Unable to load job recommendations.",
                Toast.LENGTH_SHORT
        ).show();
    }


    // =====================================================
    // REFRESH
    // =====================================================

    @Override
    protected void onResume() {

        super.onResume();

        // Reload if returning from Job Detail.
        if (jobseekerApi != null) {
            loadRecommendedJobs();
        }
    }
}