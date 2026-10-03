package com.example.jobmatch_frontend.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.jobmatch_frontend.api.*;
import com.example.jobmatch_frontend.models.*;
import com.example.jobmatch_frontend.session.SessionManager;
import java.util.*;
import retrofit2.*;

public class RecruiterSearchActivity extends AppCompatActivity {
    private final List<RecruiterJobListResponse.RecruiterJob> jobs = new ArrayList<>();
    private final List<RecruiterApplicantResponse.Applicant> applicants = new ArrayList<>();
    private LinearLayout results;
    private EditText search;
    private TextView status;
    private boolean searchJobs = true;
    private RecruiterJobApi jobApi;
    private RecruiterApplicantApi applicantApi;
    private int requestVersion = 0;
    private ProgressBar progressBar;
    private boolean jobsLoaded, applicantsLoaded;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        SessionManager session = new SessionManager(this);
        if (!session.isLoggedIn() || !"recruiter".equalsIgnoreCase(session.getRole())) {
            Toast.makeText(this, "Recruiter login required", Toast.LENGTH_SHORT).show();
            finish(); return;
        }
        jobApi = ApiClient.getClient(session).create(RecruiterJobApi.class);
        applicantApi = ApiClient.getClient(session).create(RecruiterApplicantApi.class);
        setContentView(com.example.jobmatch_frontend.R.layout.activity_recruiter_search);
        search = findViewById(com.example.jobmatch_frontend.R.id.etSearch);
        status = findViewById(com.example.jobmatch_frontend.R.id.tvSearchStatus);
        results = findViewById(com.example.jobmatch_frontend.R.id.searchResults);
        progressBar = findViewById(com.example.jobmatch_frontend.R.id.progressBar);
        findViewById(com.example.jobmatch_frontend.R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(com.example.jobmatch_frontend.R.id.btnSearch).setOnClickListener(v -> render());
        findViewById(com.example.jobmatch_frontend.R.id.navHome).setOnClickListener(v -> finish());
        findViewById(com.example.jobmatch_frontend.R.id.navJobs).setOnClickListener(v -> startActivity(new Intent(this, MyJobListingsActivity.class)));
        findViewById(com.example.jobmatch_frontend.R.id.navApplicants).setOnClickListener(v -> startActivity(new Intent(this, RecruiterApplicantsActivity.class)));
        findViewById(com.example.jobmatch_frontend.R.id.navAccount).setOnClickListener(v -> startActivity(new Intent(this, RecruiterProfileActivity.class)));
        RadioGroup tabs = findViewById(com.example.jobmatch_frontend.R.id.rgSearchType);
        RadioButton jobTab = findViewById(com.example.jobmatch_frontend.R.id.rbJobs);
        search.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s,int start,int count,int after){}
            public void onTextChanged(CharSequence s,int start,int before,int count){render();}
            public void afterTextChanged(Editable e){}
        });
        tabs.setOnCheckedChangeListener((group,id)->{
            searchJobs = id == jobTab.getId();
            search.setHint(searchJobs ? "Search job title..." : "Search candidate name or skills...");
            ((Button)findViewById(com.example.jobmatch_frontend.R.id.btnSearch)).setText(searchJobs ? "Search Jobs" : "Search Candidates");
            render();
        });
        jobTab.setChecked(true);
    }
    @Override protected void onResume(){super.onResume(); loadData();}
    private void loadData() {
        final int version = ++requestVersion;
        jobsLoaded = false; applicantsLoaded = false;
        progressBar.setVisibility(View.VISIBLE);
        status.setText("Loading...");
        jobApi.getMyJobs().enqueue(new Callback<RecruiterJobListResponse>() {
            public void onResponse(Call<RecruiterJobListResponse> c,Response<RecruiterJobListResponse> r){
                if(version!=requestVersion)return;
                jobsLoaded = true;
                jobs.clear();
                if(r.isSuccessful() && r.body()!=null && r.body().isSuccess() && r.body().getJobs()!=null)
                    jobs.addAll(r.body().getJobs());
                else Toast.makeText(RecruiterSearchActivity.this,"Could not load jobs",Toast.LENGTH_SHORT).show();
                render();
            }
            public void onFailure(Call<RecruiterJobListResponse> c,Throwable t){
                if(version!=requestVersion)return;
                jobsLoaded = true;
                progressBar.setVisibility(View.GONE);
                status.setText("Could not load jobs. Check your connection.");
            }
        });
        applicantApi.getAllApplicants().enqueue(new Callback<RecruiterApplicantResponse>() {
            public void onResponse(Call<RecruiterApplicantResponse> c,Response<RecruiterApplicantResponse> r){
                if(version!=requestVersion)return;
                applicantsLoaded = true;
                applicants.clear();
                if(r.isSuccessful() && r.body()!=null && r.body().isSuccess() && r.body().getApplicants()!=null)
                    applicants.addAll(r.body().getApplicants());
                else Toast.makeText(RecruiterSearchActivity.this,"Could not load candidates",Toast.LENGTH_SHORT).show();
                render();
            }
            public void onFailure(Call<RecruiterApplicantResponse> c,Throwable t){
                if(version!=requestVersion)return;
                applicantsLoaded = true;
                progressBar.setVisibility(View.GONE);
                status.setText("Could not load candidates. Check your connection.");
            }
        });
    }
    private boolean contains(String value,String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }
    private void render(){
        if(results==null)return;
        results.removeAllViews();
        progressBar.setVisibility((searchJobs ? jobsLoaded : applicantsLoaded) ? View.GONE : View.VISIBLE);
        if (!(searchJobs ? jobsLoaded : applicantsLoaded)) { status.setText("Loading..."); return; }
        String q=search.getText().toString().trim().toLowerCase(Locale.ROOT);
        int count=0;
        if(searchJobs){
            for(RecruiterJobListResponse.RecruiterJob job:jobs){
                if(!contains(job.getJobTitle(),q))continue;
                count++;
                addResult(job.getJobTitle(),job.getStatus(),()->{
                    Intent i=new Intent(this,RecruiterAiMatchesActivity.class);
                    i.putExtra("job_id",job.getJobId()); startActivity(i);
                });
            }
        } else {
            for(RecruiterApplicantResponse.Applicant a:applicants){
                if(!contains(a.getCandidateName(),q) && !contains(a.getSkills(),q))continue;
                count++;
                addResult(a.getCandidateName(),a.getSkills(),()->{
                    Intent i=new Intent(this,CandidateDetailActivity.class);
                    i.putExtra("application_id",a.getApplicationId());
                    i.putExtra("job_id",a.getJobId());
                    i.putExtra("profile_id",a.getProfileId()); startActivity(i);
                });
            }
        }
        status.setText(count+" "+(searchJobs?"jobs":"applicants")+" found");
    }
    private void addResult(String title,String detail,Runnable click){
        LinearLayout item=new LinearLayout(this); item.setOrientation(LinearLayout.VERTICAL);
        item.setPadding(dp(16),dp(16),dp(16),dp(16));
        item.setBackgroundResource(com.example.jobmatch_frontend.R.drawable.bg_dashboard_search);
        TextView name=new TextView(this); name.setText(title==null?"Untitled":title);
        name.setTextSize(17); name.setTextColor(0xFF202124); item.addView(name);
        TextView sub=new TextView(this); sub.setText(detail==null?"":detail); item.addView(sub);
        item.setOnClickListener(v->click.run());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1,-2);
        params.bottomMargin = dp(10);
        results.addView(item,params);
        View line=new View(this);line.setBackgroundColor(0xFFE6E6E6);
        results.addView(line,new LinearLayout.LayoutParams(-1,dp(1)));
    }
    private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+0.5f);}
}
