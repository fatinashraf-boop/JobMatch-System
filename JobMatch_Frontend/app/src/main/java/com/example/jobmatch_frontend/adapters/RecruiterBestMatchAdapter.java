package com.example.jobmatch_frontend.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobmatch_frontend.R;
import com.example.jobmatch_frontend.models.MatchScore;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RecruiterBestMatchAdapter
        extends RecyclerView.Adapter<RecruiterBestMatchAdapter.ViewHolder> {

    public interface OnCandidateClickListener {
        void onCandidateClick(MatchScore match);
    }

    private final List<MatchScore> matches = new ArrayList<>();
    private final OnCandidateClickListener listener;

    public RecruiterBestMatchAdapter(
            OnCandidateClickListener listener
    ) {
        this.listener = listener;
    }

    public void setMatches(List<MatchScore> newMatches) {

        matches.clear();

        if (newMatches != null) {
            matches.addAll(newMatches);
        }

        notifyDataSetChanged();
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
                                R.layout.item_recruiter_best_match,
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

        MatchScore match = matches.get(position);

        // NAME
        String name = safeText(
                match.getFullName(),
                "Candidate"
        );

        holder.tvCandidateName.setText(name);

        // INITIAL
        String initial = "C";

        if (!name.trim().isEmpty()) {
            initial =
                    String.valueOf(
                            Character.toUpperCase(
                                    name.trim().charAt(0)
                            )
                    );
        }

        holder.tvCandidateInitial.setText(initial);

        // HEADLINE
        holder.tvHeadline.setText(
                safeText(
                        match.getHeadline(),
                        "Candidate Profile"
                )
        );

        // LOCATION
        holder.tvLocation.setText(
                safeText(
                        match.getLocation(),
                        "Location not specified"
                )
        );

        // EXPECTED SALARY
        Double expectedSalary =
                match.getExpectedSalary();

        if (
                expectedSalary != null
                        &&
                        expectedSalary > 0
        ) {

            holder.tvExpectedSalary.setText(
                    String.format(
                            Locale.getDefault(),
                            "Expected: RM %,.0f",
                            expectedSalary
                    )
            );

        } else {

            holder.tvExpectedSalary.setText(
                    "Expected salary not specified"
            );
        }

        // AI MATCH SCORE
        double score =
                match.getSimilarityScore();

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

        // AI MATCH REASON
        holder.tvMatchReason.setText(
                safeText(
                        match.getMatchReason(),
                        "AI candidate match"
                )
        );

        // RANK
        Integer ranking =
                match.getRankingPosition();

        if (ranking != null && ranking > 0) {

            holder.tvRanking.setVisibility(
                    View.VISIBLE
            );

            holder.tvRanking.setText(
                    "#" + ranking
            );

        } else {

            holder.tvRanking.setVisibility(
                    View.GONE
            );
        }

        holder.itemView.setOnClickListener(
                view -> {

                    if (listener != null) {
                        listener.onCandidateClick(match);
                    }
                }
        );
    }

    @Override
    public int getItemCount() {
        return matches.size();
    }

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

    static class ViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvCandidateInitial;
        TextView tvCandidateName;
        TextView tvHeadline;
        TextView tvExpectedSalary;
        TextView tvLocation;
        TextView tvMatchScore;
        TextView tvMatchReason;
        TextView tvRanking;

        public ViewHolder(
                @NonNull View itemView
        ) {

            super(itemView);

            tvCandidateInitial =
                    itemView.findViewById(
                            R.id.tvCandidateInitial
                    );

            tvCandidateName =
                    itemView.findViewById(
                            R.id.tvCandidateName
                    );

            tvHeadline =
                    itemView.findViewById(
                            R.id.tvHeadline
                    );

            tvExpectedSalary =
                    itemView.findViewById(
                            R.id.tvExpectedSalary
                    );

            tvLocation =
                    itemView.findViewById(
                            R.id.tvLocation
                    );

            tvMatchScore =
                    itemView.findViewById(
                            R.id.tvMatchScore
                    );

            tvMatchReason =
                    itemView.findViewById(
                            R.id.tvMatchReason
                    );

            tvRanking =
                    itemView.findViewById(
                            R.id.tvRanking
                    );
        }
    }
}