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
import com.example.jobmatch_frontend.adapters.RecruiterBestMatchAdapter;
import com.example.jobmatch_frontend.api.ApiClient;
import com.example.jobmatch_frontend.api.MatchScoreApi;
import com.example.jobmatch_frontend.models.MatchScore;
import com.example.jobmatch_frontend.models.MatchScoreResponse;
import com.example.jobmatch_frontend.session.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecruiterAiMatchesActivity
        extends AppCompatActivity {

    private int jobId = -1;

    private RecyclerView recyclerViewMatches;
    private TextView tvNoMatches;
    private ProgressBar progressBar;

    private RecruiterBestMatchAdapter adapter;

    private SessionManager sessionManager;
    private MatchScoreApi matchScoreApi;

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_recruiter_ai_matches
        );

        jobId =
                getIntent().getIntExtra(
                        "job_id",
                        -1
                );

        if (jobId <= 0) {

            Toast.makeText(
                    this,
                    "Invalid job selected.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        sessionManager =
                new SessionManager(this);

        recyclerViewMatches =
                findViewById(
                        R.id.recyclerViewMatches
                );

        tvNoMatches =
                findViewById(
                        R.id.tvNoMatches
                );

        progressBar =
                findViewById(
                        R.id.progressBar
                );

        matchScoreApi =
                ApiClient
                        .getClient(sessionManager)
                        .create(MatchScoreApi.class);

        setupRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Refresh AI matches whenever this screen becomes visible.
        // Rejected and withdrawn candidates are excluded by the backend.
        if (jobId > 0 && matchScoreApi != null && adapter != null) {
            loadMatches();
        }
    }

    private void setupRecyclerView() {

        adapter =
                new RecruiterBestMatchAdapter(
                        this::openCandidate
                );

        recyclerViewMatches.setLayoutManager(
                new LinearLayoutManager(this)
        );

        recyclerViewMatches.setAdapter(
                adapter
        );
    }

    private void loadMatches() {

        setLoading(true);

        matchScoreApi
                .getJobMatches(jobId)
                .enqueue(
                        new Callback<MatchScoreResponse>() {

                            @Override
                            public void onResponse(
                                    Call<MatchScoreResponse> call,
                                    Response<MatchScoreResponse> response
                            ) {

                                setLoading(false);

                                if (
                                        response.isSuccessful()
                                                &&
                                                response.body() != null
                                                &&
                                                response.body().isSuccess()
                                ) {

                                    adapter.setMatches(
                                            response
                                                    .body()
                                                    .getData()
                                    );

                                    int count =
                                            response
                                                    .body()
                                                    .getCount();

                                    tvNoMatches.setVisibility(
                                            count == 0
                                                    ? View.VISIBLE
                                                    : View.GONE
                                    );

                                    recyclerViewMatches.setVisibility(
                                            count == 0
                                                    ? View.GONE
                                                    : View.VISIBLE
                                    );

                                    return;
                                }

                                tvNoMatches.setVisibility(
                                        View.VISIBLE
                                );

                                recyclerViewMatches.setVisibility(
                                        View.GONE
                                );

                                Toast.makeText(
                                        RecruiterAiMatchesActivity.this,
                                        "Unable to load AI matches.",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }

                            @Override
                            public void onFailure(
                                    Call<MatchScoreResponse> call,
                                    Throwable t
                            ) {

                                setLoading(false);

                                tvNoMatches.setVisibility(
                                        View.VISIBLE
                                );

                                recyclerViewMatches.setVisibility(
                                        View.GONE
                                );

                                Toast.makeText(
                                        RecruiterAiMatchesActivity.this,
                                        "Network error: "
                                                + t.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                );
    }

    private void openCandidate(
            MatchScore match
    ) {

        if (match == null) {
            return;
        }

        Intent intent =
                new Intent(
                        this,
                        CandidateDetailActivity.class
                );

        intent.putExtra(
                "job_id",
                match.getJobId()
        );

        intent.putExtra(
                "profile_id",
                match.getProfileId()
        );

        startActivity(intent);
    }

    private void setLoading(
            boolean loading
    ) {

        progressBar.setVisibility(
                loading
                        ? View.VISIBLE
                        : View.GONE
        );
    }
}