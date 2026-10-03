package com.example.jobmatch_frontend.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.jobmatch_frontend.R;
import com.example.jobmatch_frontend.api.ApiClient;
import com.example.jobmatch_frontend.api.RecruiterJobApi;
import com.example.jobmatch_frontend.models.CreateJobRequest;
import com.example.jobmatch_frontend.models.CreateJobResponse;
import com.example.jobmatch_frontend.models.RecruiterJobListResponse;
import com.example.jobmatch_frontend.models.RecruiterJobResponse;
import com.example.jobmatch_frontend.session.SessionManager;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class CreateJobActivity extends AppCompatActivity {

    // =====================================================
    // VIEWS
    // =====================================================

    private TextView tvPageTitle;
    private TextView tvPageSubtitle;

    private EditText etJobTitle;
    private EditText etDescription;
    private EditText etSkills;
    private EditText etQualifications;
    private EditText etLocation;
    private EditText etSalaryMin;
    private EditText etSalaryMax;

    private Spinner spinnerJobType;
    private Spinner spinnerStatus;

    private Button btnSubmitJob;

    private ProgressBar progressBar;


    // =====================================================
    // SESSION / API
    // =====================================================

    private SessionManager sessionManager;
    private RecruiterJobApi recruiterJobApi;


    // =====================================================
    // MODE
    // =====================================================

    private boolean editMode = false;
    private int jobId = -1;


    // =====================================================
    // ON CREATE
    // =====================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_create_job);


        sessionManager =
                new SessionManager(this);


        // =================================================
        // SESSION CHECK
        // =================================================

        if (
                sessionManager.getToken() == null ||
                        sessionManager.getToken().isEmpty()
        ) {

            showMessage(
                    "Session expired. Please log in again."
            );

            finish();

            return;
        }


        // =================================================
        // ROLE CHECK
        // =================================================

        String role =
                sessionManager.getRole();


        if (
                role == null ||
                        !role.equalsIgnoreCase("recruiter")
        ) {

            showMessage(
                    "Recruiter access only."
            );

            finish();

            return;
        }


        // =================================================
        // GET MODE FROM INTENT
        // =================================================

        editMode =
                getIntent().getBooleanExtra(
                        "edit_mode",
                        false
                );


        jobId =
                getIntent().getIntExtra(
                        "job_id",
                        -1
                );


        // =================================================
        // INITIALIZE
        // =================================================

        initializeViews();

        setupApi();

        setupButtons();

        configureMode();
    }


    // =====================================================
    // INITIALIZE VIEWS
    // =====================================================

    private void initializeViews() {

        tvPageTitle =
                findViewById(
                        R.id.tvPageTitle
                );

        tvPageSubtitle =
                findViewById(
                        R.id.tvPageSubtitle
                );


        etJobTitle =
                findViewById(
                        R.id.etJobTitle
                );

        etDescription =
                findViewById(
                        R.id.etDescription
                );

        etSkills =
                findViewById(
                        R.id.etSkills
                );

        etQualifications =
                findViewById(
                        R.id.etQualifications
                );

        etLocation =
                findViewById(
                        R.id.etLocation
                );

        etSalaryMin =
                findViewById(
                        R.id.etSalaryMin
                );

        etSalaryMax =
                findViewById(
                        R.id.etSalaryMax
                );


        spinnerJobType =
                findViewById(
                        R.id.spinnerJobType
                );

        spinnerStatus =
                findViewById(
                        R.id.spinnerStatus
                );


        btnSubmitJob =
                findViewById(
                        R.id.btnSubmitJob
                );

        progressBar =
                findViewById(
                        R.id.progressBar
                );
    }


    // =====================================================
    // SETUP API
    // =====================================================

    private void setupApi() {

        recruiterJobApi =
                ApiClient
                        .getClient(sessionManager)
                        .create(
                                RecruiterJobApi.class
                        );
    }


    // =====================================================
    // SETUP BUTTON
    // =====================================================

    private void setupButtons() {

        btnSubmitJob.setOnClickListener(
                view -> submitJobPosting()
        );
    }


    // =====================================================
    // CONFIGURE CREATE / EDIT MODE
    // =====================================================

    private void configureMode() {

        if (editMode) {

            if (jobId == -1) {

                showMessage(
                        "Invalid job."
                );

                finish();

                return;
            }


            tvPageTitle.setText(
                    "Edit Job"
            );

            tvPageSubtitle.setText(
                    "Update the information for this job listing."
            );

            btnSubmitJob.setText(
                    "Save Changes"
            );


            loadExistingJob();

        } else {

            tvPageTitle.setText(
                    "Create Job"
            );

            tvPageSubtitle.setText(
                    "Create a new job opportunity for candidates."
            );

            btnSubmitJob.setText(
                    "Create Job"
            );
        }
    }


    // =====================================================
    // LOAD EXISTING JOB
    // =====================================================

    private void loadExistingJob() {

        setLoading(true);


        recruiterJobApi
                .getMyJob(jobId)
                .enqueue(
                        new Callback<RecruiterJobResponse>() {

                            @Override
                            public void onResponse(
                                    Call<RecruiterJobResponse> call,
                                    Response<RecruiterJobResponse> response
                            ) {

                                setLoading(false);


                                if (
                                        response.isSuccessful() &&
                                                response.body() != null &&
                                                response.body().isSuccess() &&
                                                response.body().getData() != null
                                ) {

                                    populateJobForm(
                                            response
                                                    .body()
                                                    .getData()
                                    );

                                    return;
                                }


                                if (response.code() == 401) {

                                    showMessage(
                                            "Session expired. Please log in again."
                                    );

                                } else if (
                                        response.code() == 403
                                ) {

                                    showMessage(
                                            "You are not allowed to edit this job."
                                    );

                                } else if (
                                        response.code() == 404
                                ) {

                                    showMessage(
                                            "Job not found."
                                    );

                                } else {

                                    showMessage(
                                            "Unable to load job."
                                    );
                                }
                            }


                            @Override
                            public void onFailure(
                                    Call<RecruiterJobResponse> call,
                                    Throwable throwable
                            ) {

                                setLoading(false);


                                showMessage(
                                        "Network error: " +
                                                throwable.getMessage()
                                );
                            }
                        }
                );
    }


    // =====================================================
    // POPULATE FORM
    // =====================================================

    private void populateJobForm(
            RecruiterJobListResponse.RecruiterJob job
    ) {

        etJobTitle.setText(
                valueOrEmpty(
                        job.getJobTitle()
                )
        );


        etDescription.setText(
                valueOrEmpty(
                        job.getJobDescription()
                )
        );


        etSkills.setText(
                valueOrEmpty(
                        job.getRequiredSkills()
                )
        );


        etQualifications.setText(
                valueOrEmpty(
                        job.getQualifications()
                )
        );


        etLocation.setText(
                valueOrEmpty(
                        job.getLocation()
                )
        );


        // =================================================
        // SALARY
        // =================================================

        if (job.getSalaryMin() != null) {

            etSalaryMin.setText(
                    formatSalaryForInput(
                            job.getSalaryMin()
                    )
            );

        } else {

            etSalaryMin.setText("");
        }


        if (job.getSalaryMax() != null) {

            etSalaryMax.setText(
                    formatSalaryForInput(
                            job.getSalaryMax()
                    )
            );

        } else {

            etSalaryMax.setText("");
        }


        // =================================================
        // SPINNERS
        // =================================================

        selectSpinnerValue(
                spinnerJobType,
                job.getJobType()
        );


        selectSpinnerValue(
                spinnerStatus,
                job.getStatus()
        );
    }


    // =====================================================
    // SUBMIT FORM
    // =====================================================

    private void submitJobPosting() {

        String title =
                etJobTitle
                        .getText()
                        .toString()
                        .trim();


        String description =
                etDescription
                        .getText()
                        .toString()
                        .trim();


        String skills =
                etSkills
                        .getText()
                        .toString()
                        .trim();


        String qualifications =
                etQualifications
                        .getText()
                        .toString()
                        .trim();


        String location =
                etLocation
                        .getText()
                        .toString()
                        .trim();


        String minSalaryString =
                etSalaryMin
                        .getText()
                        .toString()
                        .trim();


        String maxSalaryString =
                etSalaryMax
                        .getText()
                        .toString()
                        .trim();


        String jobType =
                spinnerJobType
                        .getSelectedItem()
                        .toString()
                        .trim();


        String status =
                spinnerStatus
                        .getSelectedItem()
                        .toString()
                        .trim()
                        .toLowerCase(Locale.ROOT);


        // =================================================
        // VALIDATION
        // =================================================

        if (TextUtils.isEmpty(title)) {

            etJobTitle.setError(
                    "Job title is required"
            );

            etJobTitle.requestFocus();

            return;
        }


        if (TextUtils.isEmpty(description)) {

            etDescription.setError(
                    "Job description is required"
            );

            etDescription.requestFocus();

            return;
        }


        if (TextUtils.isEmpty(skills)) {

            etSkills.setError(
                    "Required skills are required"
            );

            etSkills.requestFocus();

            return;
        }


        if (TextUtils.isEmpty(location)) {

            etLocation.setError(
                    "Location is required"
            );

            etLocation.requestFocus();

            return;
        }


        if (
                jobType.equalsIgnoreCase(
                        "Select Job Type"
                )
        ) {

            showMessage(
                    "Please select a job type."
            );

            return;
        }


        // =================================================
        // SALARY VALIDATION
        // =================================================

        Double salaryMin = null;
        Double salaryMax = null;


        try {

            if (!minSalaryString.isEmpty()) {

                salaryMin =
                        Double.parseDouble(
                                minSalaryString
                        );
            }


            if (!maxSalaryString.isEmpty()) {

                salaryMax =
                        Double.parseDouble(
                                maxSalaryString
                        );
            }


        } catch (NumberFormatException exception) {

            showMessage(
                    "Please enter valid salary values."
            );

            return;
        }


        if (
                salaryMin != null &&
                        salaryMin < 0
        ) {

            etSalaryMin.setError(
                    "Minimum salary cannot be negative"
            );

            etSalaryMin.requestFocus();

            return;
        }


        if (
                salaryMax != null &&
                        salaryMax < 0
        ) {

            etSalaryMax.setError(
                    "Maximum salary cannot be negative"
            );

            etSalaryMax.requestFocus();

            return;
        }


        if (
                salaryMin != null &&
                        salaryMax != null &&
                        salaryMax < salaryMin
        ) {

            etSalaryMax.setError(
                    "Maximum salary cannot be lower than minimum salary"
            );

            etSalaryMax.requestFocus();

            return;
        }


        // =================================================
        // REQUEST
        // =================================================

        CreateJobRequest request =
                new CreateJobRequest(
                        title,
                        description,
                        skills,
                        qualifications,
                        location,
                        salaryMin,
                        salaryMax,
                        jobType,
                        status
                );


        // =================================================
        // CREATE OR EDIT
        // =================================================

        if (editMode) {

            updateJob(request);

        } else {

            createJob(request);
        }
    }


    // =====================================================
    // CREATE JOB
    // =====================================================

    private void createJob(
            CreateJobRequest request
    ) {

        setLoading(true);


        recruiterJobApi
                .createJob(request)
                .enqueue(
                        new Callback<CreateJobResponse>() {

                            @Override
                            public void onResponse(
                                    Call<CreateJobResponse> call,
                                    Response<CreateJobResponse> response
                            ) {

                                setLoading(false);


                                if (
                                        response.isSuccessful() &&
                                                response.body() != null &&
                                                response.body().isSuccess()
                                ) {

                                    String message =
                                            response
                                                    .body()
                                                    .getMessage();


                                    showMessage(
                                            message == null ||
                                                    message.trim().isEmpty()
                                                    ? "Job created successfully."
                                                    : message
                                    );


                                    setResult(
                                            RESULT_OK
                                    );

                                    finish();

                                    return;
                                }


                                handleSaveError(
                                        response.code(),
                                        false
                                );
                            }


                            @Override
                            public void onFailure(
                                    Call<CreateJobResponse> call,
                                    Throwable throwable
                            ) {

                                setLoading(false);


                                showMessage(
                                        "Network error: " +
                                                throwable.getMessage()
                                );
                            }
                        }
                );
    }


    // =====================================================
    // UPDATE JOB
    // =====================================================

    private void updateJob(
            CreateJobRequest request
    ) {

        setLoading(true);


        recruiterJobApi
                .updateJob(
                        jobId,
                        request
                )
                .enqueue(
                        new Callback<RecruiterJobResponse>() {

                            @Override
                            public void onResponse(
                                    Call<RecruiterJobResponse> call,
                                    Response<RecruiterJobResponse> response
                            ) {

                                setLoading(false);


                                if (
                                        response.isSuccessful() &&
                                                response.body() != null &&
                                                response.body().isSuccess()
                                ) {

                                    String message =
                                            response
                                                    .body()
                                                    .getMessage();


                                    showMessage(
                                            message == null ||
                                                    message.trim().isEmpty()
                                                    ? "Job updated successfully."
                                                    : message
                                    );


                                    setResult(
                                            RESULT_OK
                                    );

                                    finish();

                                    return;
                                }


                                handleSaveError(
                                        response.code(),
                                        true
                                );
                            }


                            @Override
                            public void onFailure(
                                    Call<RecruiterJobResponse> call,
                                    Throwable throwable
                            ) {

                                setLoading(false);


                                showMessage(
                                        "Network error: " +
                                                throwable.getMessage()
                                );
                            }
                        }
                );
    }


    // =====================================================
    // SAVE ERROR
    // =====================================================

    private void handleSaveError(
            int statusCode,
            boolean updating
    ) {

        if (statusCode == 400) {

            showMessage(
                    "Please check the job information."
            );

        } else if (
                statusCode == 401
        ) {

            showMessage(
                    "Session expired. Please log in again."
            );

        } else if (
                statusCode == 403
        ) {

            showMessage(
                    updating
                            ? "You are not allowed to edit this job."
                            : "Please complete your recruiter profile before posting a job."
            );

        } else if (
                statusCode == 404
        ) {

            showMessage(
                    updating
                            ? "Job not found."
                            : "Recruiter profile not found."
            );

        } else {

            showMessage(
                    updating
                            ? "Unable to update job. Please try again."
                            : "Unable to create job. Please try again."
            );
        }
    }


    // =====================================================
    // SPINNER VALUE
    // =====================================================

    private void selectSpinnerValue(
            Spinner spinner,
            String value
    ) {

        if (
                spinner == null ||
                        value == null
        ) {

            return;
        }


        for (
                int i = 0;
                i < spinner.getCount();
                i++
        ) {

            String item =
                    spinner
                            .getItemAtPosition(i)
                            .toString();


            if (
                    item.equalsIgnoreCase(
                            value
                    )
            ) {

                spinner.setSelection(i);

                return;
            }
        }
    }


    // =====================================================
    // FORMAT SALARY FOR EDITTEXT
    // =====================================================

    private String formatSalaryForInput(
            Double value
    ) {

        if (value == null) {

            return "";
        }


        if (
                value == Math.floor(value)
        ) {

            return String.format(
                    Locale.US,
                    "%.0f",
                    value
            );
        }


        return String.valueOf(
                value
        );
    }


    // =====================================================
    // LOADING
    // =====================================================

    private void setLoading(
            boolean loading
    ) {

        progressBar.setVisibility(
                loading
                        ? View.VISIBLE
                        : View.GONE
        );


        btnSubmitJob.setEnabled(
                !loading
        );


        etJobTitle.setEnabled(
                !loading
        );

        etDescription.setEnabled(
                !loading
        );

        etSkills.setEnabled(
                !loading
        );

        etQualifications.setEnabled(
                !loading
        );

        etLocation.setEnabled(
                !loading
        );

        etSalaryMin.setEnabled(
                !loading
        );

        etSalaryMax.setEnabled(
                !loading
        );

        spinnerJobType.setEnabled(
                !loading
        );

        spinnerStatus.setEnabled(
                !loading
        );
    }


    // =====================================================
    // VALUE HELPER
    // =====================================================

    private String valueOrEmpty(
            String value
    ) {

        return value == null
                ? ""
                : value;
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
                Toast.LENGTH_SHORT
        ).show();
    }
}