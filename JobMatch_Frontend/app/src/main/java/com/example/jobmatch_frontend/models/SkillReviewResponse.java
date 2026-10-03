
package com.example.jobmatch_frontend.models;

import com.google.gson.annotations.SerializedName;

public class SkillReviewResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("matching_updated")
    private boolean matchingUpdated;

    @SerializedName("matching_warning")
    private String matchingWarning;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public boolean isMatchingUpdated() {
        return matchingUpdated;
    }

    public String getMatchingWarning() {
        return matchingWarning;
    }
}
