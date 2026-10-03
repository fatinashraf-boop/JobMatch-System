package com.example.jobmatch_frontend.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.jobmatch_frontend.R;
import com.example.jobmatch_frontend.api.ApiClient;
import com.example.jobmatch_frontend.models.AuthResponse;
import com.example.jobmatch_frontend.models.LoginRequest;
import com.example.jobmatch_frontend.session.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegisterLink;

    private SessionManager sessionManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_login
        );


        sessionManager =
                new SessionManager(this);


        // ==============================
        // VIEWS
        // ==============================

        etEmail =
                findViewById(
                        R.id.etEmail
                );

        etPassword =
                findViewById(
                        R.id.etPassword
                );

        btnLogin =
                findViewById(
                        R.id.btnLogin
                );

        tvRegisterLink =
                findViewById(
                        R.id.tvRegisterLink
                );


        // ==============================
        // LOGIN
        // ==============================

        btnLogin.setOnClickListener(
                v -> handleLogin()
        );


        // ==============================
        // REGISTER
        // ==============================

        tvRegisterLink.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            LoginActivity.this,
                            RoleSelectionActivity.class
                    );

            startActivity(intent);
        });
    }


    // =====================================================
    // HANDLE LOGIN
    // =====================================================

    private void handleLogin() {

        String email =
                etEmail.getText()
                        .toString()
                        .trim();

        String password =
                etPassword.getText()
                        .toString();

        // ==============================
        // VALIDATION
        // ==============================

        if (email.isEmpty()
                || password.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please fill in all fields",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        LoginRequest loginRequest =
                new LoginRequest(
                        email,
                        password
                );


        btnLogin.setEnabled(false);


        // ==============================
        // LOGIN API
        // ==============================

        ApiClient
                .getService()
                .login(loginRequest)
                .enqueue(
                        new Callback<AuthResponse>() {

                            @Override
                            public void onResponse(
                                    Call<AuthResponse> call,
                                    Response<AuthResponse> response
                            ) {

                                btnLogin.setEnabled(true);


                                if (response.isSuccessful()
                                        && response.body() != null) {

                                    AuthResponse auth =
                                            response.body();


                                    if (auth.isSuccess()
                                            && auth.getUser() != null) {


                                        String token =
                                                auth.getToken();

                                        int userId =
                                                auth.getUser()
                                                        .getUserId();

                                        String role =
                                                auth.getUser()
                                                        .getRole();

                                        String fullName =
                                                auth.getUser()
                                                        .getFullName();

                                        String onboardingStatus =
                                                auth.getUser()
                                                        .getOnboardingStatus();


                                        // ==============================
                                        // SAVE SESSION
                                        // ==============================

                                        sessionManager
                                                .createLoginSession(
                                                        token,
                                                        userId,
                                                        role,
                                                        fullName
                                                );


                                        Toast.makeText(
                                                LoginActivity.this,
                                                "Login Successful!",
                                                Toast.LENGTH_SHORT
                                        ).show();


                                        // ==============================
                                        // ROUTE USER
                                        // ==============================

                                        routeUser(
                                                role,
                                                onboardingStatus
                                        );


                                    } else {

                                        Toast.makeText(
                                                LoginActivity.this,
                                                auth.getMessage(),
                                                Toast.LENGTH_SHORT
                                        ).show();
                                    }


                                } else {

                                    Toast.makeText(
                                            LoginActivity.this,
                                            "Invalid credentials or account does not exist",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }
                            }


                            @Override
                            public void onFailure(
                                    Call<AuthResponse> call,
                                    Throwable t
                            ) {

                                btnLogin.setEnabled(true);

                                Toast.makeText(
                                        LoginActivity.this,
                                        "Network error: "
                                                + t.getMessage(),
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }
                );
    }


    // =====================================================
    // ROUTE USER BASED ON ROLE + ONBOARDING STATUS
    // =====================================================

    private void routeUser(
            String role,
            String onboardingStatus
    ) {

        Intent intent;


        // ==============================
        // JOB SEEKER
        // ==============================

        if ("jobseeker".equalsIgnoreCase(role)) {

            if ("complete".equalsIgnoreCase(
                    onboardingStatus
            )) {

                // Profile already completed
                intent =
                        new Intent(
                                LoginActivity.this,
                                JobseekerDashboardActivity.class
                        );

            } else {

                // Profile still incomplete
                intent =
                        new Intent(
                                LoginActivity.this,
                                ProfileActivity.class
                        );
            }


            // ==============================
            // RECRUITER
            // ==============================

        } else if ("recruiter".equalsIgnoreCase(role)) {

            if ("complete".equalsIgnoreCase(
                    onboardingStatus
            )) {

                // Company profile completed
                intent =
                        new Intent(
                                LoginActivity.this,
                                EmployerDashboardActivity.class
                        );

            } else {

                // Company profile still incomplete
                intent =
                        new Intent(
                                LoginActivity.this,
                                CreateCompanyAccountActivity.class
                        );
            }


            // ==============================
            // INVALID ROLE
            // ==============================

        } else {

            Toast.makeText(
                    LoginActivity.this,
                    "Invalid account role.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }


        // ==============================
        // CLEAR LOGIN FROM BACK STACK
        // ==============================

        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );


        startActivity(intent);

        finish();
    }
}