package com.example.jobmatch_frontend.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobmatch_frontend.R;
import com.example.jobmatch_frontend.models.Job;

import java.util.List;
import java.util.Locale;

public class JobBrowseAdapter
        extends RecyclerView.Adapter<JobBrowseAdapter.ViewHolder> {

    public interface OnJobClickListener {
        void onJobClick(Job job);
    }

    private final List<Job> jobs;
    private final OnJobClickListener listener;


    public JobBrowseAdapter(
            List<Job> jobs,
            OnJobClickListener listener
    ) {

        this.jobs = jobs;
        this.listener = listener;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view =
                LayoutInflater
                        .from(parent.getContext())
                        .inflate(
                                R.layout.item_browse_job,
                                parent,
                                false
                        );

        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {

        Job job =
                jobs.get(position);


        String company =
                safe(
                        job.getCompanyName(),
                        "Company"
                );

        holder.tvCompanyName.setText(
                company
        );


        holder.tvCompanyInitial.setText(
                String.valueOf(
                        Character.toUpperCase(
                                company.charAt(0)
                        )
                )
        );


        holder.tvJobTitle.setText(
                safe(
                        job.getJobTitle(),
                        "Job"
                )
        );


        holder.tvLocation.setText(
                safe(
                        job.getLocation(),
                        "Location not specified"
                )
        );


        holder.tvJobType.setText(
                safe(
                        job.getJobType(),
                        "Job"
                )
        );


        double minimum =
                job.getSalaryMin();

        double maximum =
                job.getSalaryMax();


        if (
                minimum > 0
                        &&
                        maximum > 0
        ) {

            holder.tvSalary.setText(
                    String.format(
                            Locale.getDefault(),
                            "RM %,.0f - RM %,.0f",
                            minimum,
                            maximum
                    )
            );

        } else if (minimum > 0) {

            holder.tvSalary.setText(
                    String.format(
                            Locale.getDefault(),
                            "From RM %,.0f",
                            minimum
                    )
            );

        } else {

            holder.tvSalary.setText(
                    "Salary not specified"
            );
        }


        holder.itemView.setOnClickListener(
                view -> {

                    if (listener != null) {
                        listener.onJobClick(job);
                    }
                }
        );
    }


    @Override
    public int getItemCount() {

        return jobs == null
                ? 0
                : jobs.size();
    }


    private String safe(
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


    static class ViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvCompanyInitial;
        TextView tvCompanyName;
        TextView tvJobTitle;
        TextView tvSalary;
        TextView tvLocation;
        TextView tvJobType;


        public ViewHolder(
                @NonNull View itemView
        ) {

            super(itemView);

            tvCompanyInitial =
                    itemView.findViewById(
                            R.id.tvCompanyInitial
                    );

            tvCompanyName =
                    itemView.findViewById(
                            R.id.tvCompanyName
                    );

            tvJobTitle =
                    itemView.findViewById(
                            R.id.tvJobTitle
                    );

            tvSalary =
                    itemView.findViewById(
                            R.id.tvSalary
                    );

            tvLocation =
                    itemView.findViewById(
                            R.id.tvLocation
                    );

            tvJobType =
                    itemView.findViewById(
                            R.id.tvJobType
                    );
        }
    }
}