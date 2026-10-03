package com.example.jobmatch_frontend.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobmatch_frontend.R;
import com.example.jobmatch_frontend.models.Job;

import java.util.List;

public class JobAdapter extends RecyclerView.Adapter<JobAdapter.JobViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Job job);
    }

    private final Context context;
    private final List<Job> jobList;
    private final OnItemClickListener listener;

    public JobAdapter(Context context, List<Job> jobList, OnItemClickListener listener) {
        this.context = context;
        this.jobList = jobList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_job, parent, false);
        return new JobViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JobViewHolder holder, int position) {
        Job job = jobList.get(position);
        holder.bind(job, listener);
    }

    @Override
    public int getItemCount() {
        return jobList != null ? jobList.size() : 0;
    }

    public static class JobViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle, tvCompany, tvLocation, tvSalary;

        public JobViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvJobTitle);
            tvCompany = itemView.findViewById(R.id.tvCompanyName);
            tvLocation = itemView.findViewById(R.id.tvJobLocation);
            tvSalary = itemView.findViewById(R.id.tvJobSalary);
        }

        public void bind(final Job job, final OnItemClickListener listener) {
            if (tvTitle != null) tvTitle.setText(job.getJobTitle());
            if (tvCompany != null) tvCompany.setText("Recruiter ID: " + job.getRecruiterId());
            if (tvLocation != null) tvLocation.setText(job.getLocation());
            if (tvSalary != null) tvSalary.setText(String.format("$%.2f - $%.2f", job.getSalaryMin(), job.getSalaryMax()));

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(job);
                }
            });
        }
    }
}