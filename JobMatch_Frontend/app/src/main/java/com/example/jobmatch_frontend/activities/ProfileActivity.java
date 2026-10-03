package com.example.jobmatch_frontend.activities;

import android.content.Intent;
import com.bumptech.glide.Glide;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.TextView;
import android.net.Uri;
import android.widget.ImageView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import androidx.appcompat.app.AppCompatActivity;

import com.example.jobmatch_frontend.R;
import com.example.jobmatch_frontend.api.ApiClient;
import com.example.jobmatch_frontend.api.ProfileApi;
import com.example.jobmatch_frontend.models.ProfileResponse;
import com.example.jobmatch_frontend.models.ProfileUpdateRequest;
import com.example.jobmatch_frontend.session.SessionManager;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ProfileActivity extends AppCompatActivity {

    private EditText etFullName;
    private EditText etEmail;
    private EditText etPhone;

    private EditText etHeadline;
    private EditText etAddress;
    private EditText etSkills;
    private EditText etEducation;
    private EditText etExperience;
    private EditText etExpectedSalary;
    private EditText etSummary;

    private ImageButton btnBack;
    private ImageButton btnNextStep;

    private SessionManager sessionManager;

    private ProfileApi profileApi;

    private void goToDashboard() {

        Intent intent =
                new Intent(
                        ProfileActivity.this,
                        JobseekerDashboardActivity.class
                );

        // Profile setup is complete.
        // Dashboard becomes the new root activity.
        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);

        finish();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_profile
        );


        // =====================================================
        // SESSION
        // =====================================================

        sessionManager =
                new SessionManager(this);


        // =====================================================
        // CHECK LOGIN
        // =====================================================

        if (!sessionManager.isLoggedIn()) {

            Toast.makeText(
                    this,
                    "Please login first.",
                    Toast.LENGTH_SHORT
            ).show();

            Intent intent =
                    new Intent(
                            ProfileActivity.this,
                            LoginActivity.class
                    );

            intent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            );

            startActivity(intent);

            finish();

            return;
        }


        // =====================================================
        // CHECK ROLE
        // =====================================================

        if (!"jobseeker".equalsIgnoreCase(
                sessionManager.getRole()
        )) {

            Toast.makeText(
                    this,
                    "Jobseeker access only.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();

            return;
        }


        // =====================================================
        // FIND VIEWS
        // =====================================================

        etFullName =
                findViewById(
                        R.id.etFullName
                );

        etEmail =
                findViewById(
                        R.id.etEmail
                );

        etPhone =
                findViewById(
                        R.id.etPhone
                );

        etAddress =
                findViewById(
                        R.id.etAddress
                );

        etSummary =
                findViewById(
                        R.id.etSummary
                );

        btnBack =
                findViewById(
                        R.id.btnBack
                );

        btnNextStep =
                findViewById(
                        R.id.btnNextStep
                );

        etHeadline =
                findViewById(R.id.etHeadline);

        etSkills =
                findViewById(R.id.etSkills);

        etEducation =
                findViewById(R.id.etEducation);

        etExperience =
                findViewById(R.id.etExperience);

        etExpectedSalary =
                findViewById(R.id.etExpectedSalary);

        // =====================================================
        // CREATE AUTHENTICATED PROFILE API
        // =====================================================

        profileApi =
                ApiClient
                        .getClient(sessionManager)
                        .create(ProfileApi.class);


        // =====================================================
        // EMAIL SHOULD NOT BE EDITED HERE
        // =====================================================

        etEmail.setEnabled(false);


        // =====================================================
        // TEMPORARY SESSION PRE-FILL
        // =====================================================

        if (
                sessionManager.getFullName()
                        != null
        ) {

            etFullName.setText(
                    sessionManager.getFullName()
            );
        }


        // =====================================================
        // LOAD PROFILE FROM SERVER
        // =====================================================

        loadProfile();


        // =====================================================
        // BACK BUTTON
        // =====================================================

        btnBack.setOnClickListener(
                v -> finish()
        );


        // =====================================================
        // NEXT BUTTON
        // =====================================================

        btnNextStep.setOnClickListener(
                v -> {

                    if (validateInputs()) {

                        updateProfile();
                    }
                }
        );

    }


    // =====================================================
    // LOAD PROFILE
    // GET /api/profiles/me
    // =====================================================

    private void loadProfile() {

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
                                        response.isSuccessful()
                                                &&
                                                response.body() != null
                                ) {

                                    ProfileResponse profileResponse =
                                            response.body();


                                    if (
                                            profileResponse.isSuccess()
                                    ) {

                                        // ==============================
                                        // USERS TABLE
                                        // ==============================

                                        if (
                                                profileResponse.getUser()
                                                        != null
                                        ) {

                                            ProfileResponse.UserData user =
                                                    profileResponse.getUser();


                                            etFullName.setText(
                                                    safeText(
                                                            user.getFullName()
                                                    )
                                            );


                                            etEmail.setText(
                                                    safeText(
                                                            user.getEmail()
                                                    )
                                            );


                                            etPhone.setText(
                                                    safeText(
                                                            user.getPhoneNumber()
                                                    )
                                            );
                                        }


                                        // ==============================
                                        // PROFILES TABLE
                                        // ==============================

                                        if (
                                                profileResponse.getProfile()
                                                        != null
                                        ) {

                                            ProfileResponse.ProfileData profile =
                                                    profileResponse.getProfile();


                                            etHeadline.setText(
                                                    safeText(profile.getHeadline())
                                            );

                                            etAddress.setText(
                                                    safeText(profile.getLocation())
                                            );

                                            etSkills.setText(
                                                    safeText(profile.getSkills())
                                            );

                                            etEducation.setText(
                                                    safeText(profile.getEducation())
                                            );

                                            etExperience.setText(
                                                    safeText(profile.getExperience())
                                            );

                                            if (profile.getExpectedSalary() != null) {

                                                etExpectedSalary.setText(
                                                        String.valueOf(
                                                                profile.getExpectedSalary()
                                                        )
                                                );

                                            } else {

                                                etExpectedSalary.setText("");
                                            }

                                            etSummary.setText(
                                                    safeText(profile.getResumeSummary())
                                            );
                                        }


                                        Log.d(
                                                "PROFILE",
                                                "Profile loaded successfully"
                                        );

                                    } else {

                                        Toast.makeText(
                                                ProfileActivity.this,
                                                profileResponse.getMessage(),
                                                Toast.LENGTH_SHORT
                                        ).show();
                                    }

                                } else {

                                    Log.e(
                                            "PROFILE",
                                            "Load failed. HTTP: "
                                                    + response.code()
                                    );


                                    Toast.makeText(
                                            ProfileActivity.this,
                                            "Failed to load profile.",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }
                            }


                            @Override
                            public void onFailure(
                                    Call<ProfileResponse> call,
                                    Throwable t
                            ) {

                                btnNextStep.setEnabled(true);

                                Log.e(
                                        "PROFILE_IMAGE",
                                        "Upload error",
                                        t
                                );

                                Toast.makeText(
                                        ProfileActivity.this,
                                        "Image upload error: "
                                                + t.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                );
    }


    // =====================================================
    // UPDATE PROFILE
    // PUT /api/profiles/me
    // =====================================================

    private void updateProfile() {

        String fullName =
                etFullName.getText()
                        .toString()
                        .trim();

        String phone =
                etPhone.getText()
                        .toString()
                        .trim();

        String headline =
                etHeadline.getText()
                        .toString()
                        .trim();

        String address =
                etAddress.getText()
                        .toString()
                        .trim();

        String skills =
                etSkills.getText()
                        .toString()
                        .trim();

        String education =
                etEducation.getText()
                        .toString()
                        .trim();

        String experience =
                etExperience.getText()
                        .toString()
                        .trim();

        String salaryText =
                etExpectedSalary.getText()
                        .toString()
                        .trim();

        String summary =
                etSummary.getText()
                        .toString()
                        .trim();


        Double expectedSalary = null;

        try {

            expectedSalary =
                    Double.parseDouble(salaryText);

        } catch (NumberFormatException e) {

            etExpectedSalary.setError(
                    "Enter a valid salary"
            );

            return;
        }


        // =====================================================
        // CREATE REQUEST
        // =====================================================

        ProfileUpdateRequest request =
                new ProfileUpdateRequest(
                        fullName,
                        phone,
                        headline,
                        skills,
                        education,
                        experience,
                        expectedSalary,
                        address,
                        summary
                );


        // Prevent double-click
        btnNextStep.setEnabled(false);


        profileApi
                .updateMyProfile(request)
                .enqueue(
                        new Callback<ProfileResponse>() {

                            @Override
                            public void onResponse(
                                    Call<ProfileResponse> call,
                                    Response<ProfileResponse> response
                            ) {

                                if (
                                        response.isSuccessful()
                                                &&
                                                response.body() != null
                                ) {

                                    ProfileResponse profileResponse =
                                            response.body();

                                    if (
                                            profileResponse.isSuccess()
                                    ) {

                                        Toast.makeText(
                                                ProfileActivity.this,
                                                "Profile saved successfully!",
                                                Toast.LENGTH_SHORT
                                        ).show();


                                        goToDashboard();

                                    } else {

                                        Toast.makeText(
                                                ProfileActivity.this,
                                                profileResponse.getMessage(),
                                                Toast.LENGTH_SHORT
                                        ).show();
                                    }

                                } else {

                                    Log.e(
                                            "PROFILE",
                                            "Update failed. HTTP: "
                                                    + response.code()
                                    );


                                    Toast.makeText(
                                            ProfileActivity.this,
                                            "Failed to save profile. HTTP "
                                                    + response.code(),
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }
                            }


                            @Override
                            public void onFailure(
                                    Call<ProfileResponse> call,
                                    Throwable t
                            ) {

                               btnNextStep.setEnabled(true);


                                Log.e(
                                        "PROFILE",
                                        "Update error: "
                                                + t.getMessage()
                                );


                                Toast.makeText(
                                        ProfileActivity.this,
                                        "Network error: "
                                                + t.getMessage(),
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }
                );
    }


    // =====================================================
    // VALIDATION
    // =====================================================

    private boolean validateInputs() {

        if (etFullName.getText().toString().trim().isEmpty()) {
            etFullName.setError("Full name is required");
            return false;
        }

        if (etPhone.getText().toString().trim().isEmpty()) {
            etPhone.setError("Phone number is required");
            return false;
        }

        if (etHeadline.getText().toString().trim().isEmpty()) {
            etHeadline.setError("Professional headline is required");
            return false;
        }

        if (etAddress.getText().toString().trim().isEmpty()) {
            etAddress.setError("Location is required");
            return false;
        }

        if (etSkills.getText().toString().trim().isEmpty()) {
            etSkills.setError("Add at least one skill");
            return false;
        }

        if (etEducation.getText().toString().trim().isEmpty()) {
            etEducation.setError("Education is required");
            return false;
        }

        if (etExperience.getText().toString().trim().isEmpty()) {
            etExperience.setError("Experience is required");
            return false;
        }

        if (etExpectedSalary.getText().toString().trim().isEmpty()) {
            etExpectedSalary.setError("Expected salary is required");
            return false;
        }

        if (etSummary.getText().toString().trim().isEmpty()) {
            etSummary.setError("Professional summary is required");
            return false;
        }

        return true;
    }


    // =====================================================
    // NULL-SAFE TEXT
    // =====================================================

    private String safeText(
            String value
    ) {

        return value == null
                ? ""
                : value;
    }
}