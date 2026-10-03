
package com.example.jobmatch_frontend.api;

import com.example.jobmatch_frontend.models.DocumentListResponse;
import com.example.jobmatch_frontend.models.DocumentSkillResponse;
import com.example.jobmatch_frontend.models.SkillReviewResponse;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.Path;

public interface DocumentApi {

    // Existing endpoints
    @GET("documents/me")
    Call<DocumentListResponse> getMyDocuments();

    @DELETE("documents/{id}")
    Call<Void> deleteDocument(
            @Path("id") int documentId
    );

    // NEW: Retrieve OCR-extracted skills
    @GET("documents/{id}/skills")
    Call<DocumentSkillResponse> getDocumentSkills(
            @Path("id") int documentId
    );

    // NEW: Confirm or reject a skill
    @PATCH("documents/{id}/skills/{skillId}/review")
    Call<SkillReviewResponse> reviewDocumentSkill(
            @Path("id") int documentId,
            @Path("skillId") int skillId,
            @Body JsonObject body
    );
}
