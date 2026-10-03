package com.example.jobmatch_frontend.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import android.view.View;
import android.graphics.Color;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.example.jobmatch_frontend.api.ApiClient;
import com.example.jobmatch_frontend.api.DocumentApi;
import com.example.jobmatch_frontend.models.DocumentSkillResponse;
import com.example.jobmatch_frontend.models.SkillReviewResponse;
import com.example.jobmatch_frontend.session.SessionManager;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.List;


import androidx.appcompat.app.AppCompatActivity;

import com.example.jobmatch_frontend.R;

import java.util.Locale;


public class OcrReviewActivity extends AppCompatActivity {

    private ImageButton btnBack;

    private Button btnFinish;

    private TextView tvFileName;
    private TextView tvConfidence;
    private TextView tvExtractedText;
    private TextView tvDocumentId;
    private TextView tvOcrId;


    // ==========================================
    // NAVIGATION SOURCE
    // ==========================================

    private String source = "onboarding";

    private boolean viewOnly = false;


    private DocumentApi documentApi;

    private LinearLayout skillsContainer;
    private ProgressBar progressSkills;
    private TextView tvSkillsStatus;
    private Button btnRetrySkills;

    private int currentDocumentId = -1;
    private boolean skillRequestRunning = false;
    private boolean skillsLoaded = false;
    private boolean hasPendingSkills = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_ocr_review
        );

        SessionManager sessionManager =
                new SessionManager(this);

        documentApi = ApiClient
                .getClient(sessionManager)
                .create(DocumentApi.class);

        skillsContainer =
                findViewById(R.id.skillsContainer);

        progressSkills =
                findViewById(R.id.progressSkills);

        tvSkillsStatus =
                findViewById(R.id.tvSkillsStatus);

        btnRetrySkills =
                findViewById(R.id.btnRetrySkills);

        btnRetrySkills.setOnClickListener(v ->
                loadDocumentSkills()
        );

        // ==========================================
        // INITIALIZE VIEWS
        // ==========================================

        btnBack =
                findViewById(
                        R.id.btnBack
                );


        btnFinish =
                findViewById(
                        R.id.btnFinish
                );


        tvFileName =
                findViewById(
                        R.id.tvFileName
                );


        tvConfidence =
                findViewById(
                        R.id.tvConfidence
                );


        tvExtractedText =
                findViewById(
                        R.id.tvExtractedText
                );


        tvDocumentId =
                findViewById(
                        R.id.tvDocumentId
                );


        tvOcrId =
                findViewById(
                        R.id.tvOcrId
                );


        // ==========================================
        // GET DATA FROM INTENT
        // ==========================================

        Intent receivedIntent =
                getIntent();


        int documentId =
                receivedIntent.getIntExtra(
                        "document_id",
                        -1
                );
        currentDocumentId = documentId;

        int ocrId =
                receivedIntent.getIntExtra(
                        "ocr_id",
                        -1
                );


        String fileName =
                receivedIntent.getStringExtra(
                        "file_name"
                );


        String extractedText =
                receivedIntent.getStringExtra(
                        "extracted_text"
                );


        double confidence =
                receivedIntent.getDoubleExtra(
                        "confidence_score",
                        0.0
                );


        // ==========================================
        // GET NAVIGATION SOURCE
        // ==========================================

        String requestedSource =
                receivedIntent.getStringExtra(
                        "source"
                );


        if (
                requestedSource != null
                        &&
                        "account".equalsIgnoreCase(
                                requestedSource.trim()
                        )
        ) {

            source = "account";
        }


        // ==========================================
        // VIEW ONLY MODE
        // ==========================================

        viewOnly =
                receivedIntent.getBooleanExtra(
                        "view_only",
                        false
                );


        if (viewOnly) {

            btnFinish.setText(
                    "BACK"
            );
        }


        // ==========================================
        // FILE NAME
        // ==========================================

        if (
                fileName == null
                        ||
                        fileName.trim().isEmpty()
        ) {

            fileName =
                    "Uploaded Document";
        }


        tvFileName.setText(
                fileName
        );


        // ==========================================
        // OCR CONFIDENCE
        // ==========================================

        String formattedConfidence =
                String.format(
                        Locale.getDefault(),
                        "%.2f%%",
                        confidence
                );


        tvConfidence.setText(
                formattedConfidence
        );


        // ==========================================
        // EXTRACTED TEXT
        // ==========================================

        if (
                extractedText == null
                        ||
                        extractedText.trim().isEmpty()
        ) {

            extractedText =
                    "No readable text was detected in this document.";
        }


        tvExtractedText.setText(
                extractedText
        );


        // ==========================================
        // DOCUMENT ID
        // ==========================================

        if (documentId != -1) {

            tvDocumentId.setText(
                    "Document ID: "
                            + documentId
            );

        } else {

            tvDocumentId.setText(
                    "Document ID: unavailable"
            );
        }


        // ==========================================
        // OCR ID
        // ==========================================

        if (ocrId != -1) {

            tvOcrId.setText(
                    "OCR ID: "
                            + ocrId
            );

        } else {

            tvOcrId.setText(
                    "OCR ID: unavailable"
            );
        }


        // ==========================================
        // SUCCESS MESSAGE
        // ==========================================

        if (!viewOnly) {

            Toast.makeText(
                    this,
                    "OCR completed successfully",
                    Toast.LENGTH_SHORT
            ).show();
        }


        // ==========================================
        // BACK BUTTON
        // ==========================================

        btnBack.setOnClickListener(v -> {

            /*
             * Existing document opened from Account.
             */
            if (viewOnly) {

                finish();

                return;
            }


            /*
             * Newly uploaded document from Account.
             */
            if ("account".equals(source)) {

                returnToAccount();

                return;
            }


            /*
             * During onboarding, Back returns to
             * DocumentUploadActivity.
             */
            finish();
        });


        // ==========================================
        // DONE / BACK BUTTON
        // ==========================================


        btnFinish.setOnClickListener(v -> {

            // Prevent navigation while saving a skill review
            if (skillRequestRunning) {

                Toast.makeText(
                        OcrReviewActivity.this,
                        "Please wait for the skill review request.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            // Existing OCR opened from Account:
            // retain the original Back behaviour.
            if (viewOnly) {
                finish();
                return;
            }

            // Check whether suggestions have loaded
            if (!skillsLoaded && currentDocumentId > 0) {

                new androidx.appcompat.app.AlertDialog.Builder(
                        OcrReviewActivity.this
                )
                        .setTitle("Skill Review Not Loaded")
                        .setMessage(
                                "The skill suggestions have not loaded. " +
                                        "Please retry before continuing."
                        )
                        .setPositiveButton(
                                "Retry",
                                (dialog, which) -> loadDocumentSkills()
                        )
                        .setNegativeButton("Cancel", null)
                        .show();

                return;
            }

            // Warn if some suggestions remain pending
            if (hasPendingSkills) {

                new androidx.appcompat.app.AlertDialog.Builder(
                        OcrReviewActivity.this
                )
                        .setTitle("Unreviewed Skills")
                        .setMessage(
                                "Some extracted skills are still pending. " +
                                        "Only confirmed skills will be included " +
                                        "in your structured matching skills."
                        )
                        .setPositiveButton(
                                "Continue",
                                (dialog, which) -> completeOcrReview()
                        )
                        .setNegativeButton(
                                "Keep Reviewing",
                                null
                        )
                        .show();

                return;
            }

            // Continue using your existing navigation
            completeOcrReview();
        });


        loadDocumentSkills();
    }

    private void showSkillSuggestions(
            List<DocumentSkillResponse.SkillItem> skills
    ) {

        skillsContainer.removeAllViews();
        hasPendingSkills = false;

        if (skills == null || skills.isEmpty()) {

            tvSkillsStatus.setText(
                    "No skills were detected in this document."
            );

            return;
        }

        int pendingCount = 0;

        for (DocumentSkillResponse.SkillItem skill : skills) {

            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(20, 20, 20, 20);
            card.setBackgroundColor(Color.rgb(238, 249, 251));

            LinearLayout.LayoutParams cardParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            cardParams.bottomMargin = 16;
            card.setLayoutParams(cardParams);

            // Skill name
            TextView tvSkillName = new TextView(this);
            tvSkillName.setText(skill.getSkillName());
            tvSkillName.setTextSize(17);
            tvSkillName.setTextColor(Color.BLACK);

            card.addView(tvSkillName);

            // Review status
            String status = skill.getReviewStatus();

            if (status == null) {
                status = "pending";
            }

            TextView tvStatus = new TextView(this);
            tvStatus.setText("Status: " + status);
            tvStatus.setTextSize(13);

            card.addView(tvStatus);

            // Only pending skills can be reviewed
            if ("pending".equalsIgnoreCase(status)) {

                pendingCount++;
                hasPendingSkills = true;

                LinearLayout buttonLayout =
                        new LinearLayout(this);

                buttonLayout.setOrientation(
                        LinearLayout.HORIZONTAL
                );

                Button btnConfirm = new Button(this);
                btnConfirm.setText("Confirm");

                Button btnReject = new Button(this);
                btnReject.setText("Reject");

                btnConfirm.setOnClickListener(v ->
                        reviewSkill(
                                skill.getDocumentSkillId(),
                                "confirmed"
                        )
                );

                btnReject.setOnClickListener(v ->
                        reviewSkill(
                                skill.getDocumentSkillId(),
                                "rejected"
                        )
                );

                buttonLayout.addView(btnConfirm);
                buttonLayout.addView(btnReject);

                card.addView(buttonLayout);
            }

            skillsContainer.addView(card);
        }

        tvSkillsStatus.setText(
                pendingCount + " skill(s) awaiting review."
        );
    }





    private void completeOcrReview() {

        // ======================================
        // VIEW EXISTING OCR ONLY
        // ======================================

        if (viewOnly) {
            finish();
            return;
        }

        // ======================================
        // UPLOAD STARTED FROM ACCOUNT
        // ======================================

        if ("account".equals(source)) {

            Toast.makeText(
                    OcrReviewActivity.this,
                    "Document uploaded successfully!",
                    Toast.LENGTH_SHORT
            ).show();

            returnToAccount();
            return;
        }

        // ======================================
        // ORIGINAL ONBOARDING FLOW
        // ======================================

        Toast.makeText(
                OcrReviewActivity.this,
                "Profile setup completed!",
                Toast.LENGTH_SHORT
        ).show();

        Intent intent = new Intent(
                OcrReviewActivity.this,
                JobseekerDashboardActivity.class
        );

        // Clear onboarding activities
        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);

        finish();
    }


    private void loadDocumentSkills() {

        if (currentDocumentId <= 0) {
            tvSkillsStatus.setText(
                    "Document ID is unavailable."
            );
            return;
        }

        if (skillRequestRunning) {
            return;
        }

        skillRequestRunning = true;
        skillsLoaded = false;

        progressSkills.setVisibility(View.VISIBLE);
        btnRetrySkills.setVisibility(View.GONE);

        tvSkillsStatus.setText(
                "Loading extracted skills..."
        );

        documentApi
                .getDocumentSkills(currentDocumentId)
                .enqueue(
                        new Callback<DocumentSkillResponse>() {

                            @Override
                            public void onResponse(
                                    Call<DocumentSkillResponse> call,
                                    Response<DocumentSkillResponse> response
                            ) {

                                if (isFinishing() || isDestroyed()) {
                                    return;
                                }

                                skillRequestRunning = false;
                                progressSkills.setVisibility(View.GONE);

                                if (
                                        response.isSuccessful() &&
                                                response.body() != null &&
                                                response.body().isSuccess()
                                ) {

                                    skillsLoaded = true;

                                    showSkillSuggestions(
                                            response.body().getData()
                                    );

                                } else {

                                    tvSkillsStatus.setText(
                                            "Unable to load skills (HTTP " +
                                                    response.code() + ")."
                                    );

                                    btnRetrySkills.setVisibility(
                                            View.VISIBLE
                                    );
                                }
                            }

                            @Override
                            public void onFailure(
                                    Call<DocumentSkillResponse> call,
                                    Throwable t
                            ) {

                                if (isFinishing() || isDestroyed()) {
                                    return;
                                }

                                skillRequestRunning = false;
                                progressSkills.setVisibility(View.GONE);

                                tvSkillsStatus.setText(
                                        "Connection failed. Please retry."
                                );

                                btnRetrySkills.setVisibility(
                                        View.VISIBLE
                                );
                            }
                        }
                );
    }


    private void reviewSkill(
            int skillId,
            String reviewStatus
    ) {

        if (skillRequestRunning) {
            return;
        }

        skillRequestRunning = true;

        progressSkills.setVisibility(View.VISIBLE);

        // Prevent duplicate taps while saving.
        skillsContainer.setEnabled(false);
        skillsContainer.setAlpha(0.5f);

        JsonObject body = new JsonObject();

        body.addProperty(
                "review_status",
                reviewStatus
        );

        documentApi.reviewDocumentSkill(
                currentDocumentId,
                skillId,
                body
        ).enqueue(new Callback<SkillReviewResponse>() {

            @Override
            public void onResponse(
                    Call<SkillReviewResponse> call,
                    Response<SkillReviewResponse> response
            ) {

                if (isFinishing() || isDestroyed()) {
                    return;
                }

                skillRequestRunning = false;
                progressSkills.setVisibility(View.GONE);

                skillsContainer.setEnabled(true);
                skillsContainer.setAlpha(1f);

                if (
                        response.isSuccessful() &&
                                response.body() != null &&
                                response.body().isSuccess()
                ) {

                    SkillReviewResponse result =
                            response.body();

                    String message = result.getMessage();

                    if (result.isMatchingUpdated()) {

                        message +=
                                "\nRecommendations refreshed.";

                    } else if (
                            result.getMatchingWarning() != null
                    ) {

                        message += "\n" +
                                result.getMatchingWarning();
                    }

                    Toast.makeText(
                            OcrReviewActivity.this,
                            message,
                            Toast.LENGTH_LONG
                    ).show();

                    loadDocumentSkills();

                } else {

                    tvSkillsStatus.setText(
                            "Unable to save review. HTTP " +
                                    response.code()
                    );

                    // Reload in case a previous request
                    // was saved but the response was lost.
                    loadDocumentSkills();
                }
            }

            @Override
            public void onFailure(
                    Call<SkillReviewResponse> call,
                    Throwable t
            ) {

                if (isFinishing() || isDestroyed()) {
                    return;
                }

                skillRequestRunning = false;
                progressSkills.setVisibility(View.GONE);

                skillsContainer.setEnabled(true);
                skillsContainer.setAlpha(1f);

                tvSkillsStatus.setText(
                        "Connection lost. Reload skills before retrying."
                );

                btnRetrySkills.setVisibility(View.VISIBLE);

                // Do not assume the review failed:
                // the server may have saved it before
                // the connection was interrupted.
            }
        });
    }

    // ==========================================
    // RETURN TO ACCOUNT
    // ==========================================

    private void returnToAccount() {

        Intent intent =
                new Intent(
                        OcrReviewActivity.this,
                        JobseekerAccountActivity.class
                );


        /*
         * Reuse the Account activity that is
         * already underneath the upload flow.
         *
         * This removes:
         * DocumentUploadActivity
         * OcrReviewActivity
         */
        intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP
                        |
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
        );


        startActivity(intent);

        finish();
    }
}