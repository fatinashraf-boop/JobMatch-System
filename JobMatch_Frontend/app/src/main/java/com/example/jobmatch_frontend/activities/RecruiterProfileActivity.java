package com.example.jobmatch_frontend.activities;

import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.bumptech.glide.Glide;
import java.io.InputStream;
import java.io.File;
import java.io.FileOutputStream;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.jobmatch_frontend.R;
import com.example.jobmatch_frontend.api.ApiClient;
import com.example.jobmatch_frontend.api.RecruiterApi;
import com.example.jobmatch_frontend.models.RecruiterResponse;
import com.example.jobmatch_frontend.session.SessionManager;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class RecruiterProfileActivity
        extends AppCompatActivity {


    // =====================================================
    // COMPANY
    // =====================================================

    private ImageView imgCompanyLogo;
    private TextView tvChangeCompanyLogo;
    private final ActivityResultLauncher<String> logoPicker = registerForActivityResult(
        new ActivityResultContracts.GetContent(), uri -> { if (uri != null) uploadCompanyLogo(uri); });
    private void uploadCompanyLogo(Uri uri) {
        try {
            String mime = getContentResolver().getType(uri);
            String extension = "image/png".equals(mime) ? ".png" : "image/webp".equals(mime) ? ".webp" : ".jpg";
            if (!"image/png".equals(mime) && !"image/webp".equals(mime) && !"image/jpeg".equals(mime)) {
                showMessage("Select a JPG, PNG or WebP image."); return;
            }
            File file = new File(getCacheDir(), "company-logo-" + System.currentTimeMillis() + extension);
            try (InputStream in = getContentResolver().openInputStream(uri);
                 FileOutputStream out = new FileOutputStream(file)) {
                if (in == null) { showMessage("Unable to read image."); return; }
                byte[] buffer = new byte[8192]; int count; long total = 0;
                while ((count = in.read(buffer)) != -1) {
                    total += count;
                    if (total > 5 * 1024 * 1024) { file.delete(); showMessage("Maximum image size is 5 MB."); return; }
                    out.write(buffer, 0, count);
                }
            }
            tvChangeCompanyLogo.setEnabled(false);
            RequestBody body = RequestBody.create(file, MediaType.parse(mime));
            MultipartBody.Part part = MultipartBody.Part.createFormData("company_logo", file.getName(), body);
            recruiterApi.uploadMyLogo(part).enqueue(new Callback<RecruiterResponse>() {
                @Override public void onResponse(Call<RecruiterResponse> call, Response<RecruiterResponse> response) {
                    tvChangeCompanyLogo.setEnabled(true); file.delete();
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        showMessage("Company logo updated."); loadRecruiterProfile();
                    } else showMessage("Unable to upload company logo (HTTP " + response.code() + ").");
                }
                @Override public void onFailure(Call<RecruiterResponse> call, Throwable t) {
                    tvChangeCompanyLogo.setEnabled(true); file.delete(); showMessage("Upload failed: " + t.getMessage());
                }
            });
        } catch (Exception e) { showMessage("Unable to prepare image: " + e.getMessage()); }
    }
    private TextView tvCompanyInitial;
    private TextView tvCompanyName;
    private TextView tvCompanyEmail;
    private TextView tvWebsite;
    private TextView tvAddress;
    private TextView tvCompanyDescription;
    private TextView tvVerificationStatus;
    private TextView tvProfileStatus;


    // =====================================================
    // ACCOUNT
    // =====================================================

    private TextView tvRecruiterName;
    private TextView tvAccountEmail;
    private TextView tvPhoneNumber;


    // =====================================================
    // BUTTONS
    // =====================================================

    private ImageButton btnBack;

    private Button btnEditProfile;
    private Button btnDashboard;
    private Button btnLogout;


    // =====================================================
    // SESSION / API
    // =====================================================

    private SessionManager sessionManager;

    private RecruiterApi recruiterApi;


    // =====================================================
    // ON CREATE
    // =====================================================

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {

        super.onCreate(
                savedInstanceState
        );

        setContentView(
                R.layout.activity_recruiter_profile
        );


        sessionManager =
                new SessionManager(this);


        // =================================================
        // SESSION CHECK
        // =================================================

        if (!sessionManager.isLoggedIn()) {

            showMessage(
                    "Please login again."
            );

            finish();

            return;
        }


        // =================================================
        // ROLE CHECK
        // =================================================

        if (
                !"recruiter".equalsIgnoreCase(
                        sessionManager.getRole()
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

        setupButtons();
    }


    // =====================================================
    // ON RESUME
    // Reload after editing company information
    // =====================================================

    @Override
    protected void onResume() {

        super.onResume();

        loadRecruiterProfile();
    }


    // =====================================================
    // INITIALIZE VIEWS
    // =====================================================

    private void initializeViews() {

        imgCompanyLogo = findViewById(R.id.imgCompanyLogo);
        tvChangeCompanyLogo = findViewById(R.id.tvChangeCompanyLogo);
        tvCompanyInitial =
                findViewById(
                        R.id.tvCompanyInitial
                );

        tvCompanyName =
                findViewById(
                        R.id.tvCompanyName
                );

        tvCompanyEmail =
                findViewById(
                        R.id.tvCompanyEmail
                );

        tvWebsite =
                findViewById(
                        R.id.tvWebsite
                );

        tvAddress =
                findViewById(
                        R.id.tvAddress
                );

        tvCompanyDescription =
                findViewById(
                        R.id.tvCompanyDescription
                );

        tvVerificationStatus =
                findViewById(
                        R.id.tvVerificationStatus
                );

        tvProfileStatus =
                findViewById(
                        R.id.tvProfileStatus
                );


        tvRecruiterName =
                findViewById(
                        R.id.tvRecruiterName
                );

        tvAccountEmail =
                findViewById(
                        R.id.tvAccountEmail
                );

        tvPhoneNumber =
                findViewById(
                        R.id.tvPhoneNumber
                );


        btnBack =
                findViewById(
                        R.id.btnBack
                );

        btnEditProfile =
                findViewById(
                        R.id.btnEditProfile
                );

        btnDashboard =
                findViewById(
                        R.id.btnDashboard
                );

        btnLogout =
                findViewById(
                        R.id.btnLogout
                );
    }


    // =====================================================
    // API
    // =====================================================

    private void setupApi() {

        recruiterApi =
                ApiClient
                        .getClient(
                                sessionManager
                        )
                        .create(
                                RecruiterApi.class
                        );
    }


    // =====================================================
    // BUTTONS
    // =====================================================

    private void setupButtons() {
        tvChangeCompanyLogo.setOnClickListener(v -> logoPicker.launch("image/*"));

        btnBack.setOnClickListener(
                view -> finish()
        );


        // =================================================
        // EDIT COMPANY PROFILE
        // =================================================

        btnEditProfile.setOnClickListener(
                view -> {

                    Intent intent =
                            new Intent(
                                    RecruiterProfileActivity.this,
                                    CreateCompanyAccountActivity.class
                            );


                    intent.putExtra(
                            "edit_mode",
                            true
                    );


                    startActivity(
                            intent
                    );
                }
        );


        // =================================================
        // DASHBOARD
        // =================================================

        btnDashboard.setOnClickListener(
                view -> {

                    Intent intent =
                            new Intent(
                                    RecruiterProfileActivity.this,
                                    EmployerDashboardActivity.class
                            );


                    intent.addFlags(
                            Intent.FLAG_ACTIVITY_CLEAR_TOP
                    );


                    startActivity(
                            intent
                    );
                }
        );


        // =================================================
        // LOGOUT
        // =================================================

        btnLogout.setOnClickListener(
                view -> showLogoutConfirmation()
        );
    }


    // =====================================================
    // LOAD PROFILE
    // GET /api/recruiters/me
    // =====================================================

    private void loadRecruiterProfile() {

        recruiterApi
                .getMyProfile()
                .enqueue(
                        new Callback<RecruiterResponse>() {

                            @Override
                            public void onResponse(
                                    Call<RecruiterResponse> call,
                                    Response<RecruiterResponse> response
                            ) {

                                if (
                                        response.isSuccessful()
                                                &&
                                                response.body() != null
                                ) {

                                    RecruiterResponse result =
                                            response.body();


                                    if (
                                            result.isSuccess()
                                                    &&
                                                    result.getRecruiter() != null
                                    ) {

                                        displayRecruiter(
                                                result.getRecruiter(),
                                                result.getUser()
                                        );

                                        return;
                                    }


                                    showMessage(
                                            result.getMessage() != null
                                                    ? result.getMessage()
                                                    : "Unable to load recruiter profile."
                                    );

                                    return;
                                }


                                if (
                                        response.code() == 401
                                ) {

                                    showMessage(
                                            "Session expired. Please login again."
                                    );

                                } else {

                                    showMessage(
                                            "Unable to load recruiter profile."
                                    );
                                }
                            }


                            @Override
                            public void onFailure(
                                    Call<RecruiterResponse> call,
                                    Throwable throwable
                            ) {

                                showMessage(
                                        "Network error: "
                                                +
                                                throwable.getMessage()
                                );
                            }
                        }
                );
    }


    // =====================================================
    // DISPLAY COMPANY
    // =====================================================

    private void displayRecruiter(
            RecruiterResponse.Recruiter recruiter,
            RecruiterResponse.User user
    ) {

        String companyName =
                valueOrDefault(
                        recruiter.getCompanyName(),
                        "Company Name"
                );


        // =====================================================
        // COMPANY HEADER
        // =====================================================

        tvCompanyName.setText(
                companyName
        );


        String logo = recruiter.getCompanyLogo();
        if (logo != null && !logo.trim().isEmpty()) {
            imgCompanyLogo.setVisibility(View.VISIBLE);
            tvCompanyInitial.setVisibility(View.GONE);
            tvChangeCompanyLogo.setText("Change Company Logo");
            String url = logo.startsWith("http") ? logo : ApiClient.SERVER_URL + (logo.startsWith("/") ? logo : "/" + logo);
            Glide.with(this).load(url).centerCrop().into(imgCompanyLogo);
        } else {
            imgCompanyLogo.setVisibility(View.GONE);
            tvCompanyInitial.setVisibility(View.VISIBLE);
            tvChangeCompanyLogo.setText("Add Company Logo");
        }

        tvCompanyInitial.setText(
                getCompanyInitial(
                        companyName
                )
        );


        // =====================================================
        // COMPANY INFORMATION
        // =====================================================

        tvCompanyEmail.setText(
                valueOrDefault(
                        recruiter.getCompanyEmail(),
                        "Not provided"
                )
        );


        tvWebsite.setText(
                valueOrDefault(
                        recruiter.getWebsite(),
                        "Not provided"
                )
        );


        tvAddress.setText(
                valueOrDefault(
                        recruiter.getAddress(),
                        "Not provided"
                )
        );


        tvCompanyDescription.setText(
                valueOrDefault(
                        recruiter.getCompanyDescription(),
                        "No description available."
                )
        );


        // =====================================================
        // VERIFICATION STATUS
        // =====================================================

        tvVerificationStatus.setText(
                "Verification: "
                        +
                        formatStatus(
                                recruiter.getVerificationStatus(),
                                "Pending"
                        )
        );


        // =====================================================
        // PROFILE STATUS
        // =====================================================

        tvProfileStatus.setText(
                "Profile: "
                        +
                        formatStatus(
                                recruiter.getRecruiterStatus(),
                                "Incomplete"
                        )
        );


        // =====================================================
        // USER ACCOUNT INFORMATION
        // =====================================================

        if (user != null) {

            tvRecruiterName.setText(
                    valueOrDefault(
                            user.getFullName(),
                            "Not available"
                    )
            );


            tvAccountEmail.setText(
                    valueOrDefault(
                            user.getEmail(),
                            "Not available"
                    )
            );


            tvPhoneNumber.setText(
                    valueOrDefault(
                            user.getPhoneNumber(),
                            "Not provided"
                    )
            );

        } else {

            tvRecruiterName.setText(
                    "Not available"
            );

            tvAccountEmail.setText(
                    "Not available"
            );

            tvPhoneNumber.setText(
                    "Not provided"
            );
        }
    }


    // =====================================================
    // COMPANY INITIAL
    // =====================================================

    private String getCompanyInitial(
            String companyName
    ) {

        if (
                companyName == null ||
                        companyName.trim().isEmpty()
        ) {

            return "C";
        }


        return companyName
                .trim()
                .substring(0, 1)
                .toUpperCase(Locale.ROOT);
    }


    // =====================================================
    // LOGOUT CONFIRMATION
    // =====================================================

    private void showLogoutConfirmation() {

        new AlertDialog.Builder(this)
                .setTitle(
                        "Logout"
                )
                .setMessage(
                        "Are you sure you want to logout?"
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .setPositiveButton(
                        "Logout",
                        (dialog, which) ->
                                performLogout()
                )
                .show();
    }


    // =====================================================
    // LOGOUT
    // =====================================================

    private void performLogout() {

        // Clear JWT + user session
        sessionManager.logout();


        Intent intent =
                new Intent(
                        RecruiterProfileActivity.this,
                        LoginActivity.class
                );


        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );


        startActivity(
                intent
        );

        finish();
    }


    // =====================================================
    // DEFAULT VALUE
    // =====================================================

    private String valueOrDefault(
            String value,
            String defaultValue
    ) {

        if (
                value == null ||
                        value.trim().isEmpty()
        ) {

            return defaultValue;
        }


        return value;
    }


    // =====================================================
    // FORMAT STATUS
    // =====================================================

    private String formatStatus(
            String status,
            String defaultValue
    ) {

        if (
                status == null ||
                        status.trim().isEmpty()
        ) {

            return defaultValue;
        }


        String clean =
                status
                        .trim()
                        .replace("_", " ")
                        .toLowerCase(Locale.ROOT);


        return clean
                .substring(0, 1)
                .toUpperCase(Locale.ROOT)
                +
                clean.substring(1);
    }


    // =====================================================
    // MESSAGE
    // =====================================================

    private void showMessage(
            String message
    ) {

        Toast.makeText(
                this,
                message == null
                        ? "Something went wrong."
                        : message,
                Toast.LENGTH_LONG
        ).show();
    }
}