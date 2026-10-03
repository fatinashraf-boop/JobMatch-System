package com.example.jobmatch_frontend.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.jobmatch_frontend.R;

public class RoleSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_selection);

        CardView cardJobseeker = findViewById(R.id.cardJobseeker);
        CardView cardRecruiter = findViewById(R.id.cardRecruiter);

        cardJobseeker.setOnClickListener(v -> {
            navigateToRegister("jobseeker");
        });

        cardRecruiter.setOnClickListener(v -> {
            navigateToRegister("recruiter");
        });
    }

    private void navigateToRegister(String role) {
        Intent intent = new Intent(RoleSelectionActivity.this, RegisterActivity.class);
        intent.putExtra("selected_role", role);
        startActivity(intent);
    }
}
