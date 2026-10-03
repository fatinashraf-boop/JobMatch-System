package com.example.jobmatch_frontend.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.jobmatch_frontend.R;
import com.example.jobmatch_frontend.api.ApiClient;
import com.example.jobmatch_frontend.api.ApplicationApi;
import com.example.jobmatch_frontend.models.Application;
import com.example.jobmatch_frontend.models.ApplicationDetailsResponse;
import com.example.jobmatch_frontend.session.SessionManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApplicationDetailsActivity
        extends AppCompatActivity {

    private TextView tvJobTitle;
    private TextView tvCompanyName;
    private TextView tvApplicationStatus;
    private TextView tvRecruitmentStatus;
    private TextView tvAppliedDate;
    private TextView tvReviewedDate;
    private TextView tvLocation;
    private TextView tvJobType;
    private TextView tvSalary;
    private TextView tvDescription;
    private TextView tvSkills;
    private TextView tvQualifications;

    private Button btnBack;

    private ApplicationApi applicationApi;

    private int applicationId;

    private TextView tvStageApplied;
    private TextView tvStageReviewed;
    private TextView tvStageShortlisted;
    private TextView tvStageInterviewed;
    private TextView tvStageOffer;


    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_application_details
        );


        applicationId =
                getIntent()
                        .getIntExtra(
                                "application_id",
                                -1
                        );


        if (applicationId <= 0) {

            Toast.makeText(
                    this,
                    "Invalid application.",
                    Toast.LENGTH_LONG
            ).show();

            finish();

            return;
        }


        initializeViews();


        SessionManager sessionManager =
                new SessionManager(this);


        applicationApi =
                ApiClient
                        .getClient(sessionManager)
                        .create(ApplicationApi.class);


        btnBack.setOnClickListener(
                v -> finish()
        );


        loadApplicationDetails();
    }


    private void initializeViews() {

        btnBack =
                findViewById(
                        R.id.btnBack
                );

        tvJobTitle =
                findViewById(
                        R.id.tvJobTitle
                );

        tvCompanyName =
                findViewById(
                        R.id.tvCompanyName
                );

        tvApplicationStatus =
                findViewById(
                        R.id.tvApplicationStatus
                );

        tvRecruitmentStatus =
                findViewById(
                        R.id.tvRecruitmentStatus
                );

        tvAppliedDate =
                findViewById(
                        R.id.tvAppliedDate
                );

        tvReviewedDate =
                findViewById(
                        R.id.tvReviewedDate
                );

        tvLocation =
                findViewById(
                        R.id.tvLocation
                );

        tvJobType =
                findViewById(
                        R.id.tvJobType
                );

        tvSalary =
                findViewById(
                        R.id.tvSalary
                );

        tvDescription =
                findViewById(
                        R.id.tvDescription
                );

        tvSkills =
                findViewById(
                        R.id.tvSkills
                );

        tvQualifications =
                findViewById(
                        R.id.tvQualifications
                );

        tvStageApplied =
                findViewById(
                        R.id.tvStageApplied
                );

        tvStageReviewed =
                findViewById(
                        R.id.tvStageReviewed
                );

        tvStageShortlisted =
                findViewById(
                        R.id.tvStageShortlisted
                );

        tvStageInterviewed =
                findViewById(
                        R.id.tvStageInterviewed
                );

        tvStageOffer =
                findViewById(
                        R.id.tvStageOffer
                );
    }


    private void loadApplicationDetails() {

        applicationApi
                .getApplicationDetails(
                        applicationId
                )
                .enqueue(
                        new Callback<ApplicationDetailsResponse>() {

                            @Override
                            public void onResponse(
                                    Call<ApplicationDetailsResponse> call,
                                    Response<ApplicationDetailsResponse> response
                            ) {

                                if (
                                        response.isSuccessful()
                                                &&
                                                response.body() != null
                                                &&
                                                response.body().isSuccess()
                                                &&
                                                response.body().getData() != null
                                ) {

                                    displayApplication(
                                            response.body()
                                                    .getData()
                                    );

                                    return;
                                }


                                Toast.makeText(
                                        ApplicationDetailsActivity.this,
                                        "Unable to load application details.",
                                        Toast.LENGTH_LONG
                                ).show();
                            }


                            @Override
                            public void onFailure(
                                    Call<ApplicationDetailsResponse> call,
                                    Throwable t
                            ) {

                                Toast.makeText(
                                        ApplicationDetailsActivity.this,
                                        "Unable to connect to server.",
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                );

    }


    private void displayApplication(
            Application application
    ) {

        tvJobTitle.setText(
                safeText(
                        application.getJobTitle(),
                        "Job"
                )
        );


        tvCompanyName.setText(
                safeText(
                        application.getCompanyName(),
                        "Company"
                )
        );


        String applicationStatus =
                safeText(
                        application.getApplicationStatus(),
                        "pending"
                );


        tvApplicationStatus.setText(
                "Application Status: "
                        +
                        capitalize(
                                applicationStatus
                        )
        );


        String shortlistStatus =
                application.getShortlistStatus();

        updateProgressTracker(
                applicationStatus,
                shortlistStatus,
                application.getReviewedAt()
        );

        tvRecruitmentStatus.setText(
                "Recruitment Status: "
                        +
                        getRecruitmentStatus(
                                applicationStatus,
                                shortlistStatus
                        )
        );


        tvAppliedDate.setText(
                "Applied: "
                        +
                        formatDate(
                                application.getAppliedAt()
                        )
        );


        if (
                application.getReviewedAt() == null
                        ||
                        application.getReviewedAt()
                                .trim()
                                .isEmpty()
        ) {

            tvReviewedDate.setVisibility(
                    View.GONE
            );

        } else {

            tvReviewedDate.setVisibility(
                    View.VISIBLE
            );

            tvReviewedDate.setText(
                    "Reviewed: "
                            +
                            formatDate(
                                    application.getReviewedAt()
                            )
            );
        }


        tvLocation.setText(
                "Location: "
                        +
                        safeText(
                                application.getLocation(),
                                "Not specified"
                        )
        );


        tvJobType.setText(
                "Job Type: "
                        +
                        safeText(
                                application.getJobType(),
                                "Not specified"
                        )
        );


        tvSalary.setText(
                formatSalary(
                        application.getSalaryMin(),
                        application.getSalaryMax()
                )
        );


        tvDescription.setText(
                "Job Description\n\n"
                        +
                        safeText(
                                application.getJobDescription(),
                                "Not provided"
                        )
        );


        tvSkills.setText(
                "Required Skills\n\n"
                        +
                        safeText(
                                application.getRequiredSkills(),
                                "Not specified"
                        )
        );


        tvQualifications.setText(
                "Qualifications\n\n"
                        +
                        safeText(
                                application.getQualifications(),
                                "Not specified"
                        )
        );
    }

    // ==========================================
// RECRUITMENT PROGRESS TRACKER
// ==========================================

    private void updateProgressTracker(
            String applicationStatus,
            String shortlistStatus,
            String reviewedAt
    ) {

        int currentStage = 1;


        // Reviewed
        if (
                reviewedAt != null
                        &&
                        !reviewedAt.trim().isEmpty()
        ) {

            currentStage = 2;
        }


        if (
                applicationStatus != null
                        &&
                        applicationStatus.equalsIgnoreCase(
                                "reviewed"
                        )
        ) {

            currentStage =
                    Math.max(
                            currentStage,
                            2
                    );
        }


        // Shortlisted
        if (
                applicationStatus != null
                        &&
                        applicationStatus.equalsIgnoreCase(
                                "shortlisted"
                        )
        ) {

            currentStage =
                    Math.max(
                            currentStage,
                            3
                    );
        }


        // Recruitment stages
        if (
                shortlistStatus != null
                        &&
                        !shortlistStatus.trim().isEmpty()
        ) {

            switch (
                    shortlistStatus.toLowerCase(
                            Locale.getDefault()
                    )
            ) {

                case "active":

                    currentStage =
                            Math.max(
                                    currentStage,
                                    3
                            );

                    break;


                case "interviewed":

                    currentStage =
                            Math.max(
                                    currentStage,
                                    4
                            );

                    break;


                case "offer_sent":

                case "hired":

                    currentStage =
                            Math.max(
                                    currentStage,
                                    5
                            );

                    break;
            }
        }


        // Update tracker UI
        styleStage(
                tvStageApplied,
                "Applied",
                currentStage >= 1
        );


        styleStage(
                tvStageReviewed,
                "Reviewed",
                currentStage >= 2
        );


        styleStage(
                tvStageShortlisted,
                "Shortlisted",
                currentStage >= 3
        );


        styleStage(
                tvStageInterviewed,
                "Interviewed",
                currentStage >= 4
        );


        styleStage(
                tvStageOffer,
                "Offer/Hired",
                currentStage >= 5
        );
    }
    private String getRecruitmentStatus(
            String applicationStatus,
            String shortlistStatus
    ) {

        if (
                applicationStatus.equalsIgnoreCase("rejected")
                        ||
                        applicationStatus.equalsIgnoreCase("withdrawn")
        ) {

            return "Not active";
        }


        if (
                shortlistStatus == null
                        ||
                        shortlistStatus.trim().isEmpty()
        ) {

            return "Not shortlisted yet";
        }


        switch (
                shortlistStatus
                        .toLowerCase(
                                Locale.getDefault()
                        )
        ) {

            case "active":
                return "Shortlisted";

            case "interviewed":
                return "Interviewed";

            case "offer_sent":
                return "Offer Sent";

            case "hired":
                return "Hired";

            case "removed":
                return "Removed from Shortlist";

            default:
                return capitalize(
                        shortlistStatus
                                .replace(
                                        "_",
                                        " "
                                )
                );
        }
    }


    private String formatSalary(
            Double min,
            Double max
    ) {

        if (
                min == null
                        &&
                        max == null
        ) {

            return "Salary: Not specified";
        }


        if (
                min != null
                        &&
                        max != null
        ) {

            return String.format(
                    Locale.getDefault(),
                    "Salary: RM %,.0f - RM %,.0f",
                    min,
                    max
            );
        }


        if (min != null) {

            return String.format(
                    Locale.getDefault(),
                    "Salary: From RM %,.0f",
                    min
            );
        }


        return String.format(
                Locale.getDefault(),
                "Salary: Up to RM %,.0f",
                max
        );
    }


    private String formatDate(
            String value
    ) {

        if (
                value == null
                        ||
                        value.trim().isEmpty()
        ) {

            return "-";
        }


        String[] formats = {

                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                "yyyy-MM-dd'T'HH:mm:ssXXX",
                "yyyy-MM-dd HH:mm:ss"
        };


        for (String format : formats) {

            try {

                SimpleDateFormat parser =
                        new SimpleDateFormat(
                                format,
                                Locale.getDefault()
                        );


                Date date =
                        parser.parse(
                                value.trim()
                        );


                if (date != null) {

                    SimpleDateFormat output =
                            new SimpleDateFormat(
                                    "dd MMM yyyy, h:mm a",
                                    Locale.getDefault()
                            );


                    return output.format(
                            date
                    );
                }


            } catch (ParseException ignored) {
            }
        }


        return value;
    }


    /*
 ==========================================
 STYLE PROGRESS STAGE
 ==========================================
*/

    private void styleStage(
            TextView view,
            String label,
            boolean completed
    ) {

        if (completed) {

            view.setAlpha(
                    1.0f
            );

            view.setText(
                    "✓ " + label
            );

        } else {

            view.setAlpha(
                    0.4f
            );

            view.setText(
                    label
            );
        }
    }

    private String safeText(
            String value,
            String fallback
    ) {

        if (
                value == null
                        ||
                        value.trim().isEmpty()
        ) {

            return fallback;
        }


        return value.trim();
    }


    private String capitalize(
            String value
    ) {

        if (
                value == null
                        ||
                        value.trim().isEmpty()
        ) {

            return "";
        }


        String text =
                value.trim()
                        .replace(
                                "_",
                                " "
                        );


        return text.substring(0, 1)
                .toUpperCase(
                        Locale.getDefault()
                )
                +
                text.substring(1)
                        .toLowerCase(
                                Locale.getDefault()
                        );
    }
}
