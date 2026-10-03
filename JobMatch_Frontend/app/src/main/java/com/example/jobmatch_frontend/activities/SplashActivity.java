package com.example.jobmatch_frontend.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.jobmatch_frontend.session.SessionManager;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SessionManager sessionManager = new SessionManager(this);

        if (sessionManager.isLoggedIn()) {
            String role = sessionManager.getRole();
            if ("Employer".equalsIgnoreCase(role) || "Recruiter".equalsIgnoreCase(role)) {
                startActivity(new Intent(this, EmployerDashboardActivity.class));
            } else {
                startActivity(new Intent(this, JobseekerDashboardActivity.class));
            }
        } else {
            startActivity(new Intent(this, LoginActivity.class));
        }
        finish();
    }
}