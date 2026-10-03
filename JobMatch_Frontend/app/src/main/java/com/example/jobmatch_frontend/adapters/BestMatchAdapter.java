package com.example.jobmatch_frontend.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobmatch_frontend.R;
import com.example.jobmatch_frontend.models.JobseekerDashboardResponse;

import java.util.List;
import java.util.Locale;


public class BestMatchAdapter
        extends RecyclerView.Adapter<
        BestMatchAdapter.BestMatchViewHolder> {


    // ==========================================
    // CLICK LISTENER
    // ==========================================

    public interface OnBestMatchClickListener {

        void onBestMatchClick(
                JobseekerDashboardResponse.BestMatch match
        );
    }


    private final List<
            JobseekerDashboardResponse.BestMatch
            > bestMatches;


    private final OnBestMatchClickListener
            listener;


    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public BestMatchAdapter(
            List<JobseekerDashboardResponse.BestMatch> bestMatches,
            OnBestMatchClickListener listener
    ) {

        this.bestMatches =
                bestMatches;

        this.listener =
                listener;
    }


    // ==========================================
    // CREATE VIEW HOLDER
    // ==========================================

    @NonNull
    @Override
    public BestMatchViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view =
                LayoutInflater
                        .from(
                                parent.getContext()
                        )
                        .inflate(
                                R.layout.item_best_match,
                                parent,
                                false
                        );


        return new BestMatchViewHolder(
                view
        );
    }


    // ==========================================
    // BIND DATA
    // ==========================================

    @Override
    public void onBindViewHolder(
            @NonNull BestMatchViewHolder holder,
            int position
    ) {

        JobseekerDashboardResponse.BestMatch match =
                bestMatches.get(
                        position
                );


        // JOB TITLE
        holder.tvJobTitle.setText(
                safeText(
                        match.getJobTitle()
                )
        );


        // COMPANY
        holder.tvCompanyName.setText(
                safeText(
                        match.getCompanyName()
                )
        );


        // COMPANY INITIAL
        holder.tvCompanyLogo.setText(
                getCompanyInitial(
                        match.getCompanyName()
                )
        );


        // MATCH SCORE
        double score = match.getSimilarityScore();

        // Supports both:
        // 0.82 -> 82%
        // 82   -> 82%
        if (score >= 0 && score <= 1.0) {
            score = score * 100.0;
        }

        holder.tvMatchScore.setText(
                String.format(
                        Locale.getDefault(),
                        "%.0f%%",
                        score
                )
        );


        // SALARY
        holder.tvSalary.setText(
                formatSalary(
                        match.getSalaryMin(),
                        match.getSalaryMax()
                )
        );


        // LOCATION
        holder.tvLocation.setText(
                safeText(
                        match.getLocation()
                )
        );


        // JOB TYPE
        holder.tvJobType.setText(
                safeText(
                        match.getJobType()
                )
        );


        // MATCH REASON
        String reason =
                match.getMatchReason();


        if (
                reason == null
                        ||
                        reason.trim().isEmpty()
        ) {

            holder.tvMatchReason.setVisibility(
                    View.GONE
            );

        } else {

            holder.tvMatchReason.setVisibility(
                    View.VISIBLE
            );

            holder.tvMatchReason.setText(
                    reason
            );
        }


        // CLICK
        holder.itemView.setOnClickListener(v -> {

            if (listener != null) {

                listener.onBestMatchClick(
                        match
                );
            }
        });
    }


    // ==========================================
    // ITEM COUNT
    // ==========================================

    @Override
    public int getItemCount() {

        return bestMatches != null
                ? bestMatches.size()
                : 0;
    }


    // ==========================================
    // VIEW HOLDER
    // ==========================================

    static class BestMatchViewHolder
            extends RecyclerView.ViewHolder {


        TextView tvCompanyLogo;

        TextView tvJobTitle;

        TextView tvCompanyName;

        TextView tvMatchScore;

        TextView tvSalary;

        TextView tvLocation;

        TextView tvJobType;

        TextView tvMatchReason;


        public BestMatchViewHolder(
                @NonNull View itemView
        ) {

            super(itemView);


            tvCompanyLogo =
                    itemView.findViewById(
                            R.id.tvCompanyLogo
                    );


            tvJobTitle =
                    itemView.findViewById(
                            R.id.tvJobTitle
                    );


            tvCompanyName =
                    itemView.findViewById(
                            R.id.tvCompanyName
                    );


            tvMatchScore =
                    itemView.findViewById(
                            R.id.tvMatchScore
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


            tvMatchReason =
                    itemView.findViewById(
                            R.id.tvMatchReason
                    );
        }
    }


    // ==========================================
    // SAFE TEXT
    // ==========================================

    private String safeText(
            String value
    ) {

        if (
                value == null
                        ||
                        value.trim().isEmpty()
        ) {

            return "Not specified";
        }


        return value.trim();
    }


    // ==========================================
    // COMPANY INITIAL
    // ==========================================

    private String getCompanyInitial(
            String companyName
    ) {

        if (
                companyName == null
                        ||
                        companyName.trim().isEmpty()
        ) {

            return "J";
        }


        return String
                .valueOf(
                        companyName
                                .trim()
                                .charAt(0)
                )
                .toUpperCase();
    }


    // ==========================================
    // SALARY
    // ==========================================

    private String formatSalary(
            Double minimum,
            Double maximum
    ) {

        if (
                minimum == null
                        &&
                        maximum == null
        ) {

            return "Salary not specified";
        }


        if (
                minimum != null
                        &&
                        maximum != null
        ) {

            return String.format(
                    Locale.getDefault(),
                    "RM %,.0f - RM %,.0f",
                    minimum,
                    maximum
            );
        }


        if (minimum != null) {

            return String.format(
                    Locale.getDefault(),
                    "From RM %,.0f",
                    minimum
            );
        }


        return String.format(
                Locale.getDefault(),
                "Up to RM %,.0f",
                maximum
        );
    }
}