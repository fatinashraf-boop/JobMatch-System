package com.example.jobmatch_frontend;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.example.jobmatch_frontend.activities.EmployerDashboardActivity;
import com.example.jobmatch_frontend.activities.JobseekerDashboardActivity;
import com.example.jobmatch_frontend.activities.LoginActivity;
import com.example.jobmatch_frontend.activities.RoleSelectionActivity;
import com.example.jobmatch_frontend.session.SessionManager;

public class MainActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private Button btnLogin;
    private Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);

        // Check if user is already logged in
        if (sessionManager.isLoggedIn()) {
            String role = sessionManager.getRole();

            if (role != null && (role.equalsIgnoreCase("recruiter") || role.equalsIgnoreCase("employer"))) {
                Intent intent = new Intent(this, EmployerDashboardActivity.class);
                intent.putExtra("user_id", sessionManager.getUserId());
                startActivity(intent);
                finish();
                return;
            } else if (role != null && role.equalsIgnoreCase("jobseeker")) {
                Intent intent = new Intent(this, JobseekerDashboardActivity.class);
                intent.putExtra("user_id", sessionManager.getUserId());
                startActivity(intent);
                finish();
                return;
            }
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RoleSelectionActivity.class);
            startActivity(intent);
        });
    }
}