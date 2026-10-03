package com.example.jobmatch_frontend.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.database.Cursor;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Button;
import android.widget.Toast;
import android.graphics.Color;
import android.graphics.Typeface;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.content.FileProvider;

import com.example.jobmatch_frontend.api.DocumentApi;
import com.example.jobmatch_frontend.models.DocumentListResponse;
import java.util.List;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.jobmatch_frontend.R;
import com.example.jobmatch_frontend.api.ApiClient;
import com.example.jobmatch_frontend.api.ApiService;
import com.example.jobmatch_frontend.models.OcrUploadResponse;
import com.example.jobmatch_frontend.session.SessionManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DocumentUploadActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private Button btnNextStep;

    private SessionManager sessionManager;

    private Uri selectedFileUri;

    private Uri cameraImageUri;

    private String documentType = null;

    private String source = "onboarding";

    private DocumentApi documentApi;

    private LinearLayout documentListContainer;
    private TextView tvNoDocuments;


    // File picker
    private final ActivityResultLauncher<String[]> filePicker =
            registerForActivityResult(

                    new ActivityResultContracts.OpenDocument(),

                    uri -> {

                        if (uri != null) {

                            selectedFileUri = uri;

                            // Keep permission to read selected document
                            try {

                                getContentResolver()
                                        .takePersistableUriPermission(
                                                uri,
                                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                                        );

                            } catch (SecurityException e) {

                                android.util.Log.w(
                                        "OCR_UPLOAD",
                                        "Unable to persist URI permission",
                                        e
                                );
                            }


                            String fileName =
                                    getFileName(uri);


                            Toast.makeText(
                                    DocumentUploadActivity.this,
                                    "Selected: "
                                            + (
                                            fileName != null
                                                    ? fileName
                                                    : "document"
                                    ),
                                    Toast.LENGTH_SHORT
                            ).show();


                            android.util.Log.d(
                                    "OCR_UPLOAD",
                                    "Selected URI = " + uri
                            );


                            android.util.Log.d(
                                    "OCR_UPLOAD",
                                    "Selected file = " + fileName
                            );

                            btnNextStep.setEnabled(true);
                        }
                    }
            );

    private final ActivityResultLauncher<Uri> cameraLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.TakePicture(),

                    success -> {

                        android.util.Log.d(
                                "CAMERA_SCAN",
                                "Camera result = " + success
                        );


                        if (!success || cameraImageUri == null) {

                            Toast.makeText(
                                    DocumentUploadActivity.this,
                                    "Camera capture cancelled.",
                                    Toast.LENGTH_SHORT
                            ).show();

                            return;
                        }


                        try {

                            long imageSize = 0;


                            try (
                                    android.content.res.AssetFileDescriptor afd =
                                            getContentResolver()
                                                    .openAssetFileDescriptor(
                                                            cameraImageUri,
                                                            "r"
                                                    )
                            ) {

                                if (afd != null) {

                                    imageSize =
                                            afd.getLength();
                                }
                            }


                            android.util.Log.d(
                                    "CAMERA_SCAN",
                                    "Captured image size = "
                                            + imageSize
                                            + " bytes"
                            );


                            if (imageSize <= 0) {

                                Toast.makeText(
                                        DocumentUploadActivity.this,
                                        "Camera returned an empty image. Please try again.",
                                        Toast.LENGTH_LONG
                                ).show();

                                return;
                            }


                            selectedFileUri =
                                    cameraImageUri;


                            Toast.makeText(
                                    DocumentUploadActivity.this,
                                    "Photo captured successfully.",
                                    Toast.LENGTH_SHORT
                            ).show();


                            // Keep the captured image selected.
                            // The user explicitly starts the upload with the UPLOAD button.
                            btnNextStep.setEnabled(true);


                        } catch (Exception e) {

                            android.util.Log.e(
                                    "CAMERA_SCAN",
                                    "Unable to read captured image",
                                    e
                            );

                            Toast.makeText(
                                    DocumentUploadActivity.this,
                                    "Unable to read captured image.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_document_upload);

        String requestedType =
                getIntent().getStringExtra(
                        "document_type"
                );

        if (requestedType != null) {

            requestedType =
                    requestedType
                            .trim()
                            .toLowerCase();

            if (
                    "resume".equals(requestedType)
                            ||
                            "certificate".equals(requestedType)
            ) {

                documentType =
                        requestedType;
            }
        }

        String requestedSource =
                getIntent().getStringExtra(
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

        sessionManager = new SessionManager(this);

        documentApi =
                ApiClient
                        .getClient(sessionManager)
                        .create(DocumentApi.class);

        btnBack = findViewById(R.id.btnBack);
        btnNextStep = findViewById(R.id.btnNextStep);
        btnNextStep.setEnabled(false);

        documentListContainer =
                findViewById(R.id.documentListContainer);

        tvNoDocuments =
                findViewById(R.id.tvNoDocuments);

        btnBack.setOnClickListener(v -> finish());

        // SELECT FILES
        findViewById(
                R.id.btnChooseFile
        ).setOnClickListener(v -> {

            String[] mimeTypes = {

                    "image/jpeg",

                    "image/png",

                    "application/pdf"
            };


            filePicker.launch(
                    mimeTypes
            );
        });

        // CAMERA
        // CAMERA
        findViewById(
                R.id.btnOpenCamera
        ).setOnClickListener(v -> {

            openCamera();

        });

        // UPLOAD

        btnNextStep.setOnClickListener(v -> {

            if (selectedFileUri == null) {

                Toast.makeText(
                        this,
                        "Please select a document first",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            uploadDocument();
        });
    }

    private void openCamera() {

        try {

            File cameraDir =
                    new File(
                            getCacheDir(),
                            "camera"
                    );

            if (!cameraDir.exists()) {

                boolean created =
                        cameraDir.mkdirs();

                android.util.Log.d(
                        "CAMERA_SCAN",
                        "Camera directory created = " + created
                );
            }


            File imageFile =
                    File.createTempFile(
                            "camera_scan_",
                            ".jpg",
                            cameraDir
                    );


            cameraImageUri =
                    FileProvider.getUriForFile(
                            this,
                            getPackageName() + ".fileprovider",
                            imageFile
                    );


            android.util.Log.d(
                    "CAMERA_SCAN",
                    "Camera URI = " + cameraImageUri
            );

            android.util.Log.d(
                    "CAMERA_SCAN",
                    "Camera file = " + imageFile.getAbsolutePath()
            );


            cameraLauncher.launch(
                    cameraImageUri
            );

        } catch (Exception e) {

            android.util.Log.e(
                    "CAMERA_SCAN",
                    "Unable to open camera",
                    e
            );

            Toast.makeText(
                    this,
                    "Unable to open camera: " + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void uploadDocument() {

        // Get logged-in user ID from existing SessionManager
        int userId = sessionManager.getUserId();

        android.util.Log.d(
                "OCR_UPLOAD",
                "Session user ID = " + userId
        );

        // Check whether user is logged in
        if (userId == -1) {

            Toast.makeText(
                    this,
                    "User session not found. Please login again.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        // Check whether a file was selected
        if (selectedFileUri == null) {

            Toast.makeText(
                    this,
                    "Please select a document first.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        btnNextStep.setEnabled(false);
        btnNextStep.setText("UPLOADING...");

        try {

            // ==========================================
            // 1. COPY SELECTED FILE TO APP CACHE
            // ==========================================

            File file =
                    createFileFromUri(
                            selectedFileUri
                    );


            if (
                    file == null
                            ||
                            !file.exists()
                            ||
                            file.length() <= 0
            ) {

                Toast.makeText(
                        this,
                        "The selected image is empty or cannot be read.",
                        Toast.LENGTH_LONG
                ).show();
                resetUploadButton();
                return;
            }

            android.util.Log.d(
                    "OCR_UPLOAD",
                    "File name = " + file.getName()
            );

            android.util.Log.d(
                    "OCR_UPLOAD",
                    "File size = " + file.length()
            );


            // ==========================================
            // 2. CREATE user_id REQUEST BODY
            // ==========================================

            RequestBody userIdBody =
                    RequestBody.create(
                            MediaType.parse("text/plain"),
                            String.valueOf(userId)
                    );


            // ==========================================
            // 3. CREATE document_type REQUEST BODY
            // ==========================================

            RequestBody documentTypeBody =
                    RequestBody.create(
                            MediaType.parse("text/plain"),
                            documentType
                    );

            // ==========================================
            // 4. DETERMINE FILE MIME TYPE
            // ==========================================
            String mimeType =
                    getContentResolver()
                            .getType(
                                    selectedFileUri
                            );


            if (mimeType == null) {

                String fileName =
                        file.getName()
                                .toLowerCase();


                if (
                        fileName.endsWith(".png")
                ) {

                    mimeType =
                            "image/png";

                }

                else if (
                        fileName.endsWith(".jpg")
                                ||
                                fileName.endsWith(".jpeg")
                ) {

                    mimeType =
                            "image/jpeg";

                }

                else if (
                        fileName.endsWith(".pdf")
                ) {

                    mimeType =
                            "application/pdf";

                }

                else {

                    Toast.makeText(
                            this,
                            "Unsupported document type.",
                            Toast.LENGTH_LONG
                    ).show();
                    resetUploadButton();
                    return;
                }
            }

            boolean supportedType =

                    "image/jpeg".equals(mimeType)

                            ||

                            "image/png".equals(mimeType)

                            ||

                            "application/pdf".equals(mimeType);


            if (!supportedType) {

                Toast.makeText(
                        this,
                        "Only JPG, JPEG, PNG and PDF files are allowed.",
                        Toast.LENGTH_LONG
                ).show();
                resetUploadButton();
                return;
            }

            // ==========================================
            // 5. CREATE FILE REQUEST BODY
            // ==========================================

            RequestBody fileBody =
                    RequestBody.create(
                            MediaType.parse(mimeType),
                            file
                    );


            // ==========================================
            // 6. CREATE MULTIPART FILE
            // ==========================================

            MultipartBody.Part documentPart =
                    MultipartBody.Part.createFormData(
                            "document",
                            file.getName(),
                            fileBody
                    );


            // ==========================================
            // 7. CREATE AUTHENTICATED API SERVICE
            // ==========================================

            ApiService apiService =
                    ApiClient
                            .getClient(sessionManager)
                            .create(ApiService.class);


            // ==========================================
            // 8. SHOW UPLOAD MESSAGE
            // ==========================================

            Toast.makeText(
                    this,
                    "Uploading document and processing OCR...",
                    Toast.LENGTH_LONG
            ).show();


            android.util.Log.d(
                    "OCR_UPLOAD",
                    "Sending user_id = " + userId
            );

            android.util.Log.d(
                    "OCR_UPLOAD",
                    "Sending document_type = " + documentType
            );


            // ==========================================
            // 9. SEND MULTIPART REQUEST
            // ==========================================

            apiService.uploadDocument(
                    userIdBody,
                    documentTypeBody,
                    documentPart
            ).enqueue(
                    new Callback<OcrUploadResponse>() {

                        @Override
                        public void onResponse(
                                Call<OcrUploadResponse> call,
                                Response<OcrUploadResponse> response
                        ) {

                            android.util.Log.d(
                                    "OCR_UPLOAD",
                                    "HTTP status = "
                                            + response.code()
                            );


                            // ==================================
                            // SUCCESS
                            // ==================================

                            if (response.isSuccessful()
                                    && response.body() != null
                                    && response.body().isSuccess()) {

                                OcrUploadResponse.Data data =
                                        response.body().getData();


                                if (data == null) {
                                    resetUploadButton();

                                    new androidx.appcompat.app.AlertDialog.Builder(
                                            DocumentUploadActivity.this
                                    )
                                            .setTitle("Upload Error")
                                            .setMessage(
                                                    "Server returned a successful response, "
                                                            + "but no OCR data was received."
                                            )
                                            .setPositiveButton(
                                                    "OK",
                                                    null
                                            )
                                            .show();

                                    return;
                                }


                                android.util.Log.d(
                                        "OCR_UPLOAD",
                                        "Document ID = "
                                                + data.getDocumentId()
                                );

                                android.util.Log.d(
                                        "OCR_UPLOAD",
                                        "OCR ID = "
                                                + data.getOcrId()
                                );

                                android.util.Log.d(
                                        "OCR_UPLOAD",
                                        "Extracted text = "
                                                + data.getExtractedText()
                                );

                                android.util.Log.d(
                                        "OCR_UPLOAD",
                                        "Confidence = "
                                                + data.getConfidenceScore()
                                );


                                // ==================================
                                // OPEN OCR REVIEW SCREEN
                                // ==================================

                                Intent intent =
                                        new Intent(
                                                DocumentUploadActivity.this,
                                                OcrReviewActivity.class
                                        );


                                intent.putExtra(
                                        "document_id",
                                        data.getDocumentId()
                                );

                                intent.putExtra(
                                        "ocr_id",
                                        data.getOcrId()
                                );

                                intent.putExtra(
                                        "file_name",
                                        data.getFileName()
                                );

                                intent.putExtra(
                                        "extracted_text",
                                        data.getExtractedText()
                                );

                                intent.putExtra(
                                        "confidence_score",
                                        data.getConfidenceScore()
                                );

                                intent.putExtra(
                                        "source",
                                        source
                                );
                                selectedFileUri = null;
                                resetUploadButton();

                                // Refresh the document list immediately.
                                // onResume() refreshes it again when returning from OCR Review.
                                loadUploadedDocuments();

                                startActivity(intent);

                            }


                            // ==================================
                            // SERVER ERROR
                            // ==================================

                            else {

                                String errorMessage =
                                        "Unknown server error.";

                                try {

                                    if (response.errorBody()
                                            != null) {

                                        errorMessage =
                                                response
                                                        .errorBody()
                                                        .string();
                                    }

                                } catch (Exception e) {

                                    errorMessage =
                                            e.getMessage();

                                    if (errorMessage == null) {

                                        errorMessage =
                                                "Unable to read server error.";
                                    }
                                }


                                android.util.Log.e(
                                        "OCR_UPLOAD_ERROR",
                                        "HTTP "
                                                + response.code()
                                                + ": "
                                                + errorMessage
                                );


                                resetUploadButton();

                                new androidx.appcompat.app.AlertDialog.Builder(
                                        DocumentUploadActivity.this
                                )
                                        .setTitle("Upload Error")
                                        .setMessage(
                                                "HTTP "
                                                        + response.code()
                                                        + "\n\n"
                                                        + errorMessage
                                        )
                                        .setPositiveButton(
                                                "OK",
                                                null
                                        )
                                        .show();
                            }
                        }


                        // ======================================
                        // NETWORK / RETROFIT FAILURE
                        // ======================================

                        @Override
                        public void onFailure(
                                Call<OcrUploadResponse> call,
                                Throwable t
                        ) {

                            android.util.Log.e(
                                    "OCR_UPLOAD_ERROR",
                                    "Network/API failure",
                                    t
                            );


                            String errorMessage =
                                    t.getMessage();

                            if (errorMessage == null) {

                                errorMessage =
                                        "Unable to connect to the server.";
                            }


                            resetUploadButton();

                            new androidx.appcompat.app.AlertDialog.Builder(
                                    DocumentUploadActivity.this
                            )
                                    .setTitle("Connection Error")
                                    .setMessage(
                                            errorMessage
                                    )
                                    .setPositiveButton(
                                            "OK",
                                            null
                                    )
                                    .show();
                        }
                    }
            );


        } catch (Exception e) {

            // ==========================================
            // LOCAL/PREPARATION ERROR
            // ==========================================

            e.printStackTrace();


            android.util.Log.e(
                    "OCR_UPLOAD_ERROR",
                    "Error preparing upload",
                    e
            );


            String errorMessage =
                    e.getMessage();

            if (errorMessage == null) {

                errorMessage =
                        "An unexpected error occurred.";
            }


            resetUploadButton();

            new androidx.appcompat.app.AlertDialog.Builder(
                    DocumentUploadActivity.this
            )
                    .setTitle("Upload Error")
                    .setMessage(
                            "An error occurred while preparing "
                                    + "the upload.\n\n"
                                    + errorMessage
                    )
                    .setPositiveButton(
                            "OK",
                            null
                    )
                    .show();
        }
    }

    private void resetUploadButton() {
        if (btnNextStep != null) {
            btnNextStep.setText("UPLOAD");
            btnNextStep.setEnabled(selectedFileUri != null);
        }
    }

    @Override
    protected void onResume() {

        super.onResume();

        if (documentApi != null) {

            loadUploadedDocuments();
        }
    }

    private void loadUploadedDocuments() {

        if (documentApi == null) {
            return;
        }

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

                                    showUploadedDocuments(
                                            response.body().getData()
                                    );

                                } else {

                                    tvNoDocuments.setVisibility(
                                            View.VISIBLE
                                    );

                                    tvNoDocuments.setText(
                                            "Unable to load documents."
                                    );

                                    documentListContainer.removeAllViews();
                                }
                            }

                            @Override
                            public void onFailure(
                                    Call<DocumentListResponse> call,
                                    Throwable t
                            ) {

                                android.util.Log.e(
                                        "DOCUMENT_LIST",
                                        "Unable to load documents",
                                        t
                                );

                                tvNoDocuments.setVisibility(
                                        View.VISIBLE
                                );

                                tvNoDocuments.setText(
                                        "Unable to connect to document server."
                                );

                                documentListContainer.removeAllViews();
                            }
                        }
                );
    }

    private void showUploadedDocuments(
            List<DocumentListResponse.DocumentItem> documents
    ) {

        documentListContainer.removeAllViews();

        if (documents == null || documents.isEmpty()) {

            tvNoDocuments.setVisibility(
                    View.VISIBLE
            );

            tvNoDocuments.setText(
                    "No documents uploaded yet."
            );

            return;
        }


        int displayedDocuments = 0;


        for (
                DocumentListResponse.DocumentItem document
                : documents
        ) {

            if (document == null) {
                continue;
            }


            /*
             * When this screen is being used for
             * certificates/qualifications, don't display
             * the user's resume here.
             */
            if (
                    "certificate".equalsIgnoreCase(
                            documentType
                    )
                            &&
                            "resume".equalsIgnoreCase(
                                    document.getDocumentType()
                            )
            ) {

                continue;
            }


            addDocumentRow(
                    document
            );

            displayedDocuments++;
        }


        if (displayedDocuments == 0) {

            tvNoDocuments.setVisibility(
                    View.VISIBLE
            );

            if (
                    "resume".equalsIgnoreCase(
                            documentType
                    )
            ) {

                tvNoDocuments.setText(
                        "No resume uploaded yet."
                );

            } else {

                tvNoDocuments.setText(
                        "No certificates or qualifications uploaded yet."
                );
            }

        } else {

            tvNoDocuments.setVisibility(
                    View.GONE
            );
        }
    }

    private void addDocumentRow(
            DocumentListResponse.DocumentItem document
    ) {

        float density =
                getResources()
                        .getDisplayMetrics()
                        .density;


        int padding =
                (int) (14 * density);

        int marginBottom =
                (int) (10 * density);


        LinearLayout card =
                new LinearLayout(this);

        card.setOrientation(
                LinearLayout.VERTICAL
        );

        card.setPadding(
                padding,
                padding,
                padding,
                padding
        );


        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.setMargins(
                0,
                0,
                0,
                marginBottom
        );

        card.setLayoutParams(
                cardParams
        );

        card.setBackgroundColor(
                Color.rgb(
                        238,
                        249,
                        251
                )
        );


        // FILE NAME
        TextView tvFileName =
                new TextView(this);

        String fileName =
                document.getFileName();

        if (
                fileName == null
                        ||
                        fileName.trim().isEmpty()
        ) {

            fileName =
                    "Uploaded document";
        }

        tvFileName.setText(
                fileName
        );

        tvFileName.setTextColor(
                Color.rgb(
                        30,
                        30,
                        30
                )
        );

        tvFileName.setTextSize(
                15
        );

        tvFileName.setTypeface(
                null,
                Typeface.BOLD
        );


        // TYPE
        TextView tvType =
                new TextView(this);

        String type =
                document.getDocumentType();

        if (
                type == null
                        ||
                        type.trim().isEmpty()
        ) {

            type =
                    "Document";
        } else {

            type =
                    capitalize(type);
        }


        // STATUS
        String status =
                document.getUploadStatus();

        if (
                status == null
                        ||
                        status.trim().isEmpty()
        ) {

            status =
                    "Uploaded";
        } else {

            status =
                    capitalize(status);
        }


        tvType.setText(
                type
                        + " • "
                        + status
        );

        tvType.setTextColor(
                Color.rgb(
                        100,
                        100,
                        100
                )
        );

        tvType.setTextSize(
                13
        );


        card.addView(
                tvFileName
        );

        card.addView(
                tvType
        );


        Button btnDelete = new Button(this);
        btnDelete.setText("Delete document");
        btnDelete.setContentDescription("Delete " + fileName);
        btnDelete.setOnClickListener(v -> confirmDeleteDocument(document));
        card.addView(btnDelete);

        documentListContainer.addView(
                card
        );
    }

    private void confirmDeleteDocument(DocumentListResponse.DocumentItem document) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete document?")
                .setMessage("This will permanently delete the uploaded document and its OCR result.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> {
                    documentApi.deleteDocument(document.getDocumentId()).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(DocumentUploadActivity.this, "Document deleted", Toast.LENGTH_SHORT).show();
                                loadUploadedDocuments();
                            } else {
                                Toast.makeText(DocumentUploadActivity.this,
                                        "Delete failed (HTTP " + response.code() + ")", Toast.LENGTH_LONG).show();
                            }
                        }
                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(DocumentUploadActivity.this, "Connection error. Please try again.", Toast.LENGTH_LONG).show();
                        }
                    });
                }).show();
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

        value =
                value.trim();

        return value
                .substring(0, 1)
                .toUpperCase()
                +
                value.substring(1)
                        .toLowerCase();
    }

    private File createFileFromUri(Uri uri) {

        try {

            String fileName =
                    getFileName(uri);


            if (
                    fileName == null
                            ||
                            fileName.trim().isEmpty()
            ) {

                fileName =
                        "document_"
                                + System.currentTimeMillis()
                                + ".jpg";
            }


            /*
             * Don't overwrite the same camera source file.
             *
             * Camera URI may already point into getCacheDir().
             * Therefore create a separate upload copy.
             */
            File uploadDir =
                    new File(
                            getCacheDir(),
                            "upload"
                    );


            if (!uploadDir.exists()) {

                uploadDir.mkdirs();
            }


            File file =
                    new File(
                            uploadDir,
                            "upload_"
                                    + System.currentTimeMillis()
                                    + "_"
                                    + fileName
                    );


            InputStream inputStream =
                    getContentResolver()
                            .openInputStream(uri);


            if (inputStream == null) {

                android.util.Log.e(
                        "OCR_UPLOAD",
                        "InputStream is null"
                );

                return null;
            }


            FileOutputStream outputStream =
                    new FileOutputStream(file);


            byte[] buffer =
                    new byte[8192];

            int length;


            while (
                    (length = inputStream.read(buffer))
                            != -1
            ) {

                outputStream.write(
                        buffer,
                        0,
                        length
                );
            }


            outputStream.flush();
            outputStream.close();
            inputStream.close();


            android.util.Log.d(
                    "OCR_UPLOAD",
                    "Copied upload file = "
                            + file.getAbsolutePath()
            );


            android.util.Log.d(
                    "OCR_UPLOAD",
                    "Copied upload size = "
                            + file.length()
                            + " bytes"
            );


            if (file.length() <= 0) {

                android.util.Log.e(
                        "OCR_UPLOAD",
                        "Copied file is empty"
                );

                file.delete();

                return null;
            }


            return file;


        } catch (Exception e) {

            android.util.Log.e(
                    "OCR_UPLOAD",
                    "createFileFromUri failed",
                    e
            );

            return null;
        }
    }

    private String getFileName(Uri uri) {

        String result = null;

        if ("content".equals(uri.getScheme())) {

            try (Cursor cursor =
                         getContentResolver()
                                 .query(
                                         uri,
                                         null,
                                         null,
                                         null,
                                         null
                                 )) {

                if (cursor != null
                        && cursor.moveToFirst()) {

                    int nameIndex =
                            cursor.getColumnIndex(
                                    OpenableColumns.DISPLAY_NAME
                            );

                    if (nameIndex >= 0) {

                        result =
                                cursor.getString(nameIndex);
                    }
                }
            }
        }

        if (result == null) {

            result = uri.getPath();

            if (result != null) {

                int cut =
                        result.lastIndexOf('/');

                if (cut != -1) {

                    result =
                            result.substring(cut + 1);
                }
            }
        }

        return result;
    }
}