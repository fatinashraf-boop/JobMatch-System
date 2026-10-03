package com.example.jobmatch_frontend.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobmatch_frontend.R;
import com.example.jobmatch_frontend.adapters.JobBrowseAdapter;
import com.example.jobmatch_frontend.api.ApiClient;
import com.example.jobmatch_frontend.api.JobApi;
import com.example.jobmatch_frontend.models.Job;
import com.example.jobmatch_frontend.models.JobSearchResponse;
import com.example.jobmatch_frontend.session.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BrowseJobsActivity
        extends AppCompatActivity {

    private EditText etSearch;
    private EditText etLocation;

    private Spinner spinnerJobType;

    private TextView tvResultCount;
    private TextView tvNoJobs;

    private ProgressBar progressBar;

    private RecyclerView recyclerViewJobs;

    private View btnBack;
    private View btnSearch;

    private View navHome;
    private View navJobs;
    private View navApplications;
    private View navAccount;

    private JobBrowseAdapter adapter;

    private JobApi jobApi;
    private SessionManager sessionManager;

    private final List<Job> jobs =
            new ArrayList<>();


    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_browse_jobs
        );

        initializeViews();

        sessionManager =
                new SessionManager(this);

        jobApi =
                ApiClient
                        .getClient(sessionManager)
                        .create(JobApi.class);

        setupRecyclerView();

        setupListeners();

        setupInitialSearch();

        loadJobs();
    }


    private void initializeViews() {

        etSearch =
                findViewById(
                        R.id.etSearch
                );

        etLocation =
                findViewById(
                        R.id.etLocation
                );

        spinnerJobType =
                findViewById(
                        R.id.spinnerJobType
                );

        tvResultCount =
                findViewById(
                        R.id.tvResultCount
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

        btnBack =
                findViewById(
                        R.id.btnBack
                );

        btnSearch =
                findViewById(
                        R.id.btnSearch
                );

        navHome =
                findViewById(
                        R.id.navHome
                );

        navJobs =
                findViewById(
                        R.id.navJobs
                );

        navApplications =
                findViewById(
                        R.id.navApplications
                );

        navAccount =
                findViewById(
                        R.id.navAccount
                );
    }


    private void setupRecyclerView() {

        adapter =
                new JobBrowseAdapter(
                        jobs,
                        this::openJobDetail
                );

        recyclerViewJobs.setLayoutManager(
                new LinearLayoutManager(this)
        );

        recyclerViewJobs.setAdapter(
                adapter
        );
    }


    private void setupInitialSearch() {

        String keyword =
                getIntent().getStringExtra(
                        "keyword"
                );

        if (
                keyword != null
                        &&
                        !keyword.trim().isEmpty()
        ) {

            etSearch.setText(
                    keyword.trim()
            );
        }
    }


    private void setupListeners() {

        btnBack.setOnClickListener(
                view -> finish()
        );


        btnSearch.setOnClickListener(
                view -> loadJobs()
        );


        etSearch.setOnEditorActionListener(
                (v, actionId, event) -> {

                    if (
                            actionId
                                    == EditorInfo.IME_ACTION_SEARCH
                    ) {

                        loadJobs();

                        return true;
                    }

                    return false;
                }
        );


        navHome.setOnClickListener(
                view -> {

                    Intent intent =
                            new Intent(
                                    BrowseJobsActivity.this,
                                    JobseekerDashboardActivity.class
                            );

                    intent.addFlags(
                            Intent.FLAG_ACTIVITY_CLEAR_TOP
                    );

                    startActivity(intent);
                }
        );


        navJobs.setOnClickListener(
                view -> {
                    // Already on Jobs.
                }
        );


        navApplications.setOnClickListener(
                view -> {

                    Intent intent =
                            new Intent(
                                    BrowseJobsActivity.this,
                                    MyApplicationsActivity.class
                            );

                    startActivity(intent);
                }
        );


        navAccount.setOnClickListener(
                view -> {

                    Intent intent =
                            new Intent(
                                    BrowseJobsActivity.this,
                                    JobseekerAccountActivity.class
                            );

                    startActivity(intent);
                }
        );
    }


    private void loadJobs() {

        String keyword =
                etSearch
                        .getText()
                        .toString()
                        .trim();

        String location =
                etLocation
                        .getText()
                        .toString()
                        .trim();

        String selectedType =
                spinnerJobType
                        .getSelectedItem()
                        .toString();

        String jobType = null;

        if (
                !selectedType.equalsIgnoreCase(
                        "All Job Types"
                )
        ) {

            jobType = selectedType;
        }


        setLoading(true);


        jobApi.searchJobs(
                        emptyToNull(keyword),
                        emptyToNull(location),
                        jobType
                )
                .enqueue(
                        new Callback<JobSearchResponse>() {

                            @Override
                            public void onResponse(
                                    Call<JobSearchResponse> call,
                                    Response<JobSearchResponse> response
                            ) {

                                setLoading(false);

                                if (
                                        response.isSuccessful()
                                                &&
                                                response.body() != null
                                                &&
                                                response.body().isSuccess()
                                ) {

                                    displayJobs(
                                            response
                                                    .body()
                                                    .getData()
                                    );

                                    return;
                                }

                                displayJobs(
                                        new ArrayList<>()
                                );

                                Toast.makeText(
                                        BrowseJobsActivity.this,
                                        "Unable to load jobs.",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }


                            @Override
                            public void onFailure(
                                    Call<JobSearchResponse> call,
                                    Throwable t
                            ) {

                                setLoading(false);

                                displayJobs(
                                        new ArrayList<>()
                                );

                                Toast.makeText(
                                        BrowseJobsActivity.this,
                                        "Network error: "
                                                + t.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                );
    }


    private void displayJobs(
            List<Job> result
    ) {

        jobs.clear();

        if (result != null) {
            jobs.addAll(result);
        }

        adapter.notifyDataSetChanged();


        int count =
                jobs.size();


        if (count == 1) {

            tvResultCount.setText(
                    "1 job found"
            );

        } else {

            tvResultCount.setText(
                    count + " jobs found"
            );
        }


        boolean empty =
                jobs.isEmpty();

        tvNoJobs.setVisibility(
                empty
                        ? View.VISIBLE
                        : View.GONE
        );

        recyclerViewJobs.setVisibility(
                empty
                        ? View.GONE
                        : View.VISIBLE
        );
    }


    private void openJobDetail(
            Job job
    ) {

        if (
                job == null
                        ||
                        job.getJobId() <= 0
        ) {
            return;
        }

        Intent intent =
                new Intent(
                        BrowseJobsActivity.this,
                        JobDetailActivity.class
                );

        intent.putExtra(
                "job_id",
                job.getJobId()
        );

        startActivity(intent);
    }


    private String emptyToNull(
            String value
    ) {

        if (
                value == null
                        ||
                        value.trim().isEmpty()
        ) {

            return null;
        }

        return value.trim();
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