package com.example.jobmatch_frontend.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobmatch_frontend.R;
import com.example.jobmatch_frontend.models.Application;

import java.util.List;
import java.util.Locale;

public class ApplicationAdapter
        extends RecyclerView.Adapter<ApplicationAdapter.ApplicationViewHolder> {

    private final List<Application> applications;

    private final OnWithdrawClickListener withdrawClickListener;

    private final OnApplicationClickListener applicationClickListener;


    // ==========================================
    // WITHDRAW CLICK LISTENER
    // ==========================================

    public interface OnWithdrawClickListener {

        void onWithdrawClick(
                Application application
        );
    }

    public interface OnApplicationClickListener {

        void onApplicationClick(
                Application application
        );
    }


    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public ApplicationAdapter(
            List<Application> applications,
            OnWithdrawClickListener withdrawClickListener,
            OnApplicationClickListener applicationClickListener
    ) {

        this.applications =
                applications;

        this.withdrawClickListener =
                withdrawClickListener;

        this.applicationClickListener =
                applicationClickListener;
    }

    // ==========================================
    // CREATE VIEW HOLDER
    // ==========================================

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
                                R.layout.item_application,
                                parent,
                                false
                        );

        return new ApplicationViewHolder(view);
    }


    // ==========================================
    // BIND DATA
    // ==========================================

    @Override
    public void onBindViewHolder(
            @NonNull ApplicationViewHolder holder,
            int position
    ) {

        Application application =
                applications.get(position);


        holder.tvJobTitle.setText(
                safeText(
                        application.getJobTitle(),
                        "Job"
                )
        );


        holder.tvCompanyName.setText(
                safeText(
                        application.getCompanyName(),
                        "Company"
                )
        );


        holder.tvLocation.setText(
                safeText(
                        application.getLocation(),
                        "Location not specified"
                )
        );


        holder.tvJobType.setText(
                safeText(
                        application.getJobType(),
                        "Job type not specified"
                )
        );


        String status =
                safeText(
                        application.getApplicationStatus(),
                        "pending"
                );


        holder.tvApplicationStatus.setText(
                capitalize(status)
        );


        holder.tvAppliedDate.setText(
                "Applied: "
                        +
                        formatDate(
                                application.getAppliedAt()
                        )
        );

        String shortlistStatus =
                application.getShortlistStatus();

        String recruitmentStatus;

        if (
                shortlistStatus != null
                        &&
                        !shortlistStatus.trim().isEmpty()
        ) {

            recruitmentStatus =
                    capitalize(shortlistStatus);

        } else if (
                status.equalsIgnoreCase("shortlisted")
        ) {

            recruitmentStatus =
                    "Shortlisted";

        } else if (
                status.equalsIgnoreCase("rejected")
        ) {

            recruitmentStatus =
                    "Rejected";

        } else if (
                status.equalsIgnoreCase("withdrawn")
        ) {

            recruitmentStatus =
                    "Withdrawn";

        } else if (
                status.equalsIgnoreCase("reviewed")
        ) {

            recruitmentStatus =
                    "Application Reviewed";

        } else {

            recruitmentStatus =
                    "Not shortlisted yet";
        }

        holder.tvRecruitmentStatus.setText(
                recruitmentStatus
        );

        // ======================================
        // WITHDRAW BUTTON
        // ======================================

        boolean canWithdraw =
                !status.equalsIgnoreCase("withdrawn")
                        &&
                        !status.equalsIgnoreCase("rejected")
                        &&
                        !status.equalsIgnoreCase("shortlisted");


        holder.btnWithdraw.setVisibility(
                canWithdraw
                        ? View.VISIBLE
                        : View.GONE
        );


        // Important for RecyclerView recycling
        holder.btnWithdraw.setOnClickListener(null);


        if (canWithdraw) {

            holder.btnWithdraw.setOnClickListener(
                    v -> {

                        if (
                                withdrawClickListener
                                        != null
                        ) {

                            withdrawClickListener
                                    .onWithdrawClick(
                                            application
                                    );
                        }
                    }
            );
        }

        holder.itemView.setOnClickListener(
                v -> {

                    if (
                            applicationClickListener
                                    != null
                    ) {

                        applicationClickListener
                                .onApplicationClick(
                                        application
                                );
                    }
                }
        );
    }


    // ==========================================
    // ITEM COUNT
    // ==========================================

    @Override
    public int getItemCount() {

        return applications.size();
    }


    // ==========================================
    // SAFE TEXT
    // ==========================================

    private String safeText(
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


    // ==========================================
    // CAPITALIZE
    // ==========================================

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


        String text =
                value.trim();


        return text.substring(0, 1)
                .toUpperCase(Locale.getDefault())
                +
                text.substring(1)
                        .toLowerCase(Locale.getDefault());
    }

    // ==========================================
// FORMAT API DATE
// ==========================================

    private String formatDate(
            String dateString
    ) {

        if (
                dateString == null
                        ||
                        dateString.trim().isEmpty()
        ) {

            return "-";
        }


        String value =
                dateString.trim();


        // Possible formats returned by Node/MySQL/Gson
        String[] inputFormats = {

                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",

                "yyyy-MM-dd'T'HH:mm:ss'Z'",

                "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",

                "yyyy-MM-dd'T'HH:mm:ssXXX",

                "yyyy-MM-dd HH:mm:ss"
        };


        for (
                String inputFormat
                : inputFormats
        ) {

            try {

                SimpleDateFormat parser =
                        new SimpleDateFormat(
                                inputFormat,
                                Locale.getDefault()
                        );


                Date date =
                        parser.parse(
                                value
                        );


                if (date != null) {

                    SimpleDateFormat formatter =
                            new SimpleDateFormat(
                                    "dd MMM yyyy, h:mm a",
                                    Locale.getDefault()
                            );


                    return formatter.format(
                            date
                    );
                }


            } catch (ParseException ignored) {

                // Try the next supported format
            }
        }


        // If API returns an unexpected format,
        // show the original value instead of crashing.
        return value;
    }

    // ==========================================
    // VIEW HOLDER
    // ==========================================

    static class ApplicationViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvJobTitle;
        TextView tvCompanyName;
        TextView tvLocation;
        TextView tvJobType;
        TextView tvApplicationStatus;
        TextView tvAppliedDate;
        TextView tvRecruitmentStatus;
        Button btnWithdraw;


        public ApplicationViewHolder(
                @NonNull View itemView
        ) {

            super(itemView);


            tvJobTitle =
                    itemView.findViewById(
                            R.id.tvJobTitle
                    );

            tvCompanyName =
                    itemView.findViewById(
                            R.id.tvCompanyName
                    );

            tvLocation =
                    itemView.findViewById(
                            R.id.tvLocation
                    );

            tvJobType =
                    itemView.findViewById(
                            R.id.tvJobType
                    );

            tvApplicationStatus =
                    itemView.findViewById(
                            R.id.tvApplicationStatus
                    );

            tvAppliedDate =
                    itemView.findViewById(
                            R.id.tvAppliedDate
                    );

            tvRecruitmentStatus =
                    itemView.findViewById(
                            R.id.tvRecruitmentStatus
                    );

            btnWithdraw =
                    itemView.findViewById(
                            R.id.btnWithdraw
                    );
        }
    }
}