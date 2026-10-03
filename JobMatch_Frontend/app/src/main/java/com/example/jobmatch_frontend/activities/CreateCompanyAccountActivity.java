package com.example.jobmatch_frontend.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.util.Log;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.example.jobmatch_frontend.api.ApiClient;
import com.example.jobmatch_frontend.api.RecruiterApi;
import com.example.jobmatch_frontend.models.RecruiterResponse;
import com.example.jobmatch_frontend.models.RecruiterUpdateRequest;

import androidx.appcompat.app.AppCompatActivity;

import com.example.jobmatch_frontend.R;
import com.example.jobmatch_frontend.session.SessionManager;

public class CreateCompanyAccountActivity extends AppCompatActivity {

    private EditText etCompanyName;
    private EditText etWebsite;
    private EditText etCompanyEmail;
    private EditText etCompanyAddress;
    private EditText etCompanyDescription;

    private ImageButton btnBack;
    private ImageButton btnNext;

    private SessionManager sessionManager;

    private boolean editMode = false;

    private RecruiterApi recruiterApi;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_create_company_account
        );


        // ==============================
        // SESSION
        // ==============================

        sessionManager =
                new SessionManager(this);


        // ==============================
        // VERIFY USER
        // ==============================

        if (!sessionManager.isLoggedIn()) {

            Toast.makeText(
                    this,
                    "User session not found. Please login again.",
                    Toast.LENGTH_LONG
            ).show();

            finish();

            return;
        }


        if (!"recruiter".equalsIgnoreCase(
                sessionManager.getRole()
        )) {

            Toast.makeText(
                    this,
                    "Recruiter account required.",
                    Toast.LENGTH_LONG
            ).show();

            finish();

            return;
        }

        editMode =
                getIntent().getBooleanExtra(
                        "edit_mode",
                        false
                );

        // ==============================
        // VIEWS
        // ==============================

        etCompanyName =
                findViewById(
                        R.id.etCompanyName
                );

        etWebsite =
                findViewById(
                        R.id.etWebsite
                );

        etCompanyEmail =
                findViewById(
                        R.id.etCompanyEmail
                );

        etCompanyAddress =
                findViewById(
                        R.id.etCompanyAddress
                );

        etCompanyDescription =
                findViewById(
                        R.id.etCompanyDescription
                );

        btnBack =
                findViewById(
                        R.id.btnBack
                );

        btnNext =
                findViewById(
                        R.id.btnNext
                );

        // ==============================
        // API
        // ==============================

        recruiterApi =
                ApiClient
                        .getClient(sessionManager)
                        .create(RecruiterApi.class);

        // ==============================
        // EDIT MODE
        // LOAD EXISTING COMPANY DATA
        // ==============================

        if (editMode) {

            loadExistingCompanyProfile();
        }


        // ==============================
        // BACK
        // ==============================

        btnBack.setOnClickListener(
                v -> finish()
        );


        // ==============================
        // NEXT
        // ==============================

        btnNext.setOnClickListener(v -> {

            if (validateInputs()) {
                updateCompanyAccount();
            }
        });
    }


    // =====================================================
    // VALIDATION
    // =====================================================

    private boolean validateInputs() {

        String companyName =
                etCompanyName
                        .getText()
                        .toString()
                        .trim();

        String companyEmail =
                etCompanyEmail
                        .getText()
                        .toString()
                        .trim();

        String address =
                etCompanyAddress
                        .getText()
                        .toString()
                        .trim();

        String description =
                etCompanyDescription
                        .getText()
                        .toString()
                        .trim();


        if (companyName.isEmpty()) {

            etCompanyName.setError(
                    "Company name is required"
            );

            etCompanyName.requestFocus();

            return false;
        }


        if (companyEmail.isEmpty()) {

            etCompanyEmail.setError(
                    "Company email is required"
            );

            etCompanyEmail.requestFocus();

            return false;
        }


        if (!android.util.Patterns
                .EMAIL_ADDRESS
                .matcher(companyEmail)
                .matches()) {

            etCompanyEmail.setError(
                    "Enter a valid email address"
            );

            etCompanyEmail.requestFocus();

            return false;
        }


        if (address.isEmpty()) {

            etCompanyAddress.setError(
                    "Company address is required"
            );

            etCompanyAddress.requestFocus();

            return false;
        }


        if (description.isEmpty()) {

            etCompanyDescription.setError(
                    "Company description is required"
            );

            etCompanyDescription.requestFocus();

            return false;
        }


        return true;
    }

    private void updateCompanyAccount() {

        String companyName =
                etCompanyName.getText()
                        .toString()
                        .trim();

        String website =
                etWebsite.getText()
                        .toString()
                        .trim();

        String companyEmail =
                etCompanyEmail.getText()
                        .toString()
                        .trim();

        String address =
                etCompanyAddress.getText()
                        .toString()
                        .trim();

        String companyDescription =
                etCompanyDescription.getText()
                        .toString()
                        .trim();


        RecruiterUpdateRequest request =
                new RecruiterUpdateRequest(
                        companyName,
                        companyEmail,
                        companyDescription,
                        website.isEmpty() ? null : website,
                        null,
                        address
                );

        btnNext.setEnabled(false);


        recruiterApi
                .updateMyProfile(request)
                .enqueue(
                        new Callback<RecruiterResponse>() {

                            @Override
                            public void onResponse(
                                    Call<RecruiterResponse> call,
                                    Response<RecruiterResponse> response
                            ) {

                                btnNext.setEnabled(true);

                                if (response.isSuccessful()
                                        && response.body() != null) {

                                    RecruiterResponse result =
                                            response.body();

                                    if (result.isSuccess()
                                            && result.getRecruiter() != null) {

                                        if (editMode) {

                                            Toast.makeText(
                                                    CreateCompanyAccountActivity.this,
                                                    "Company profile updated successfully!",
                                                    Toast.LENGTH_SHORT
                                            ).show();


                                            // Return to existing RecruiterProfileActivity.
                                            // onResume() will reload the latest DB data.

                                            setResult(
                                                    RESULT_OK
                                            );

                                            finish();

                                        } else {

                                            Toast.makeText(
                                                    CreateCompanyAccountActivity.this,
                                                    "Company account created successfully!",
                                                    Toast.LENGTH_SHORT
                                            ).show();


                                            Intent intent =
                                                    new Intent(
                                                            CreateCompanyAccountActivity.this,
                                                            RecruiterProfileActivity.class
                                                    );


                                            startActivity(
                                                    intent
                                            );

                                            finish();
                                        }

                                    } else {

                                        Toast.makeText(
                                                CreateCompanyAccountActivity.this,
                                                result.getMessage(),
                                                Toast.LENGTH_LONG
                                        ).show();
                                    }

                                } else {

                                    String errorMessage =
                                            "Unable to save company account.";

                                    try {

                                        if (response.errorBody() != null) {

                                            errorMessage =
                                                    response
                                                            .errorBody()
                                                            .string();
                                        }

                                    } catch (Exception e) {

                                        Log.e(
                                                "RECRUITER_UPDATE",
                                                "Error reading response",
                                                e
                                        );
                                    }

                                    Toast.makeText(
                                            CreateCompanyAccountActivity.this,
                                            errorMessage,
                                            Toast.LENGTH_LONG
                                    ).show();
                                }
                            }


                            @Override
                            public void onFailure(
                                    Call<RecruiterResponse> call,
                                    Throwable t
                            ) {

                                btnNext.setEnabled(true);

                                Toast.makeText(
                                        CreateCompanyAccountActivity.this,
                                        "Network error: "
                                                + t.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                );
    }

    private void loadExistingCompanyProfile() {

        btnNext.setEnabled(false);


        recruiterApi
                .getMyProfile()
                .enqueue(
                        new Callback<RecruiterResponse>() {

                            @Override
                            public void onResponse(
                                    Call<RecruiterResponse> call,
                                    Response<RecruiterResponse> response
                            ) {

                                btnNext.setEnabled(true);


                                if (
                                        response.isSuccessful()
                                                &&
                                                response.body() != null
                                                &&
                                                response.body().isSuccess()
                                                &&
                                                response.body().getRecruiter() != null
                                ) {

                                    RecruiterResponse.Recruiter recruiter =
                                            response
                                                    .body()
                                                    .getRecruiter();


                                    etCompanyName.setText(
                                            valueOrEmpty(
                                                    recruiter.getCompanyName()
                                            )
                                    );


                                    etCompanyEmail.setText(
                                            valueOrEmpty(
                                                    recruiter.getCompanyEmail()
                                            )
                                    );


                                    etWebsite.setText(
                                            valueOrEmpty(
                                                    recruiter.getWebsite()
                                            )
                                    );


                                    etCompanyAddress.setText(
                                            valueOrEmpty(
                                                    recruiter.getAddress()
                                            )
                                    );


                                    etCompanyDescription.setText(
                                            valueOrEmpty(
                                                    recruiter.getCompanyDescription()
                                            )
                                    );


                                    return;
                                }


                                Toast.makeText(
                                        CreateCompanyAccountActivity.this,
                                        "Unable to load company profile.",
                                        Toast.LENGTH_LONG
                                ).show();
                            }


                            @Override
                            public void onFailure(
                                    Call<RecruiterResponse> call,
                                    Throwable throwable
                            ) {

                                btnNext.setEnabled(true);


                                Toast.makeText(
                                        CreateCompanyAccountActivity.this,
                                        "Network error: "
                                                + throwable.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                );
    }

    private String valueOrEmpty(
            String value
    ) {

        return value == null
                ? ""
                : value;
    }
}