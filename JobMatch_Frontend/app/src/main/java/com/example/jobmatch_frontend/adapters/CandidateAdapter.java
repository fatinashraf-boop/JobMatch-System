package com.example.jobmatch_frontend.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobmatch_frontend.R;
import com.example.jobmatch_frontend.models.CandidateMatch;

import java.util.List;

public class CandidateAdapter extends RecyclerView.Adapter<CandidateAdapter.CandidateViewHolder> {

    private final Context context;
    private final List<CandidateMatch> candidateList;

    public CandidateAdapter(Context context, List<CandidateMatch> candidateList) {
        this.context = context;
        this.candidateList = candidateList;
    }

    @NonNull
    @Override
    public CandidateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_candidate, parent, false);
        return new CandidateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CandidateViewHolder holder, int position) {
        CandidateMatch match = candidateList.get(position);

        holder.tvCandidateHeadline.setText(match.getHeadline() != null ? match.getHeadline() : "Candidate #" + match.getProfileId());
        holder.tvMatchScore.setText((int)(match.getSimilarityScore() * 100) + "% Match");
        holder.tvCandidateSkills.setText("Skills: " + match.getSkills());
        holder.tvCandidateReason.setText(match.getMatchReason());

        holder.itemView.setOnClickListener(v ->
                Toast.makeText(context, "Viewing Profile ID: " + match.getProfileId(), Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public int getItemCount() {
        return candidateList != null ? candidateList.size() : 0;
    }

    public static class CandidateViewHolder extends RecyclerView.ViewHolder {
        TextView tvCandidateHeadline, tvMatchScore, tvCandidateSkills, tvCandidateReason;

        public CandidateViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCandidateHeadline = itemView.findViewById(R.id.tvCandidateHeadline);
            tvMatchScore = itemView.findViewById(R.id.tvMatchScore);
            tvCandidateSkills = itemView.findViewById(R.id.tvCandidateSkills);
            tvCandidateReason = itemView.findViewById(R.id.tvCandidateReason);
        }
    }
}