package com.example.jobmatch_frontend.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.jobmatch_frontend.R;
import com.example.jobmatch_frontend.api.ApiClient;
import com.example.jobmatch_frontend.api.AuthApi;
import com.example.jobmatch_frontend.models.AuthResponse;
import com.example.jobmatch_frontend.models.RegisterRequest;
import com.example.jobmatch_frontend.activities.CreateCompanyAccountActivity;
import com.example.jobmatch_frontend.session.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText etFullName;
    private EditText etRegisterEmail;
    private EditText etPhoneNumber;
    private EditText etRole;
    private EditText etRegisterPassword;

    private Button btnRegisterSubmit;
    private TextView tvLoginLink;

    private SessionManager sessionManager;

    // Selected role received from RoleSelectionActivity
    private String selectedRole;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);


        // ==============================
        // INITIALIZE SESSION
        // ==============================

        sessionManager =
                new SessionManager(this);


        // ==============================
        // FIND VIEWS
        // ==============================

        etFullName =
                findViewById(R.id.etFullName);

        etRegisterEmail =
                findViewById(R.id.etRegisterEmail);

        etPhoneNumber =
                findViewById(R.id.etPhoneNumber);

        etRole =
                findViewById(R.id.etRole);

        etRegisterPassword =
                findViewById(R.id.etRegisterPassword);

        btnRegisterSubmit =
                findViewById(R.id.btnRegisterSubmit);

        tvLoginLink =
                findViewById(R.id.tvLoginLink);


        // ==============================
        // RECEIVE SELECTED ROLE
        // ==============================

        selectedRole =
                getIntent().getStringExtra("selected_role");


        // ==============================
        // CHECK ROLE
        // ==============================

        if (selectedRole == null
                || selectedRole.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "Please select your role first.",
                    Toast.LENGTH_LONG
            ).show();

            finish();

            return;
        }


        // Convert to lowercase
        selectedRole =
                selectedRole.trim().toLowerCase();


        // ==============================
        // VALIDATE ROLE
        // ==============================

        if (!selectedRole.equals("jobseeker")
                && !selectedRole.equals("recruiter")) {

            Toast.makeText(
                    this,
                    "Invalid role selected.",
                    Toast.LENGTH_LONG
            ).show();

            finish();

            return;
        }


        // ==============================
        // STORE ROLE IN HIDDEN FIELD
        // ==============================
        //
        // We keep etRole for now because
        // your existing RegisterRequest
        // structure uses it.
        //
        // The user will NOT type this.
        //

        etRole.setText(selectedRole);

        etRole.setVisibility(View.GONE);


        // ==============================
        // DEBUG LOG
        // ==============================

        android.util.Log.d(
                "ROLE_SELECTION",
                "Selected role = " + selectedRole
        );


        // ==============================
        // REGISTER BUTTON
        // ==============================

        btnRegisterSubmit.setOnClickListener(
                v -> handleRegistration()
        );


        // ==============================
        // LOGIN LINK
        // ==============================

        tvLoginLink.setOnClickListener(v -> {

            startActivity(
                    new Intent(
                            RegisterActivity.this,
                            LoginActivity.class
                    )
            );

            finish();
        });
    }


    // =====================================================
    // HANDLE REGISTRATION
    // =====================================================

    private void handleRegistration() {

        String name =
                etFullName
                        .getText()
                        .toString()
                        .trim();

        String email =
                etRegisterEmail
                        .getText()
                        .toString()
                        .trim()
                        .toLowerCase(java.util.Locale.ROOT);

        String phone =
                etPhoneNumber
                        .getText()
                        .toString()
                        .trim();

        String password =
                etRegisterPassword
                        .getText()
                        .toString();

        // ==============================
        // VALIDATE INPUT
        // ==============================

        if (name.isEmpty()
                || email.isEmpty()
                || phone.isEmpty()
                || password.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please fill in all required fields.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // ==============================
        // VALIDATE EMAIL
        // ==============================

        if (email.length() > 100 ||
                !android.util.Patterns.EMAIL_ADDRESS
                        .matcher(email)
                        .matches()) {

            etRegisterEmail.setError(
                    "Please enter a valid email address."
            );

            etRegisterEmail.requestFocus();
            return;
        }


        // ==============================
        // VALIDATE PASSWORD LENGTH
        // ==============================

        if (password.length() < 8 ||
                password.length() > 72) {

            etRegisterPassword.setError(
                    "Password must be between 8 and 72 characters."
            );

            etRegisterPassword.requestFocus();
            return;
        }


        // ==============================
        // VALIDATE PASSWORD STRENGTH
        // ==============================

        boolean hasUppercase =
                password.matches(".*[A-Z].*");

        boolean hasLowercase =
                password.matches(".*[a-z].*");

        boolean hasNumber =
                password.matches(".*\\d.*");

        boolean hasSpecial =
                password.matches(
                        ".*[^A-Za-z0-9].*"
                );


        if (!hasUppercase ||
                !hasLowercase ||
                !hasNumber ||
                !hasSpecial) {

            etRegisterPassword.setError(
                    "Include uppercase, lowercase, number and special character."
            );

            etRegisterPassword.requestFocus();
            return;
        }

        // ==============================
        // MAKE SURE ROLE EXISTS
        // ==============================

        if (selectedRole == null
                || selectedRole.isEmpty()) {

            Toast.makeText(
                    this,
                    "Role selection is missing.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // ==============================
        // DEBUG
        // ==============================

        android.util.Log.d(
                "REGISTER",
                "Name = " + name
        );

        android.util.Log.d(
                "REGISTER",
                "Email = " + email
        );

        android.util.Log.d(
                "REGISTER",
                "Role = " + selectedRole
        );


        // ==============================
        // CREATE REGISTER REQUEST
        // ==============================

        RegisterRequest registerRequest =
                new RegisterRequest(
                        name,
                        email,
                        phone,
                        selectedRole,
                        password
                );


        // ==============================
// CALL BACKEND
// ==============================

        AuthApi authApi =
                ApiClient.getService();


        authApi.register(registerRequest)
                .enqueue(
                        new Callback<AuthResponse>() {

                            @Override
                            public void onResponse(
                                    Call<AuthResponse> call,
                                    Response<AuthResponse> response
                            ) {

                                if (response.isSuccessful()
                                        && response.body() != null) {

                                    AuthResponse auth =
                                            response.body();


                                    if (auth.isSuccess()
                                            && auth.getUser() != null) {

                                        int userId =
                                                auth.getUser().getUserId();

                                        String token =
                                                auth.getToken();

                                        String role =
                                                auth.getUser().getRole();

                                        String fullName =
                                                auth.getUser().getFullName();


                                        // ==============================
                                        // SAVE LOGIN SESSION
                                        // ==============================

                                        sessionManager.createLoginSession(
                                                token,
                                                userId,
                                                role,
                                                fullName
                                        );


                                        Toast.makeText(
                                                RegisterActivity.this,
                                                "Account Created!",
                                                Toast.LENGTH_SHORT
                                        ).show();


                                        // ==============================
                                        // JOB SEEKER
                                        // ==============================

                                        if ("jobseeker".equalsIgnoreCase(role)) {

                                            Intent intent =
                                                    new Intent(
                                                            RegisterActivity.this,
                                                            ProfileActivity.class
                                                    );

                                            intent.putExtra(
                                                    "from_registration",
                                                    true
                                            );

                                            startActivity(intent);
                                        }


                                        // ==============================
                                        // RECRUITER
                                        // ==============================

                                        else if ("recruiter".equalsIgnoreCase(role)) {

                                            Intent intent =
                                                    new Intent(
                                                            RegisterActivity.this,
                                                            CreateCompanyAccountActivity.class
                                                    );

                                            intent.putExtra(
                                                    "from_registration",
                                                    true
                                            );

                                            startActivity(intent);
                                        }


                                        finish();

                                    } else {

                                        String message =
                                                auth.getMessage() != null
                                                        ? auth.getMessage()
                                                        : "Registration failed.";

                                        Toast.makeText(
                                                RegisterActivity.this,
                                                message,
                                                Toast.LENGTH_LONG
                                        ).show();
                                    }

                                } else {

                                    String errorMessage =
                                            "Registration failed.";

                                    try {

                                        if (response.errorBody() != null) {

                                            errorMessage =
                                                    response
                                                            .errorBody()
                                                            .string();
                                        }

                                    } catch (Exception e) {

                                        errorMessage =
                                                e.getMessage();
                                    }


                                    android.util.Log.e(
                                            "REGISTER",
                                            "HTTP "
                                                    + response.code()
                                                    + ": "
                                                    + errorMessage
                                    );


                                    Toast.makeText(
                                            RegisterActivity.this,
                                            errorMessage,
                                            Toast.LENGTH_LONG
                                    ).show();
                                }
                            }


                            @Override
                            public void onFailure(
                                    Call<AuthResponse> call,
                                    Throwable t
                            ) {

                                android.util.Log.e(
                                        "REGISTER",
                                        "Registration network error",
                                        t
                                );

                                Toast.makeText(
                                        RegisterActivity.this,
                                        "Network error: "
                                                + t.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                );
    }
}