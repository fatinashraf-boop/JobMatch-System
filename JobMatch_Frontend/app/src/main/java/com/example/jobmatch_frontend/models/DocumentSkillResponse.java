
package com.example.jobmatch_frontend.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class DocumentSkillResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("data")
    private List<SkillItem> data;

    public boolean isSuccess() {
        return success;
    }

    public List<SkillItem> getData() {
        return data;
    }

    public static class SkillItem {

        @SerializedName("document_skill_id")
        private int documentSkillId;

        @SerializedName("skill_name")
        private String skillName;

        @SerializedName("matched_term")
        private String matchedTerm;

        @SerializedName("review_status")
        private String reviewStatus;

        public int getDocumentSkillId() {
            return documentSkillId;
        }

        public String getSkillName() {
            return skillName;
        }

        public String getMatchedTerm() {
            return matchedTerm;
        }

        public String getReviewStatus() {
            return reviewStatus;
        }
    }
}
