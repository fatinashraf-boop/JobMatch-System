package com.example.jobmatch_frontend.activities;

import android.content.Intent;
import androidx.appcompat.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import com.example.jobmatch_frontend.api.RecruiterJobApi;
import com.example.jobmatch_frontend.models.RecruiterJobListResponse;
import java.util.List;
import com.bumptech.glide.Glide;
import com.example.jobmatch_frontend.api.RecruiterApi;
import com.example.jobmatch_frontend.models.RecruiterResponse;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobmatch_frontend.MainActivity;
import com.example.jobmatch_frontend.R;
import com.example.jobmatch_frontend.api.ApiClient;
import com.example.jobmatch_frontend.api.RecruiterDashboardApi;
import com.example.jobmatch_frontend.models.RecruiterDashboardResponse;
import com.example.jobmatch_frontend.session.SessionManager;

import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class EmployerDashboardActivity
        extends AppCompatActivity {


    // ==========================================
    // VIEWS
    // ==========================================

    private TextView tvGreeting;
    private TextView tvCompanyName;

    private TextView tvActiveJobsCount;
    private TextView tvApplicantsCount;
    private TextView tvShortlistedCount;

    private TextView tvViewMoreMatches;
    private TextView tvNoMatches;

    private Button btnViewAiMatches;

    private View navHome;
    private View navJobs;
    private View navApplicants;
    private View navAccount;
    private View btnCompanyProfile;
    private View tvSearch;
    private ImageView imgDashboardCompanyLogo;
    private ImageView imgDashboardCompanyFallback;
    private RecruiterApi recruiterApi;
    private RecruiterJobApi recruiterJobApi;
    private LinearLayout layoutDashboardJobs;
    private TextView tvDashboardJobsStatus;

    private View cardActiveJobs;
    private View cardApplicants;
    private View cardShortlisted;

    private RecyclerView recyclerViewCandidates;


    // ==========================================
    // SESSION + API
    // ==========================================

    private SessionManager sessionManager;

    private RecruiterDashboardApi
            recruiterDashboardApi;


    // ==========================================
    // ON CREATE
    // ==========================================

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_employer_dashboard
        );


        // --------------------------------------
        // SESSION
        // --------------------------------------

        sessionManager =
                new SessionManager(this);


        if (!sessionManager.isLoggedIn()) {

            Toast.makeText(
                    this,
                    "Please login again.",
                    Toast.LENGTH_LONG
            ).show();

            goToLogin();

            return;
        }


        if (
                !"recruiter".equalsIgnoreCase(
                        sessionManager.getRole()
                )
        ) {

            Toast.makeText(
                    this,
                    "Recruiter access only.",
                    Toast.LENGTH_LONG
            ).show();

            finish();

            return;
        }


        // --------------------------------------
        // INITIALIZE
        // --------------------------------------

        initializeViews();

        setupRecyclerView();

        setupApi();

        setupButtons();

        setGreeting();
    }


    // ==========================================
    // INITIALIZE VIEWS
    // ==========================================

    private void initializeViews() {

        tvGreeting =
                findViewById(
                        R.id.tvGreeting
                );


        tvCompanyName =
                findViewById(
                        R.id.tvCompanyName
                );


        tvActiveJobsCount =
                findViewById(
                        R.id.tvActiveJobsCount
                );


        tvApplicantsCount =
                findViewById(
                        R.id.tvApplicantsCount
                );


        tvShortlistedCount =
                findViewById(
                        R.id.tvShortlistedCount
                );


        tvViewMoreMatches =
                findViewById(
                        R.id.tvViewMoreMatches
                );


        tvNoMatches =
                findViewById(
                        R.id.tvNoMatches
                );


        btnViewAiMatches =
                findViewById(
                        R.id.btnViewAiMatches
                );


        navHome =
                findViewById(
                        R.id.navHome
                );

        navJobs =
                findViewById(
                        R.id.navJobs
                );

        navApplicants =
                findViewById(
                        R.id.navApplicants
                );

        navAccount =
                findViewById(
                        R.id.navAccount
                );

        btnCompanyProfile = findViewById(R.id.btnCompanyProfile);
        tvSearch = findViewById(R.id.tvSearch);
        layoutDashboardJobs = findViewById(R.id.layoutDashboardJobs);
        tvDashboardJobsStatus = findViewById(R.id.tvDashboardJobsStatus);
        imgDashboardCompanyLogo = findViewById(R.id.imgDashboardCompanyLogo);
        imgDashboardCompanyFallback = findViewById(R.id.imgDashboardCompanyFallback);


        cardActiveJobs =
                findViewById(
                        R.id.cardActiveJobs
                );

        cardApplicants =
                findViewById(
                        R.id.cardApplicants
                );

        cardShortlisted =
                findViewById(
                        R.id.cardShortlisted
                );

        recyclerViewCandidates =
                findViewById(
                        R.id.recyclerViewCandidates
                );
    }


    // ==========================================
    // RECYCLER VIEW
    // ==========================================

    private void setupRecyclerView() {

        // AI recommendations are job-specific.
        // The recruiter selects a job first from My Job Listings.

        recyclerViewCandidates.setVisibility(
                View.GONE
        );

        tvNoMatches.setVisibility(
                View.GONE
        );
    }


    // ==========================================
    // API
    // ==========================================

    private void setupApi() {

        recruiterDashboardApi =
                ApiClient
                        .getClient(
                                sessionManager
                        )
                        .create(
                                RecruiterDashboardApi.class
                        );
        recruiterApi = ApiClient.getClient(sessionManager).create(RecruiterApi.class);
        recruiterJobApi = ApiClient.getClient(sessionManager).create(RecruiterJobApi.class);
    }


    // ==========================================
    // BUTTONS
    // ==========================================

    private void setupButtons() {
        // Browse the recruiter's own vacancies or existing applicants.
        tvSearch.setOnClickListener(v ->
                startActivity(new Intent(this, RecruiterSearchActivity.class)));


        // ==========================================
        // ACTIVE JOB LISTINGS
        // Select a job to view its applicants and AI ranking.
        // ==========================================

        btnViewAiMatches.setOnClickListener(
                view -> openMyJobs()
        );


        tvViewMoreMatches.setOnClickListener(
                view -> openMyJobs()
        );


        // ==========================================
        // SUMMARY CARDS
        // ==========================================

        cardActiveJobs.setOnClickListener(
                view -> openMyJobs()
        );

        cardApplicants.setOnClickListener(
                view -> openApplicants()
        );

        cardShortlisted.setOnClickListener(
                view -> openShortlistedApplicants()
        );


        btnCompanyProfile.setOnClickListener(view -> {
            Intent intent = new Intent(EmployerDashboardActivity.this,
                    RecruiterProfileActivity.class);
            startActivity(intent);
        });

        // ==========================================
        // BOTTOM NAVIGATION
        // ==========================================

        navHome.setOnClickListener(
                view -> {
                    // Already on recruiter dashboard.
                }
        );


        navJobs.setOnClickListener(
                view -> openMyJobs()
        );


        navApplicants.setOnClickListener(
                view -> openApplicants()
        );


        navAccount.setOnClickListener(
                view -> {

                    Intent intent =
                            new Intent(
                                    EmployerDashboardActivity.this,
                                    RecruiterProfileActivity.class
                            );

                    startActivity(intent);
                }
        );
    }

    private void openMyJobs() {

        Intent intent =
                new Intent(
                        EmployerDashboardActivity.this,
                        MyJobListingsActivity.class
                );

        startActivity(intent);
    }

    private void openApplicants() {

        Intent intent =
                new Intent(
                        EmployerDashboardActivity.this,
                        RecruiterApplicantsActivity.class
                );

        startActivity(intent);
    }

    private void openShortlistedApplicants() {

        Intent intent =
                new Intent(
                        EmployerDashboardActivity.this,
                        ShortlistedApplicantsActivity.class
                );

        startActivity(intent);
    }

    // ==========================================
    // GREETING
    // ==========================================

    private void setGreeting() {

        int hour =
                Calendar
                        .getInstance()
                        .get(
                                Calendar.HOUR_OF_DAY
                        );


        if (hour < 12) {

            tvGreeting.setText(
                    "Good Morning"
            );

        } else if (hour < 18) {

            tvGreeting.setText(
                    "Good Afternoon"
            );

        } else {

            tvGreeting.setText(
                    "Good Evening"
            );
        }
    }


    // ==========================================
    // LOAD DASHBOARD
    // ==========================================

    private void loadDashboard() {

        recruiterDashboardApi
                .getDashboard()
                .enqueue(
                        new Callback<
                                RecruiterDashboardResponse
                                >() {


                            @Override
                            public void onResponse(
                                    Call<RecruiterDashboardResponse> call,
                                    Response<RecruiterDashboardResponse> response
                            ) {


                                if (
                                        response.isSuccessful()
                                                &&
                                                response.body() != null
                                                &&
                                                response.body().isSuccess()
                                ) {

                                    RecruiterDashboardResponse.Data data =
                                            response
                                                    .body()
                                                    .getData();


                                    if (data == null) {

                                        Toast.makeText(
                                                EmployerDashboardActivity.this,
                                                "Dashboard data not available.",
                                                Toast.LENGTH_LONG
                                        ).show();

                                        return;
                                    }


                                    // ------------------------------
                                    // COMPANY NAME
                                    // ------------------------------

                                    String companyName =
                                            data.getCompanyName();


                                    if (
                                            companyName == null
                                                    ||
                                                    companyName
                                                            .trim()
                                                            .isEmpty()
                                    ) {

                                        companyName =
                                                "Recruiter";
                                    }


                                    tvCompanyName.setText(
                                            companyName
                                    );


                                    // ------------------------------
                                    // ACTIVE JOBS
                                    // ------------------------------

                                    tvActiveJobsCount.setText(
                                            String.valueOf(
                                                    data.getActiveJobs()
                                            )
                                    );


                                    // ------------------------------
                                    // APPLICANTS
                                    // ------------------------------

                                    tvApplicantsCount.setText(
                                            String.valueOf(
                                                    data.getTotalApplicants()
                                            )
                                    );


                                    // ------------------------------
                                    // SHORTLISTED
                                    // ------------------------------

                                    tvShortlistedCount.setText(
                                            String.valueOf(
                                                    data.getShortlisted()
                                            )
                                    );


                                    return;
                                }


                                // ------------------------------
                                // UNAUTHORIZED
                                // ------------------------------

                                if (
                                        response.code() == 401
                                                ||
                                                response.code() == 403
                                ) {

                                    Toast.makeText(
                                            EmployerDashboardActivity.this,
                                            "Session expired. Please login again.",
                                            Toast.LENGTH_LONG
                                    ).show();


                                    sessionManager.logout();

                                    goToLogin();

                                    return;
                                }


                                Toast.makeText(
                                        EmployerDashboardActivity.this,
                                        "Unable to load recruiter dashboard.",
                                        Toast.LENGTH_LONG
                                ).show();
                            }


                            @Override
                            public void onFailure(
                                    Call<RecruiterDashboardResponse> call,
                                    Throwable t
                            ) {

                                Toast.makeText(
                                        EmployerDashboardActivity.this,
                                        "Connection error: "
                                                + t.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                );
    }


    // Load the same saved company logo used by RecruiterProfileActivity.
    private void loadCompanyLogo() {
        if (recruiterApi == null) return;
        recruiterApi.getMyProfile().enqueue(new Callback<RecruiterResponse>() {
            @Override
            public void onResponse(Call<RecruiterResponse> call,
                                   Response<RecruiterResponse> response) {
                if (!response.isSuccessful() || response.body() == null ||
                        !response.body().isSuccess() || response.body().getRecruiter() == null) {
                    showDefaultCompanyLogo();
                    return;
                }
                String logo = response.body().getRecruiter().getCompanyLogo();
                if (logo == null || logo.trim().isEmpty()) {
                    showDefaultCompanyLogo();
                    return;
                }
                String url = logo.startsWith("http://") || logo.startsWith("https://")
                        ? logo : ApiClient.SERVER_URL + (logo.startsWith("/") ? logo : "/" + logo);
                Glide.with(EmployerDashboardActivity.this)
                        .load(url)
                        .circleCrop()
                        .error(R.drawable.ic_profile)
                        .into(imgDashboardCompanyLogo);
                imgDashboardCompanyLogo.setVisibility(View.VISIBLE);
                imgDashboardCompanyFallback.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<RecruiterResponse> call, Throwable t) {
                showDefaultCompanyLogo();
            }
        });
    }

    private void showDefaultCompanyLogo() {
        Glide.with(this).clear(imgDashboardCompanyLogo);
        imgDashboardCompanyLogo.setVisibility(View.GONE);
        imgDashboardCompanyFallback.setVisibility(View.VISIBLE);
    }

    // ==========================================
    // REFRESH DASHBOARD
    // ==========================================

    @Override
    protected void onResume() {

        super.onResume();


        if (
                sessionManager != null
                        &&
                        sessionManager.isLoggedIn()
                        &&
                        recruiterDashboardApi != null
        ) {

            loadDashboard();
            loadCompanyLogo();
            loadDashboardJobs();
        }
    }

    private int jobDp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void loadDashboardJobs() {
        if (recruiterJobApi == null || layoutDashboardJobs == null) return;
        layoutDashboardJobs.removeAllViews();
        tvDashboardJobsStatus.setVisibility(View.VISIBLE);
        tvDashboardJobsStatus.setText("Loading your active jobs...");
        recruiterJobApi.getMyJobs().enqueue(new Callback<RecruiterJobListResponse>() {
            @Override public void onResponse(Call<RecruiterJobListResponse> call,
                    Response<RecruiterJobListResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                if (!response.isSuccessful() || response.body() == null
                        || !response.body().isSuccess()) {
                    tvDashboardJobsStatus.setText("Unable to load job listings.");
                    return;
                }
                List<RecruiterJobListResponse.RecruiterJob> jobs = response.body().getJobs();
                layoutDashboardJobs.removeAllViews();
                int count = 0;
                if (jobs != null) {
                    for (RecruiterJobListResponse.RecruiterJob job : jobs) {
                        if (job == null) continue;
                        count++;
                        View card = getLayoutInflater().inflate(
                                R.layout.item_recruiter_job, layoutDashboardJobs, false);
                        TextView title = card.findViewById(R.id.tvJobTitle);
                        TextView status = card.findViewById(R.id.tvStatus);
                        title.setText(job.getJobTitle() == null ? "Untitled job" : job.getJobTitle());
                        status.setText(job.getStatus() == null ? "Listed" : job.getStatus());

                        // Show the same read-only details as My Job Listings.
                        TextView info = card.findViewById(R.id.tvJobInfo);
                        TextView salary = card.findViewById(R.id.tvSalary);
                        TextView applicants = card.findViewById(R.id.tvApplicantCount);
                        info.setText(dashboardValue(job.getJobType(), "Job") + " • "
                                + dashboardValue(job.getLocation(), "Location not specified"));
                        salary.setText(formatDashboardSalary(job));
                        applicants.setText(String.format(Locale.getDefault(),
                                "%d Active Applicants • %d Shortlisted",
                                job.getApplicantCount(), job.getShortlistedCount()));
                        info.setVisibility(View.VISIBLE);
                        salary.setVisibility(View.VISIBLE);
                        applicants.setVisibility(View.VISIBLE);

                        // Management actions remain in My Job Listings.
                        card.findViewById(R.id.btnEditJob).setVisibility(View.GONE);
                        card.findViewById(R.id.btnChangeStatus).setVisibility(View.GONE);

                        View.OnClickListener openMatches = v -> {
                            Intent intent = new Intent(EmployerDashboardActivity.this,
                                    RecruiterAiMatchesActivity.class);
                            intent.putExtra("job_id", job.getJobId());
                            startActivity(intent);
                        };
                        card.findViewById(R.id.btnAiMatches).setOnClickListener(openMatches);
                        card.setOnClickListener(openMatches);
                        layoutDashboardJobs.addView(card);
                    }
                }
                tvDashboardJobsStatus.setText(count == 0
                        ? "No job listings returned. Tap View All to manage your jobs."
                        : count + (count == 1 ? " job listing" : " job listings"));
            }
            @Override public void onFailure(Call<RecruiterJobListResponse> call, Throwable t) {
                if (!isFinishing() && !isDestroyed())
                    tvDashboardJobsStatus.setText("Unable to load jobs. Check your connection.");
            }
        });
    }

    private String dashboardValue(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    // Matches the salary formatting used by RecruiterJobAdapter.
    private String formatDashboardSalary(RecruiterJobListResponse.RecruiterJob job) {
        Double min = job.getSalaryMin();
        Double max = job.getSalaryMax();
        if (min == null && max == null) return "Salary not specified";
        if (min != null && max != null) {
            return String.format(Locale.getDefault(), "RM %,.0f - RM %,.0f", min, max);
        }
        if (min != null) {
            return String.format(Locale.getDefault(), "From RM %,.0f", min);
        }
        return String.format(Locale.getDefault(), "Up to RM %,.0f", max);
    }

    // ==========================================
    // LOGIN SCREEN
    // ==========================================

    private void goToLogin() {

        Intent intent =
                new Intent(
                        EmployerDashboardActivity.this,
                        MainActivity.class
                );


        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );


        startActivity(intent);

        finish();
    }
}