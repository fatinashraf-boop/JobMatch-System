package com.example.jobmatch_frontend.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobmatch_frontend.R;
import com.example.jobmatch_frontend.models.RecruiterApplicantResponse;

import java.util.List;
import java.util.Locale;

public class RecruiterApplicantAdapter
        extends RecyclerView.Adapter<RecruiterApplicantAdapter.ViewHolder> {

    public interface OnApplicantClickListener {
        void onApplicantClick(
                RecruiterApplicantResponse.Applicant applicant
        );
    }


    private final List<RecruiterApplicantResponse.Applicant> applicants;
    private final OnApplicantClickListener listener;


    public RecruiterApplicantAdapter(
            List<RecruiterApplicantResponse.Applicant> applicants,
            OnApplicantClickListener listener
    ) {
        this.applicants = applicants;
        this.listener = listener;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view =
                LayoutInflater.from(parent.getContext())
                        .inflate(
                                R.layout.item_recruiter_applicant,
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

        RecruiterApplicantResponse.Applicant applicant =
                applicants.get(position);


        // =================================================
        // NAME
        // =================================================

        String name = applicant.getCandidateName();

        if (name == null || name.trim().isEmpty()) {
            name = "Candidate";
        }

        holder.tvCandidateName.setText(name);


        // =================================================
        // HEADLINE
        // =================================================

        String headline = applicant.getHeadline();

        if (headline == null || headline.trim().isEmpty()) {
            headline = "No headline provided";
        }

        holder.tvHeadline.setText(headline);


        // =================================================
        // LOCATION
        // =================================================

        String location = applicant.getLocation();

        if (location == null || location.trim().isEmpty()) {
            location = "Location not provided";
        }

        holder.tvLocation.setText(location);


        // =================================================
        // MATCH SCORE
        // =================================================

        if (applicant.getSimilarityScore() != null) {

            double score =
                    applicant.getSimilarityScore();

            /*
             * Supports either:
             *
             * 0.92
             *
             * or
             *
             * 92
             */

            if (score <= 1.0) {
                score = score * 100.0;
            }

            holder.tvMatchScore.setText(
                    String.format(
                            Locale.getDefault(),
                            "%.0f%% Match",
                            score
                    )
            );

            holder.tvMatchScore.setVisibility(
                    View.VISIBLE
            );

        } else {

            holder.tvMatchScore.setText(
                    "Not scored"
            );
        }


        // =================================================
        // APPLICATION STATUS
        // =================================================

        holder.tvApplicationStatus.setText(
                "Application: "
                        + formatStatus(
                        applicant.getApplicationStatus()
                )
        );


        // =================================================
        // RECRUITMENT STATUS
        // =================================================

        String recruitmentStatus =
                applicant.getShortlistStatus();

        if (
                recruitmentStatus == null
                        ||
                        recruitmentStatus.trim().isEmpty()
        ) {

            holder.tvRecruitmentStatus.setText(
                    "Recruitment: Not shortlisted"
            );

        } else {

            holder.tvRecruitmentStatus.setText(
                    "Recruitment: "
                            + formatStatus(
                            recruitmentStatus
                    )
            );
        }


        // =================================================
        // APPLIED DATE
        // =================================================

        String appliedAt =
                applicant.getAppliedAt();

        if (
                appliedAt == null
                        ||
                        appliedAt.trim().isEmpty()
        ) {

            holder.tvAppliedDate.setText(
                    "Applied date unavailable"
            );

        } else {

            holder.tvAppliedDate.setText(
                    "Applied: " + appliedAt
            );
        }


        // =================================================
        // CLICK
        // =================================================

        View.OnClickListener clickListener =
                v -> {

                    if (listener != null) {

                        listener.onApplicantClick(
                                applicant
                        );
                    }
                };


        holder.btnViewCandidate.setOnClickListener(
                clickListener
        );

        holder.itemView.setOnClickListener(
                clickListener
        );
    }


    @Override
    public int getItemCount() {
        return applicants.size();
    }


    // =====================================================
    // STATUS FORMATTER
    // =====================================================

    private String formatStatus(String status) {

        if (
                status == null
                        ||
                        status.trim().isEmpty()
        ) {

            return "Unknown";
        }


        status =
                status.replace(
                        "_",
                        " "
                );


        String[] words =
                status.split(" ");


        StringBuilder result =
                new StringBuilder();


        for (String word : words) {

            if (word.isEmpty()) {
                continue;
            }

            if (result.length() > 0) {
                result.append(" ");
            }

            result.append(
                    Character.toUpperCase(
                            word.charAt(0)
                    )
            );

            if (word.length() > 1) {

                result.append(
                        word.substring(1)
                                .toLowerCase()
                );
            }
        }


        return result.toString();
    }


    // =====================================================
    // VIEW HOLDER
    // =====================================================

    static class ViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvCandidateName;
        TextView tvHeadline;
        TextView tvMatchScore;
        TextView tvLocation;
        TextView tvApplicationStatus;
        TextView tvRecruitmentStatus;
        TextView tvAppliedDate;

        Button btnViewCandidate;


        public ViewHolder(
                @NonNull View itemView
        ) {

            super(itemView);

            tvCandidateName =
                    itemView.findViewById(
                            R.id.tvCandidateName
                    );

            tvHeadline =
                    itemView.findViewById(
                            R.id.tvHeadline
                    );

            tvMatchScore =
                    itemView.findViewById(
                            R.id.tvMatchScore
                    );

            tvLocation =
                    itemView.findViewById(
                            R.id.tvLocation
                    );

            tvApplicationStatus =
                    itemView.findViewById(
                            R.id.tvApplicationStatus
                    );

            tvRecruitmentStatus =
                    itemView.findViewById(
                            R.id.tvRecruitmentStatus
                    );

            tvAppliedDate =
                    itemView.findViewById(
                            R.id.tvAppliedDate
                    );

            btnViewCandidate =
                    itemView.findViewById(
                            R.id.btnViewCandidate
                    );
        }
    }
}