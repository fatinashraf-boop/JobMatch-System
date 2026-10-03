package com.example.jobmatch_frontend.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobmatch_frontend.R;
import com.example.jobmatch_frontend.adapters.RecruiterJobAdapter;
import com.example.jobmatch_frontend.api.ApiClient;
import com.example.jobmatch_frontend.api.RecruiterJobApi;
import com.example.jobmatch_frontend.models.JobStatusRequest;
import com.example.jobmatch_frontend.models.RecruiterJobListResponse;
import com.example.jobmatch_frontend.models.ShortlistResponse;
import com.example.jobmatch_frontend.session.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MyJobListingsActivity
        extends AppCompatActivity {


    private TextView tvJobCount;
    private TextView tvNoJobs;

    private ProgressBar progressBar;

    private RecyclerView recyclerViewJobs;

    private Button btnCreateJob;


    private SessionManager sessionManager;

    private RecruiterJobApi recruiterJobApi;

    private RecruiterJobAdapter adapter;


    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {

        super.onCreate(
                savedInstanceState
        );

        setContentView(
                R.layout.activity_my_job_listings
        );


        sessionManager =
                new SessionManager(this);


        if (
                sessionManager.getToken() == null ||
                        sessionManager.getToken().isEmpty()
        ) {

            showMessage(
                    "Session expired. Please log in again."
            );

            finish();

            return;
        }


        if (
                sessionManager.getRole() == null ||
                        !sessionManager
                                .getRole()
                                .equalsIgnoreCase(
                                        "recruiter"
                                )
        ) {

            showMessage(
                    "Recruiter access only."
            );

            finish();

            return;
        }


        initializeViews();

        setupApi();

        setupRecyclerView();

        setupButtons();
    }


    @Override
    protected void onResume() {

        super.onResume();

        loadJobs();
    }


    private void initializeViews() {

        tvJobCount =
                findViewById(
                        R.id.tvJobCount
                );

        tvNoJobs =
                findViewById(
                        R.id.tvNoJobs
                );

        progressBar =
                findViewById(
                        R.id.progressBar
                );

        recyclerViewJobs =
                findViewById(
                        R.id.recyclerViewJobs
                );

        btnCreateJob =
                findViewById(
                        R.id.btnCreateJob
                );
    }


    private void setupApi() {

        recruiterJobApi =
                ApiClient
                        .getClient(
                                sessionManager
                        )
                        .create(
                                RecruiterJobApi.class
                        );
    }


    private void setupRecyclerView() {

        adapter =
                new RecruiterJobAdapter(
                        new RecruiterJobAdapter.OnJobActionListener() {

                            @Override
                            public void onViewAiMatches(
                                    RecruiterJobListResponse.RecruiterJob job
                            ) {

                                openAiMatches(
                                        job
                                );
                            }

                            @Override
                            public void onEditJob(
                                    RecruiterJobListResponse.RecruiterJob job
                            ) {

                                openEditJob(
                                        job
                                );
                            }


                            @Override
                            public void onChangeStatus(
                                    RecruiterJobListResponse.RecruiterJob job
                            ) {

                                confirmStatusChange(
                                        job
                                );
                            }
                        }
                );


        recyclerViewJobs.setLayoutManager(
                new LinearLayoutManager(
                        this
                )
        );


        recyclerViewJobs.setAdapter(
                adapter
        );
    }


    private void setupButtons() {

        btnCreateJob.setOnClickListener(
                view -> {

                    Intent intent =
                            new Intent(
                                    MyJobListingsActivity.this,
                                    CreateJobActivity.class
                            );

                    startActivity(
                            intent
                    );
                }
        );
    }


    private void loadJobs() {

        setLoading(
                true
        );


        recruiterJobApi
                .getMyJobs()
                .enqueue(
                        new Callback<RecruiterJobListResponse>() {

                            @Override
                            public void onResponse(
                                    Call<RecruiterJobListResponse> call,
                                    Response<RecruiterJobListResponse> response
                            ) {

                                setLoading(
                                        false
                                );


                                if (
                                        response.isSuccessful() &&
                                                response.body() != null &&
                                                response.body().isSuccess()
                                ) {

                                    List<RecruiterJobListResponse.RecruiterJob>
                                            jobs =
                                            response
                                                    .body()
                                                    .getJobs();


                                    adapter.setJobs(
                                            jobs
                                    );


                                    int count =
                                            jobs == null
                                                    ? 0
                                                    : jobs.size();


                                    tvJobCount.setText(
                                            count == 1
                                                    ? "1 job"
                                                    : count + " jobs"
                                    );


                                    tvNoJobs.setVisibility(
                                            count == 0
                                                    ? View.VISIBLE
                                                    : View.GONE
                                    );


                                    recyclerViewJobs.setVisibility(
                                            count == 0
                                                    ? View.GONE
                                                    : View.VISIBLE
                                    );


                                    return;
                                }


                                if (
                                        response.code() == 401
                                ) {

                                    showMessage(
                                            "Session expired."
                                    );

                                } else if (
                                        response.code() == 403
                                ) {

                                    showMessage(
                                            "Recruiter access only."
                                    );

                                } else {

                                    showMessage(
                                            "Unable to load job listings."
                                    );
                                }
                            }


                            @Override
                            public void onFailure(
                                    Call<RecruiterJobListResponse> call,
                                    Throwable throwable
                            ) {

                                setLoading(
                                        false
                                );


                                showMessage(
                                        "Network error: "
                                                +
                                                throwable.getMessage()
                                );
                            }
                        }
                );
    }


    private void openAiMatches(
            RecruiterJobListResponse.RecruiterJob job
    ) {

        if (job == null || job.getJobId() <= 0) {

            Toast.makeText(
                    this,
                    "Unable to open AI matches.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        Intent intent =
                new Intent(
                        MyJobListingsActivity.this,
                        RecruiterAiMatchesActivity.class
                );

        intent.putExtra(
                "job_id",
                job.getJobId()
        );

        startActivity(intent);
    }


    private void openEditJob(
            RecruiterJobListResponse.RecruiterJob job
    ) {

        Intent intent =
                new Intent(
                        this,
                        CreateJobActivity.class
                );


        intent.putExtra(
                "edit_mode",
                true
        );


        intent.putExtra(
                "job_id",
                job.getJobId()
        );


        startActivity(
                intent
        );
    }


    private void confirmStatusChange(
            RecruiterJobListResponse.RecruiterJob job
    ) {

        String currentStatus =
                job.getStatus();


        String targetStatus;

        String title;

        String message;


        if (
                "closed".equalsIgnoreCase(
                        currentStatus
                )
        ) {

            targetStatus =
                    "open";

            title =
                    "Reopen Job";

            message =
                    "Make this job available to job seekers again?";

        } else if (
                "draft".equalsIgnoreCase(
                        currentStatus
                )
        ) {

            targetStatus =
                    "open";

            title =
                    "Publish Job";

            message =
                    "Publish this draft job?";

        } else {

            targetStatus =
                    "closed";

            title =
                    "Close Job";

            message =
                    "Close this job listing? Job seekers will no longer be able to apply.";
        }


        new AlertDialog.Builder(
                this
        )
                .setTitle(
                        title
                )
                .setMessage(
                        message
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .setPositiveButton(
                        "Confirm",
                        (
                                dialog,
                                which
                        ) ->
                                updateJobStatus(
                                        job.getJobId(),
                                        targetStatus
                                )
                )
                .show();
    }


    private void updateJobStatus(
            int jobId,
            String status
    ) {

        setLoading(
                true
        );


        JobStatusRequest request =
                new JobStatusRequest(
                        status
                );


        recruiterJobApi
                .updateJobStatus(
                        jobId,
                        request
                )
                .enqueue(
                        new Callback<ShortlistResponse>() {

                            @Override
                            public void onResponse(
                                    Call<ShortlistResponse> call,
                                    Response<ShortlistResponse> response
                            ) {

                                setLoading(
                                        false
                                );


                                if (
                                        response.isSuccessful() &&
                                                response.body() != null &&
                                                response.body().isSuccess()
                                ) {

                                    showMessage(
                                            response
                                                    .body()
                                                    .getMessage()
                                    );


                                    loadJobs();

                                    return;
                                }


                                if (
                                        response.code() == 403
                                ) {

                                    showMessage(
                                            "You are not allowed to modify this job."
                                    );

                                } else if (
                                        response.code() == 404
                                ) {

                                    showMessage(
                                            "Job not found."
                                    );

                                } else {

                                    showMessage(
                                            "Unable to update job status."
                                    );
                                }
                            }


                            @Override
                            public void onFailure(
                                    Call<ShortlistResponse> call,
                                    Throwable throwable
                            ) {

                                setLoading(
                                        false
                                );


                                showMessage(
                                        "Network error: "
                                                +
                                                throwable.getMessage()
                                );
                            }
                        }
                );
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


    private void showMessage(
            String message
    ) {

        Toast.makeText(
                this,
                message == null
                        ? "Something went wrong."
                        : message,
                Toast.LENGTH_SHORT
        ).show();
    }
}