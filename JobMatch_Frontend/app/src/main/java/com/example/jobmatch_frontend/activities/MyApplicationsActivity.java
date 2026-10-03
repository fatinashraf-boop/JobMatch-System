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
import com.example.jobmatch_frontend.adapters.ApplicationAdapter;
import com.example.jobmatch_frontend.api.ApiClient;
import com.example.jobmatch_frontend.api.ApplicationApi;
import com.example.jobmatch_frontend.models.Application;
import com.example.jobmatch_frontend.models.ApplicationListResponse;
import com.example.jobmatch_frontend.models.WithdrawApplicationResponse;
import com.example.jobmatch_frontend.session.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyApplicationsActivity
        extends AppCompatActivity {

    private RecyclerView recyclerApplications;

    private TextView tvNoApplications;

    private ApplicationAdapter adapter;

    private final List<Application> applicationList =
            new ArrayList<>();

    private SessionManager sessionManager;

    private ApplicationApi applicationApi;


    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_my_applications
        );


        sessionManager =
                new SessionManager(this);


        recyclerApplications =
                findViewById(
                        R.id.recyclerApplications
                );


        tvNoApplications =
                findViewById(
                        R.id.tvNoApplications
                );


        recyclerApplications.setLayoutManager(
                new LinearLayoutManager(this)
        );


        adapter =
                new ApplicationAdapter(
                        applicationList,
                        this::confirmWithdrawApplication,
                        this::openApplicationDetails
                );

        recyclerApplications.setAdapter(
                adapter
        );


        applicationApi =
                ApiClient
                        .getClient(
                                sessionManager
                        )
                        .create(
                                ApplicationApi.class
                        );
    }


    @Override
    protected void onResume() {

        super.onResume();

        loadApplications();
    }


    // ==========================================
    // LOAD APPLICATIONS
    // ==========================================

    private void loadApplications() {

        applicationApi
                .getMyApplications()
                .enqueue(
                        new Callback<ApplicationListResponse>() {

                            @Override
                            public void onResponse(
                                    Call<ApplicationListResponse> call,
                                    Response<ApplicationListResponse> response
                            ) {

                                if (
                                        response.isSuccessful()
                                                &&
                                                response.body() != null
                                                &&
                                                response.body().isSuccess()
                                ) {

                                    applicationList.clear();


                                    if (
                                            response.body()
                                                    .getApplications()
                                                    != null
                                    ) {

                                        applicationList.addAll(
                                                response.body()
                                                        .getApplications()
                                        );
                                    }


                                    adapter.notifyDataSetChanged();


                                    boolean empty =
                                            applicationList.isEmpty();


                                    tvNoApplications.setVisibility(
                                            empty
                                                    ? View.VISIBLE
                                                    : View.GONE
                                    );


                                    recyclerApplications.setVisibility(
                                            empty
                                                    ? View.GONE
                                                    : View.VISIBLE
                                    );


                                } else {

                                    Toast.makeText(
                                            MyApplicationsActivity.this,
                                            "Unable to load applications.",
                                            Toast.LENGTH_LONG
                                    ).show();
                                }
                            }


                            @Override
                            public void onFailure(
                                    Call<ApplicationListResponse> call,
                                    Throwable t
                            ) {

                                Toast.makeText(
                                        MyApplicationsActivity.this,
                                        "Connection error: "
                                                +
                                                t.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                );
    }

    private void confirmWithdrawApplication(
            Application application
    ) {

        new androidx.appcompat.app.AlertDialog.Builder(this)

                .setTitle(
                        "Withdraw Application"
                )

                .setMessage(
                        "Are you sure you want to withdraw your application for "
                                +
                                application.getJobTitle()
                                +
                                "?"
                )

                .setNegativeButton(
                        "Cancel",
                        null
                )

                .setPositiveButton(
                        "Withdraw",
                        (dialog, which) ->
                                withdrawApplication(
                                        application
                                )
                )

                .show();
    }

    private void withdrawApplication(
            Application application
    ) {

        applicationApi
                .withdrawApplication(
                        application.getApplicationId()
                )
                .enqueue(
                        new Callback<WithdrawApplicationResponse>() {

                            @Override
                            public void onResponse(
                                    Call<WithdrawApplicationResponse> call,
                                    Response<WithdrawApplicationResponse> response
                            ) {

                                if (
                                        response.isSuccessful()
                                                &&
                                                response.body() != null
                                                &&
                                                response.body().isSuccess()
                                ) {

                                    Toast.makeText(
                                            MyApplicationsActivity.this,
                                            "Application withdrawn successfully",
                                            Toast.LENGTH_SHORT
                                    ).show();


                                    // Refresh immediately
                                    loadApplications();

                                    return;
                                }


                                String message =
                                        "Unable to withdraw application";


                                if (
                                        response.body() != null
                                                &&
                                                response.body().getMessage()
                                                        != null
                                ) {

                                    message =
                                            response.body()
                                                    .getMessage();
                                }


                                Toast.makeText(
                                        MyApplicationsActivity.this,
                                        message,
                                        Toast.LENGTH_LONG
                                ).show();
                            }


                            @Override
                            public void onFailure(
                                    Call<WithdrawApplicationResponse> call,
                                    Throwable t
                            ) {

                                Toast.makeText(
                                        MyApplicationsActivity.this,
                                        "Connection error: "
                                                +
                                                t.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                );
    }

    private void openApplicationDetails(
            Application application
    ) {

        Intent intent =
                new Intent(
                        MyApplicationsActivity.this,
                        ApplicationDetailsActivity.class
                );


        intent.putExtra(
                "application_id",
                application.getApplicationId()
        );


        startActivity(intent);
    }
}