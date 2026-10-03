package com.example.jobmatch_frontend.activities;

import android.content.Intent;
import android.net.Uri;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import android.graphics.drawable.Drawable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AlertDialog;

import com.example.jobmatch_frontend.R;
import com.example.jobmatch_frontend.adapters.AccountDocumentAdapter;
import com.example.jobmatch_frontend.api.ApiClient;
import com.example.jobmatch_frontend.api.DocumentApi;
import com.example.jobmatch_frontend.api.ProfileApi;
import com.example.jobmatch_frontend.models.DocumentListResponse;
import com.example.jobmatch_frontend.models.ProfileResponse;
import com.example.jobmatch_frontend.session.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class JobseekerAccountActivity
        extends AppCompatActivity {


    // =============================================
    // PROFILE VIEWS
    // =============================================

    private ImageButton btnBack;

    private TextView tvProfileInitial;
    private ImageView imgProfilePicture;
    private boolean uploadingPhoto = false;
    private final ActivityResultLauncher<String> photoPicker =
            registerForActivityResult(new ActivityResultContracts.GetContent(),
                    uri -> { if (uri != null) uploadProfilePhoto(uri); });

    private TextView tvFullName;
    private TextView tvHeadline;
    private TextView tvResumeSummary;

    private TextView tvEmail;
    private TextView tvPhone;
    private TextView tvLocation;

    private TextView tvSkills;
    private TextView tvEducation;
    private TextView tvExperience;
    private TextView tvExpectedSalary;

    private Button btnEditProfile;
    private Button btnLogout;

    // =============================================
    // DOCUMENT VIEWS
    // =============================================

    private TextView tvResumeFileName;
    private TextView tvResumeStatus;
    private TextView tvNoDocuments;

    private Button btnUploadResume;
    private Button btnUploadDocument;

    private RecyclerView recyclerDocuments;


    // =============================================
    // SESSION + API
    // =============================================

    private SessionManager sessionManager;

    private ProfileApi profileApi;
    private DocumentApi documentApi;


    // =============================================
    // DOCUMENT RECYCLER VIEW
    // =============================================

    private AccountDocumentAdapter documentAdapter;

    private final List<
            DocumentListResponse.DocumentItem
            > documentList =
            new ArrayList<>();


    // =============================================
    // ON CREATE
    // =============================================

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_jobseeker_account
        );


        sessionManager =
                new SessionManager(this);


        // =========================================
        // SESSION CHECK
        // =========================================

        if (!sessionManager.isLoggedIn()) {

            Toast.makeText(
                    this,
                    "Please login again.",
                    Toast.LENGTH_LONG
            ).show();

            finish();

            return;
        }


        initializeViews();

        setupRecyclerView();

        setupApis();

        setupButtons();
    }


    // =============================================
    // INITIALIZE VIEWS
    // =============================================

    private void initializeViews() {

        btnBack =
                findViewById(
                        R.id.btnBack
                );

        tvProfileInitial =
                findViewById(
                        R.id.tvProfileInitial
                );

        imgProfilePicture =
                findViewById(
                        R.id.imgProfilePicture
                );

        tvFullName =
                findViewById(
                        R.id.tvFullName
                );

        tvHeadline =
                findViewById(
                        R.id.tvHeadline
                );

        tvResumeSummary =
                findViewById(
                        R.id.tvResumeSummary
                );

        tvEmail =
                findViewById(
                        R.id.tvEmail
                );

        tvPhone =
                findViewById(
                        R.id.tvPhone
                );

        tvLocation =
                findViewById(
                        R.id.tvLocation
                );

        tvSkills =
                findViewById(
                        R.id.tvSkills
                );

        tvEducation =
                findViewById(
                        R.id.tvEducation
                );

        tvExperience =
                findViewById(
                        R.id.tvExperience
                );

        tvExpectedSalary =
                findViewById(
                        R.id.tvExpectedSalary
                );

        btnEditProfile =
                findViewById(
                        R.id.btnEditProfile
                );

        btnLogout =
                findViewById(
                        R.id.btnLogout
                );

        // =========================================
        // STEP 9H DOCUMENT VIEWS
        // =========================================

        tvResumeFileName =
                findViewById(
                        R.id.tvResumeFileName
                );

        tvResumeStatus =
                findViewById(
                        R.id.tvResumeStatus
                );

        tvNoDocuments =
                findViewById(
                        R.id.tvNoDocuments
                );

        btnUploadResume =
                findViewById(
                        R.id.btnUploadResume
                );

        btnUploadDocument =
                findViewById(
                        R.id.btnUploadDocument
                );

        recyclerDocuments =
                findViewById(
                        R.id.recyclerDocuments
                );
    }


    // =============================================
    // API SETUP
    // =============================================

    private void setupApis() {

        profileApi =
                ApiClient
                        .getClient(sessionManager)
                        .create(ProfileApi.class);


        documentApi =
                ApiClient
                        .getClient(sessionManager)
                        .create(DocumentApi.class);
    }


    // =============================================
    // RECYCLER VIEW
    // =============================================

    private void setupRecyclerView() {

        recyclerDocuments.setLayoutManager(
                new LinearLayoutManager(this)
        );


        documentAdapter =
                new AccountDocumentAdapter(
                        documentList,
                        document -> {

                            DocumentListResponse.OcrInfo ocr =
                                    document.getOcr();


                            if (ocr == null) {

                                Toast.makeText(
                                        JobseekerAccountActivity.this,
                                        "No OCR result available.",
                                        Toast.LENGTH_SHORT
                                ).show();

                                return;
                            }


                            Intent intent =
                                    new Intent(
                                            JobseekerAccountActivity.this,
                                            OcrReviewActivity.class
                                    );


                            intent.putExtra(
                                    "document_id",
                                    document.getDocumentId()
                            );


                            intent.putExtra(
                                    "ocr_id",
                                    ocr.getOcrId()
                            );


                            intent.putExtra(
                                    "file_name",
                                    document.getFileName()
                            );


                            intent.putExtra(
                                    "extracted_text",
                                    ocr.getExtractedText()
                            );


                            intent.putExtra(
                                    "confidence_score",
                                    ocr.getConfidenceScore() != null
                                            ? ocr.getConfidenceScore()
                                            : 0.0
                            );


                            // Open OCR in view-only mode
                            intent.putExtra(
                                    "view_only",
                                    true
                            );


                            startActivity(intent);
                        }
                );


        recyclerDocuments.setAdapter(
                documentAdapter
        );
    }


    // =============================================
    // BUTTONS
    // =============================================

    private void setupButtons() {

        btnBack.setOnClickListener(
                v -> finish()
        );


        // Tap either the profile picture or the initials to change the photo.
        imgProfilePicture.setOnClickListener(v -> photoPicker.launch("image/*"));
        tvProfileInitial.setOnClickListener(v -> photoPicker.launch("image/*"));

        // Edit Profile
        btnEditProfile.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            JobseekerAccountActivity.this,
                            ProfileActivity.class
                    );

            startActivity(intent);
        });


        // Upload / Replace Resume
        btnUploadResume.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            JobseekerAccountActivity.this,
                            DocumentUploadActivity.class
                    );

            intent.putExtra(
                    "document_type",
                    "resume"
            );

            intent.putExtra(
                    "source",
                    "account"
            );

            startActivity(intent);
        });


        // Upload Certificate / Supporting Document
        btnUploadDocument.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            JobseekerAccountActivity.this,
                            DocumentUploadActivity.class
                    );

            intent.putExtra(
                    "document_type",
                    "certificate"
            );

            intent.putExtra(
                    "source",
                    "account"
            );

            startActivity(intent);
        });

        // =========================================
        // LOGOUT
        // =========================================

        btnLogout.setOnClickListener(
                v -> showLogoutConfirmation()
        );
    }

    // =============================================
// LOGOUT CONFIRMATION
// =============================================

    private void uploadProfilePhoto(Uri uri) {
        if (uploadingPhoto) return;
        File file = null;
        try {
            String mime = getContentResolver().getType(uri);
            if (!"image/jpeg".equals(mime) && !"image/png".equals(mime) && !"image/webp".equals(mime)) {
                Toast.makeText(this, "Select JPG, PNG or WebP.", Toast.LENGTH_SHORT).show(); return;
            }
            String ext = "image/png".equals(mime) ? ".png" : "image/webp".equals(mime) ? ".webp" : ".jpg";
            file = new File(getCacheDir(), "profile-photo-" + System.currentTimeMillis() + ext);
            try (InputStream in = getContentResolver().openInputStream(uri);
                 FileOutputStream out = new FileOutputStream(file)) {
                if (in == null) throw new java.io.IOException("Cannot read image");
                byte[] buffer = new byte[8192]; int count; long total = 0;
                while ((count = in.read(buffer)) != -1) {
                    total += count;
                    if (total > 5 * 1024 * 1024) throw new java.io.IOException("Maximum image size is 5 MB");
                    out.write(buffer, 0, count);
                }
            }
            uploadingPhoto = true;
            imgProfilePicture.setEnabled(false); tvProfileInitial.setEnabled(false);
            RequestBody body = RequestBody.create(file, MediaType.parse(mime));
            MultipartBody.Part part = MultipartBody.Part.createFormData("profile_picture", file.getName(), body);
            final File temporaryFile = file;
            profileApi.uploadProfilePicture(part).enqueue(new Callback<ProfileResponse>() {
                @Override public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                    uploadingPhoto = false;
                    imgProfilePicture.setEnabled(true); tvProfileInitial.setEnabled(true); temporaryFile.delete();
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(JobseekerAccountActivity.this, "Profile photo updated.", Toast.LENGTH_SHORT).show();
                        loadProfile();
                    } else Toast.makeText(JobseekerAccountActivity.this, "Unable to upload photo (HTTP " + response.code() + ").", Toast.LENGTH_LONG).show();
                }
                @Override public void onFailure(Call<ProfileResponse> call, Throwable t) {
                    uploadingPhoto = false;
                    imgProfilePicture.setEnabled(true); tvProfileInitial.setEnabled(true); temporaryFile.delete();
                    Toast.makeText(JobseekerAccountActivity.this, "Upload failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            if (file != null) file.delete();
            Toast.makeText(this, "Unable to prepare photo: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showLogoutConfirmation() {

        new AlertDialog.Builder(this)
                .setTitle("Logout")
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


// =============================================
// LOGOUT
// =============================================

    private void performLogout() {

        sessionManager.logout();

        Intent intent =
                new Intent(
                        JobseekerAccountActivity.this,
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


    // =============================================
    // LOAD PROFILE
    // =============================================

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
                                                &&
                                                response.body().isSuccess()
                                ) {

                                    displayProfile(
                                            response.body()
                                    );

                                } else {

                                    Toast.makeText(
                                            JobseekerAccountActivity.this,
                                            "Unable to load profile.",
                                            Toast.LENGTH_LONG
                                    ).show();
                                }
                            }


                            @Override
                            public void onFailure(
                                    Call<ProfileResponse> call,
                                    Throwable t
                            ) {

                                android.util.Log.e(
                                        "ACCOUNT_PROFILE",
                                        "Profile loading error",
                                        t
                                );


                                Toast.makeText(
                                        JobseekerAccountActivity.this,
                                        "Unable to connect to server.",
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                );
    }


    // =============================================
    // LOAD DOCUMENTS
    // =============================================

    private void loadDocuments() {

        documentApi
                .getMyDocuments()
                .enqueue(
                        new Callback<DocumentListResponse>() {

                            @Override
                            public void onResponse(
                                    Call<DocumentListResponse> call,
                                    Response<DocumentListResponse> response
                            ) {

                                if (
                                        response.isSuccessful()
                                                &&
                                                response.body() != null
                                                &&
                                                response.body().isSuccess()
                                ) {

                                    displayDocuments(
                                            response.body()
                                                    .getData()
                                    );

                                } else {

                                    Toast.makeText(
                                            JobseekerAccountActivity.this,
                                            "Unable to load documents.",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }
                            }


                            @Override
                            public void onFailure(
                                    Call<DocumentListResponse> call,
                                    Throwable t
                            ) {

                                android.util.Log.e(
                                        "ACCOUNT_DOCUMENTS",
                                        "Document loading error",
                                        t
                                );


                                Toast.makeText(
                                        JobseekerAccountActivity.this,
                                        "Unable to connect to document server.",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }
                );
    }

    // =============================================
// DISPLAY PROFILE PICTURE
// =============================================

    private void displayProfilePicture(
            String profilePicture
    ) {

        Log.d(
                "PROFILE_IMAGE",
                "Profile picture from API: "
                        + profilePicture
        );


        // =========================================
        // NO PROFILE PICTURE
        // =========================================

        if (
                profilePicture == null
                        ||
                        profilePicture.trim().isEmpty()
        ) {

            imgProfilePicture.setVisibility(
                    View.GONE
            );

            tvProfileInitial.setVisibility(
                    View.VISIBLE
            );

            return;
        }


        // =========================================
        // BUILD IMAGE URL
        // =========================================

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
                    ApiClient.SERVER_URL.replaceAll("/+$", "")
                            + "/"
                            + profilePicture.replaceFirst("^/+", "");
        }


        Log.d(
                "PROFILE_IMAGE",
                "Final image URL: "
                        + imageUrl
        );


        // =========================================
        // LOAD IMAGE
        // =========================================

        Glide.with(this)
                .load(imageUrl)
                .centerCrop()
                .listener(
                        new RequestListener<Drawable>() {

                            @Override
                            public boolean onLoadFailed(
                                    GlideException e,
                                    Object model,
                                    Target<Drawable> target,
                                    boolean isFirstResource
                            ) {

                                Log.e(
                                        "PROFILE_IMAGE",
                                        "Glide failed to load: "
                                                + imageUrl,
                                        e
                                );


                                imgProfilePicture.setVisibility(
                                        View.GONE
                                );

                                tvProfileInitial.setVisibility(
                                        View.VISIBLE
                                );


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

                                Log.d(
                                        "PROFILE_IMAGE",
                                        "Profile picture loaded successfully"
                                );


                                tvProfileInitial.setVisibility(
                                        View.GONE
                                );

                                imgProfilePicture.setVisibility(
                                        View.VISIBLE
                                );


                                return false;
                            }
                        }
                )
                .into(
                        imgProfilePicture
                );
    }

    // =============================================
    // DISPLAY PROFILE
    // =============================================

    private void displayProfile(
            ProfileResponse data
    ) {

        ProfileResponse.UserData user =
                data.getUser();

        ProfileResponse.ProfileData profile =
                data.getProfile();


        // =========================================
        // USER DATA
        // =========================================

        if (user != null) {

            String fullName =
                    safeText(
                            user.getFullName()
                    );


            tvFullName.setText(
                    fullName
            );


            tvProfileInitial.setText(
                    getInitial(fullName)
            );


            tvEmail.setText(
                    "Email: "
                            + safeText(
                            user.getEmail()
                    )
            );


            tvPhone.setText(
                    "Phone: "
                            + safeText(
                            user.getPhoneNumber()
                    )
            );
        }


        // =========================================
        // PROFILE DATA
        // =========================================

        if (profile != null) {

            // =========================================
            // PROFILE PICTURE
            // =========================================

            displayProfilePicture(
                    profile.getProfilePicture()
            );

            //HEADLINE

            String headline =
                    profile.getHeadline();


            if (
                    headline == null
                            ||
                            headline.trim().isEmpty()
            ) {

                tvHeadline.setText(
                        "Job Seeker"
                );

            } else {

                tvHeadline.setText(
                        headline.trim()
                );
            }


            tvResumeSummary.setText(
                    safeText(
                            profile.getResumeSummary()
                    )
            );


            tvLocation.setText(
                    "Location: "
                            + safeText(
                            profile.getLocation()
                    )
            );


            tvSkills.setText(
                    "Skills\n"
                            + safeText(
                            profile.getSkills()
                    )
            );


            tvEducation.setText(
                    "Education\n"
                            + safeText(
                            profile.getEducation()
                    )
            );


            tvExperience.setText(
                    "Experience\n"
                            + safeText(
                            profile.getExperience()
                    )
            );


            tvExpectedSalary.setText(
                    formatSalary(
                            profile.getExpectedSalary()
                    )
            );
        }
    }

    // =============================================
    // DISPLAY DOCUMENTS
    // =============================================

    private void displayDocuments(
            List<DocumentListResponse.DocumentItem> documents
    ) {

        documentList.clear();


        DocumentListResponse.DocumentItem latestResume =
                null;


        if (documents != null) {

            for (
                    DocumentListResponse.DocumentItem document
                    : documents
            ) {

                if (
                        "resume".equalsIgnoreCase(
                                document.getDocumentType()
                        )
                ) {

                    // API already returns newest documents first,
                    // therefore the first resume is the latest.
                    if (latestResume == null) {

                        latestResume =
                                document;
                    }

                } else {

                    documentList.add(
                            document
                    );
                }
            }
        }


        // =========================================
        // MY RESUME
        // =========================================

        if (latestResume != null) {

            tvResumeFileName.setText(
                    safeText(
                            latestResume.getFileName()
                    )
            );


            tvResumeStatus.setText(
                    "Status: "
                            + capitalizeText(
                            latestResume.getUploadStatus()
                    )
            );


            btnUploadResume.setText(
                    "Replace Resume"
            );

        } else {

            tvResumeFileName.setText(
                    "No resume uploaded yet."
            );


            tvResumeStatus.setText(
                    "Upload a resume to improve job matching."
            );


            btnUploadResume.setText(
                    "Upload Resume"
            );
        }


        // =========================================
        // CERTIFICATES / OTHER DOCUMENTS
        // =========================================

        if (documentList.isEmpty()) {

            tvNoDocuments.setVisibility(
                    View.VISIBLE
            );


            recyclerDocuments.setVisibility(
                    View.GONE
            );

        } else {

            tvNoDocuments.setVisibility(
                    View.GONE
            );


            recyclerDocuments.setVisibility(
                    View.VISIBLE
            );
        }


        documentAdapter.notifyDataSetChanged();
    }


    // =============================================
    // SALARY
    // =============================================

    private String formatSalary(
            Double salary
    ) {

        if (salary == null) {

            return "Expected Salary\nNot specified";
        }


        return String.format(
                Locale.getDefault(),
                "Expected Salary\nRM %,.0f",
                salary
        );
    }


    // =============================================
    // INITIAL
    // =============================================

    private String getInitial(
            String name
    ) {

        if (
                name == null
                        ||
                        name.trim().isEmpty()
                        ||
                        "Not provided".equals(name)
        ) {

            return "J";
        }


        return String
                .valueOf(
                        name.trim()
                                .charAt(0)
                )
                .toUpperCase(
                        Locale.getDefault()
                );
    }


    // =============================================
    // SAFE TEXT
    // =============================================

    private String safeText(
            String value
    ) {

        if (
                value == null
                        ||
                        value.trim().isEmpty()
        ) {

            return "Not provided";
        }


        return value.trim();
    }


    // =============================================
    // CAPITALIZE STATUS / DOCUMENT TYPE
    // =============================================

    private String capitalizeText(
            String value
    ) {

        if (
                value == null
                        ||
                        value.trim().isEmpty()
        ) {

            return "Unknown";
        }


        value = value.trim();


        return value.substring(0, 1)
                .toUpperCase(
                        Locale.getDefault()
                )
                + value.substring(1)
                .toLowerCase(
                        Locale.getDefault()
                );
    }


    // =============================================
    // REFRESH WHEN RETURNING TO ACCOUNT
    // =============================================

    @Override
    protected void onResume() {

        super.onResume();


        if (
                sessionManager != null
                        &&
                        sessionManager.isLoggedIn()
        ) {

            if (profileApi != null) {

                loadProfile();
            }


            if (documentApi != null) {

                loadDocuments();
            }
        }
    }
}