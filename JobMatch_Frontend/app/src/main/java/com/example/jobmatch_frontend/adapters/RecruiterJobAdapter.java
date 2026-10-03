package com.example.jobmatch_frontend.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobmatch_frontend.R;
import com.example.jobmatch_frontend.models.RecruiterJobListResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class RecruiterJobAdapter
        extends RecyclerView.Adapter<RecruiterJobAdapter.JobViewHolder> {


    public interface OnJobActionListener {

        // NEW
        void onViewAiMatches(
                RecruiterJobListResponse.RecruiterJob job
        );

        void onEditJob(
                RecruiterJobListResponse.RecruiterJob job
        );

        void onChangeStatus(
                RecruiterJobListResponse.RecruiterJob job
        );
    }


    private final List<RecruiterJobListResponse.RecruiterJob>
            jobs = new ArrayList<>();

    private final OnJobActionListener listener;


    public RecruiterJobAdapter(
            OnJobActionListener listener
    ) {

        this.listener =
                listener;
    }


    public void setJobs(
            List<RecruiterJobListResponse.RecruiterJob> newJobs
    ) {

        jobs.clear();

        if (newJobs != null) {

            jobs.addAll(
                    newJobs
            );
        }

        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public JobViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view =
                LayoutInflater
                        .from(parent.getContext())
                        .inflate(
                                R.layout.item_recruiter_job,
                                parent,
                                false
                        );


        return new JobViewHolder(
                view
        );
    }


    @Override
    public void onBindViewHolder(
            @NonNull JobViewHolder holder,
            int position
    ) {

        RecruiterJobListResponse.RecruiterJob job =
                jobs.get(position);


        holder.tvJobTitle.setText(
                valueOrDefault(
                        job.getJobTitle(),
                        "Untitled Job"
                )
        );


        holder.tvStatus.setText(
                formatStatus(
                        job.getStatus()
                )
        );


        holder.tvJobInfo.setText(
                valueOrDefault(
                        job.getJobType(),
                        "Job"
                )
                        +
                        " • "
                        +
                        valueOrDefault(
                                job.getLocation(),
                                "Location not specified"
                        )
        );


        holder.tvSalary.setText(
                formatSalary(job)
        );

        holder.tvApplicantCount.setText(
                String.format(
                        Locale.getDefault(),
                        "%d Active Applicants • %d Shortlisted",
                        job.getApplicantCount(),
                        job.getShortlistedCount()
                )
        );


        holder.btnAiMatches.setOnClickListener(
                view ->
                        listener.onViewAiMatches(
                                job
                        )
        );

        holder.btnEditJob.setOnClickListener(
                view ->
                        listener.onEditJob(
                                job
                        )
        );


        String status =
                job.getStatus();


        if (
                "closed".equalsIgnoreCase(status)
        ) {

            holder.btnChangeStatus.setText(
                    "Reopen Job"
            );

        } else if (
                "draft".equalsIgnoreCase(status)
        ) {

            holder.btnChangeStatus.setText(
                    "Publish Job"
            );

        } else {

            holder.btnChangeStatus.setText(
                    "Close Job"
            );
        }


        holder.btnChangeStatus.setOnClickListener(
                view ->
                        listener.onChangeStatus(
                                job
                        )
        );
    }


    @Override
    public int getItemCount() {

        return jobs.size();
    }


    private String formatSalary(
            RecruiterJobListResponse.RecruiterJob job
    ) {

        Double min =
                job.getSalaryMin();

        Double max =
                job.getSalaryMax();


        if (
                min == null &&
                        max == null
        ) {

            return "Salary not specified";
        }


        if (
                min != null &&
                        max != null
        ) {

            return String.format(
                    Locale.getDefault(),
                    "RM %,.0f - RM %,.0f",
                    min,
                    max
            );
        }


        if (min != null) {

            return String.format(
                    Locale.getDefault(),
                    "From RM %,.0f",
                    min
            );
        }


        return String.format(
                Locale.getDefault(),
                "Up to RM %,.0f",
                max
        );
    }


    private String formatStatus(
            String value
    ) {

        if (
                value == null ||
                        value.trim().isEmpty()
        ) {

            return "Unknown";
        }


        String clean =
                value
                        .replace("_", " ")
                        .trim();


        return clean.substring(0, 1)
                .toUpperCase()
                +
                clean.substring(1)
                        .toLowerCase();
    }


    private String valueOrDefault(
            String value,
            String fallback
    ) {

        return value == null ||
                value.trim().isEmpty()
                ? fallback
                : value;
    }


    static class JobViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvJobTitle;
        TextView tvStatus;
        TextView tvJobInfo;
        TextView tvSalary;
        TextView tvApplicantCount;
        Button btnAiMatches;
        Button btnEditJob;
        Button btnChangeStatus;


        JobViewHolder(
                @NonNull View itemView
        ) {

            super(itemView);


            tvJobTitle =
                    itemView.findViewById(
                            R.id.tvJobTitle
                    );

            tvStatus =
                    itemView.findViewById(
                            R.id.tvStatus
                    );

            tvJobInfo =
                    itemView.findViewById(
                            R.id.tvJobInfo
                    );

            tvSalary =
                    itemView.findViewById(
                            R.id.tvSalary
                    );

            tvApplicantCount =
                    itemView.findViewById(
                            R.id.tvApplicantCount
                    );

            btnAiMatches =
                    itemView.findViewById(
                            R.id.btnAiMatches
                    );

            btnEditJob =
                    itemView.findViewById(
                            R.id.btnEditJob
                    );

            btnChangeStatus =
                    itemView.findViewById(
                            R.id.btnChangeStatus
                    );
        }
    }
}