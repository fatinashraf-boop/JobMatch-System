package com.example.jobmatch_frontend.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import android.graphics.drawable.Drawable;

import androidx.annotation.Nullable;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobmatch_frontend.R;
import com.example.jobmatch_frontend.api.ApiClient;
import com.example.jobmatch_frontend.api.JobseekerApi;
import com.example.jobmatch_frontend.models.JobseekerDashboardResponse;
import com.example.jobmatch_frontend.adapters.BestMatchAdapter;
import com.example.jobmatch_frontend.session.SessionManager;
import com.example.jobmatch_frontend.api.ProfileApi;
import com.example.jobmatch_frontend.models.ProfileResponse;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class JobseekerDashboardActivity
        extends AppCompatActivity {


    private TextView tvWelcomeUser;
    private TextView tvUserHeadline;

    private TextView tvJobsApplied;
    private TextView tvInterviews;
    private TextView tvNoMatches;

    private TextView tvMoreMatches;

    private View navHome;
    private View navJobs;
    private View navApplications;
    private View navAccount;

    private View cardJobsApplied;
    private View cardInterviews;
    private View btnCompleteProfile;
    private ImageView imgDashboardProfile;
    private ImageView imgDashboardProfileFallback;
    private TextView btnBrowseJobs;
    private Button btnMyApplications;


    private RecyclerView recyclerViewJobs;


    private SessionManager sessionManager;

    private JobseekerApi jobseekerApi;
    private ProfileApi profileApi;

    private BestMatchAdapter bestMatchAdapter;

    private final List<JobseekerDashboardResponse.BestMatch>
            bestMatchList =
            new ArrayList<>();

    private String getGreeting() {

        int hour =
                Calendar
                        .getInstance()
                        .get(Calendar.HOUR_OF_DAY);

        if (hour < 12) {
            return "Good Morning";
        }

        if (hour < 18) {
            return "Good Afternoon";
        }

        return "Good Evening";
    }

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_jobseeker_dashboard
        );


        sessionManager =
                new SessionManager(this);


        // =============================================
        // SESSION CHECK
        // =============================================

        if (!sessionManager.isLoggedIn()) {

            openLogin();

            return;
        }


        if (!"jobseeker".equalsIgnoreCase(
                sessionManager.getRole()
        )) {

            Toast.makeText(
                    this,
                    "Job seeker account required.",
                    Toast.LENGTH_LONG
            ).show();

            finish();

            return;
        }


        initializeViews();

        setupRecyclerView();

        setupNavigation();


        jobseekerApi =
                ApiClient
                        .getClient(sessionManager)
                        .create(JobseekerApi.class);


        profileApi =
                ApiClient
                        .getClient(sessionManager)
                        .create(ProfileApi.class);
    }

    // =====================================================
    // INITIALIZE
    // =====================================================

    private void initializeViews() {

        tvWelcomeUser =
                findViewById(
                        R.id.tvWelcomeUser
                );


        tvUserHeadline =
                findViewById(
                        R.id.tvUserHeadline
                );


        tvJobsApplied =
                findViewById(
                        R.id.tvJobsApplied
                );


        tvInterviews =
                findViewById(
                        R.id.tvInterviews
                );


        tvNoMatches =
                findViewById(
                        R.id.tvNoMatches
                );

        tvMoreMatches =
                findViewById(
                        R.id.tvMoreMatches
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

        cardJobsApplied =
                findViewById(
                        R.id.cardJobsApplied
                );

        cardInterviews =
                findViewById(
                        R.id.cardInterviews
                );

        btnCompleteProfile =
                findViewById(
                        R.id.btnCompleteProfile
                );

        imgDashboardProfile =
                findViewById(
                        R.id.imgDashboardProfile
                );

        imgDashboardProfileFallback =
                findViewById(
                        R.id.imgDashboardProfileFallback
                );


        btnBrowseJobs =
                findViewById(
                        R.id.btnBrowseJobs
                );


        btnMyApplications =
                findViewById(
                        R.id.btnMyApplications
                );


        recyclerViewJobs =
                findViewById(
                        R.id.recyclerViewJobs
                );
    }


    // =====================================================
    // RECYCLER VIEW
    // =====================================================

    private void setupRecyclerView() {

        recyclerViewJobs.setLayoutManager(
                new LinearLayoutManager(this)
        );


        bestMatchAdapter =
                new BestMatchAdapter(
                        bestMatchList,
                        match -> {

                            Intent intent =
                                    new Intent(
                                            JobseekerDashboardActivity.this,
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

        recyclerViewJobs.setAdapter(
                bestMatchAdapter
        );
    }


    // =====================================================
    // LOAD DASHBOARD FROM API
    // =====================================================

    private void loadDashboard() {

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

                                    displayDashboard(
                                            response.body()
                                                    .getDashboard()
                                    );

                                } else {

                                    Toast.makeText(
                                            JobseekerDashboardActivity.this,
                                            "Unable to load dashboard.",
                                            Toast.LENGTH_LONG
                                    ).show();
                                }
                            }


                            @Override
                            public void onFailure(
                                    Call<JobseekerDashboardResponse> call,
                                    Throwable t
                            ) {

                                android.util.Log.e(
                                        "JOBSEEKER_DASHBOARD",
                                        "Dashboard error",
                                        t
                                );


                                Toast.makeText(
                                        JobseekerDashboardActivity.this,
                                        "Unable to connect to server.",
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                );
    }


    // =====================================================
    // DISPLAY DATA
    // =====================================================

    private void displayDashboard(
            JobseekerDashboardResponse.Dashboard dashboard
    ) {

        String candidateName =
                dashboard.getCandidateName();


        if (
                candidateName == null
                        ||
                        candidateName.trim().isEmpty()
        ) {

            candidateName =
                    "Job Seeker";
        }

        tvUserHeadline.setText(
                getGreeting()
        );

        tvWelcomeUser.setText(
                candidateName
        );

        tvJobsApplied.setText(
                String.valueOf(
                        dashboard.getJobsApplied()
                )
        );


        tvInterviews.setText(
                String.valueOf(
                        dashboard.getInterviews()
                )
        );


        List<JobseekerDashboardResponse.BestMatch>
                matches =
                dashboard.getBestMatches();


        bestMatchList.clear();


        if (
                matches == null
                        ||
                        matches.isEmpty()
        ) {

            tvNoMatches.setVisibility(
                    View.VISIBLE
            );

            recyclerViewJobs.setVisibility(
                    View.GONE
            );

        } else {

            tvNoMatches.setVisibility(
                    View.GONE
            );

            recyclerViewJobs.setVisibility(
                    View.VISIBLE
            );


            bestMatchList.addAll(
                    matches
            );
        }


        bestMatchAdapter.notifyDataSetChanged();
    }


    // =====================================================
    // NAVIGATION
    // =====================================================

    private void setupNavigation() {

        // =========================================
        // PROFILE ICON
        // =========================================

        btnCompleteProfile.setOnClickListener(
                view -> openAccount()
        );


        // =========================================
        // SEARCH
        // =========================================

        btnBrowseJobs.setOnClickListener(
                view -> openBrowseJobs()
        );


        // =========================================
        // OLD APPLICATION BUTTON
        // Retained for compatibility
        // =========================================

        btnMyApplications.setOnClickListener(
                view -> openApplications()
        );


        // =========================================
        // BEST MATCHES - MORE
        // =========================================

        tvMoreMatches.setOnClickListener(
                view -> openRecommendedJobs()
        );


        // =========================================
        // STAT CARDS
        // =========================================

        cardJobsApplied.setOnClickListener(
                view -> openApplications()
        );

        cardInterviews.setOnClickListener(
                view -> openApplications()
        );


        // =========================================
        // BOTTOM NAVIGATION
        // =========================================

        navHome.setOnClickListener(
                view -> {
                    // Already on Home.
                }
        );


        navJobs.setOnClickListener(
                view -> openBrowseJobs()
        );


        navApplications.setOnClickListener(
                view -> openApplications()
        );


        navAccount.setOnClickListener(
                view -> openAccount()
        );

    }

    private void loadProfilePicture() {

        if (profileApi == null) {
            return;
        }

        profileApi
                .getMyProfile()
                .enqueue(
                        new Callback<ProfileResponse>() {

                            @Override
                            public void onResponse(
                                    Call<ProfileResponse> call,
                                    Response<ProfileResponse> response
                            ) {

                                if (
                                        !response.isSuccessful()
                                                ||
                                                response.body() == null
                                ) {

                                    showDefaultProfilePicture();
                                    return;
                                }

                                ProfileResponse.ProfileData profile =
                                        response.body().getProfile();

                                if (profile == null) {

                                    showDefaultProfilePicture();
                                    return;
                                }

                                String profilePicture =
                                        profile.getProfilePicture();

                                displayDashboardProfilePicture(
                                        profilePicture
                                );
                            }


                            @Override
                            public void onFailure(
                                    Call<ProfileResponse> call,
                                    Throwable t
                            ) {

                                showDefaultProfilePicture();
                            }
                        }
                );
    }

    private void displayDashboardProfilePicture(
            String profilePicture
    ) {

        if (
                profilePicture == null
                        ||
                        profilePicture.trim().isEmpty()
        ) {

            showDefaultProfilePicture();
            return;
        }


        String imageUrl;

        if (
                profilePicture.startsWith("http://")
                        ||
                        profilePicture.startsWith("https://")
        ) {

            imageUrl =
                    profilePicture;

        } else {

            imageUrl =
                    ApiClient.SERVER_URL
                            + profilePicture;
        }


        imgDashboardProfile.setVisibility(
                View.VISIBLE
        );

        imgDashboardProfileFallback.setVisibility(
                View.GONE
        );


        Glide.with(this)
                .load(imageUrl)
                .circleCrop()
                .listener(
                        new RequestListener<Drawable>() {

                            @Override
                            public boolean onLoadFailed(
                                    @Nullable GlideException e,
                                    Object model,
                                    Target<Drawable> target,
                                    boolean isFirstResource
                            ) {

                                showDefaultProfilePicture();

                                return false;
                            }


                            @Override
                            public boolean onResourceReady(
                                    Drawable resource,
                                    Object model,
                                    Target<Drawable> target,
                                    DataSource dataSource,
                                    boolean isFirstResource
                            ) {

                                imgDashboardProfileFallback
                                        .setVisibility(
                                                View.GONE
                                        );

                                imgDashboardProfile
                                        .setVisibility(
                                                View.VISIBLE
                                        );

                                return false;
                            }
                        }
                )
                .into(
                        imgDashboardProfile
                );
    }

    private void showDefaultProfilePicture() {

        imgDashboardProfile.setVisibility(
                View.GONE
        );

        imgDashboardProfileFallback.setVisibility(
                View.VISIBLE
        );
    }

    private void openApplications() {

        Intent intent =
                new Intent(
                        JobseekerDashboardActivity.this,
                        MyApplicationsActivity.class
                );

        startActivity(intent);
    }


    private void openAccount() {

        Intent intent =
                new Intent(
                        JobseekerDashboardActivity.this,
                        JobseekerAccountActivity.class
                );

        startActivity(intent);
    }

    private void openRecommendedJobs() {

        Intent intent =
                new Intent(
                        JobseekerDashboardActivity.this,
                        RecommendedJobsActivity.class
                );

        startActivity(intent);
    }

    private void openBrowseJobs() {

        Intent intent =
                new Intent(
                        JobseekerDashboardActivity.this,
                        BrowseJobsActivity.class
                );

        startActivity(intent);
    }

    // =====================================================
    // LOGIN
    // =====================================================

    private void openLogin() {

        Intent intent =
                new Intent(
                        JobseekerDashboardActivity.this,
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
                jobseekerApi != null
                        &&
                        sessionManager != null
                        &&
                        sessionManager.isLoggedIn()
        ) {

            loadDashboard();
            loadProfilePicture();
        }
    }
}