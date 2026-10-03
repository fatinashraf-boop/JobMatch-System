package com.example.jobmatch_frontend.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobmatch_frontend.R;
import com.example.jobmatch_frontend.models.JobseekerApplicationsResponse;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;


public class JobseekerApplicationAdapter
        extends RecyclerView.Adapter<
        JobseekerApplicationAdapter.ApplicationViewHolder> {


    public interface OnApplicationClickListener {

        void onApplicationClick(
                JobseekerApplicationsResponse.ApplicationItem item
        );
    }


    private final List<
            JobseekerApplicationsResponse.ApplicationItem
            > applications;

    private final OnApplicationClickListener listener;


    public JobseekerApplicationAdapter(
            List<JobseekerApplicationsResponse.ApplicationItem> applications,
            OnApplicationClickListener listener
    ) {

        this.applications = applications;

        this.listener = listener;
    }


    @NonNull
    @Override
    public ApplicationViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view =
                LayoutInflater
                        .from(parent.getContext())
                        .inflate(
                                R.layout.item_jobseeker_application,
                                parent,
                                false
                        );


        return new ApplicationViewHolder(view);
    }


    @Override
    public void onBindViewHolder(
            @NonNull ApplicationViewHolder holder,
            int position
    ) {

        JobseekerApplicationsResponse.ApplicationItem item =
                applications.get(position);


        holder.tvApplicationJobTitle.setText(
                safeText(item.getJobTitle())
        );


        holder.tvApplicationCompany.setText(
                safeText(item.getCompanyName())
        );


        holder.tvApplicationLocation.setText(
                safeText(item.getLocation())
        );


        holder.tvApplicationSalary.setText(
                formatSalary(
                        item.getSalaryMin(),
                        item.getSalaryMax()
                )
        );


        holder.tvApplicationStatus.setText(
                formatStatus(item)
        );


        holder.tvAppliedDate.setText(
                "Applied: "
                        + formatDate(
                        item.getAppliedAt()
                )
        );


        holder.itemView.setOnClickListener(v -> {

            if (listener != null) {

                listener.onApplicationClick(item);
            }
        });
    }


    @Override
    public int getItemCount() {

        return applications == null
                ? 0
                : applications.size();
    }


    private String formatStatus(
            JobseekerApplicationsResponse.ApplicationItem item
    ) {

        String shortlistStatus =
                item.getShortlistStatus();


        if (
                shortlistStatus != null
                        &&
                        !shortlistStatus.trim().isEmpty()
                        &&
                        !"active".equalsIgnoreCase(shortlistStatus)
        ) {

            return capitalize(shortlistStatus);
        }


        return capitalize(
                item.getApplicationStatus()
        );
    }


    private String capitalize(String value) {

        if (
                value == null
                        ||
                        value.trim().isEmpty()
        ) {

            return "Pending";
        }


        value = value.trim();


        return value.substring(0, 1)
                .toUpperCase(Locale.getDefault())
                + value.substring(1)
                .toLowerCase(Locale.getDefault());
    }


    private String formatSalary(
            Double min,
            Double max
    ) {

        if (min == null && max == null) {

            return "Salary not specified";
        }


        if (min != null && max != null) {

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


    private String formatDate(String value) {

        if (
                value == null
                        ||
                        value.trim().isEmpty()
        ) {

            return "Not available";
        }


        try {

            SimpleDateFormat input =
                    new SimpleDateFormat(
                            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                            Locale.US
                    );


            input.setTimeZone(
                    TimeZone.getTimeZone("UTC")
            );


            Date date =
                    input.parse(value);


            if (date == null) {

                return value;
            }


            SimpleDateFormat output =
                    new SimpleDateFormat(
                            "dd MMM yyyy",
                            Locale.getDefault()
                    );


            return output.format(date);


        } catch (Exception e) {

            return value;
        }
    }


    private String safeText(String value) {

        if (
                value == null
                        ||
                        value.trim().isEmpty()
        ) {

            return "Not specified";
        }


        return value;
    }


    static class ApplicationViewHolder
            extends RecyclerView.ViewHolder {


        TextView tvApplicationJobTitle;
        TextView tvApplicationCompany;
        TextView tvApplicationStatus;
        TextView tvApplicationLocation;
        TextView tvApplicationSalary;
        TextView tvAppliedDate;


        public ApplicationViewHolder(
                @NonNull View itemView
        ) {

            super(itemView);


            tvApplicationJobTitle =
                    itemView.findViewById(
                            R.id.tvApplicationJobTitle
                    );


            tvApplicationCompany =
                    itemView.findViewById(
                            R.id.tvApplicationCompany
                    );


            tvApplicationStatus =
                    itemView.findViewById(
                            R.id.tvApplicationStatus
                    );


            tvApplicationLocation =
                    itemView.findViewById(
                            R.id.tvApplicationLocation
                    );


            tvApplicationSalary =
                    itemView.findViewById(
                            R.id.tvApplicationSalary
                    );


            tvAppliedDate =
                    itemView.findViewById(
                            R.id.tvAppliedDate
                    );
        }
    }
}