package com.example.jobmatch_frontend.api;

import com.example.jobmatch_frontend.models.OCRListResponse;
import com.example.jobmatch_frontend.models.OCRResponse;
import com.example.jobmatch_frontend.models.OCRResult;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface OCRApi {

    /*
     * POST /api/ocr
     */
    @POST("ocr")
    Call<OCRResponse> processOCR(
            @Body OCRResult ocrResult
    );

    /*
     * GET /api/ocr/document/{document_id}
     */
    @GET("ocr/document/{document_id}")
    Call<OCRListResponse> getOCRByDocument(
            @Path("document_id") int documentId
    );

    /*
     * GET /api/ocr/{id}
     */
    @GET("ocr/{id}")
    Call<OCRResponse> getOCRById(
            @Path("id") int ocrId
    );

    /*
     * PUT /api/ocr/{id}
     */
    @PUT("ocr/{id}")
    Call<OCRResponse> updateOCR(
            @Path("id") int ocrId,
            @Body OCRResult ocrResult
    );

    /*
     * DELETE /api/ocr/{id}
     */
    @DELETE("ocr/{id}")
    Call<OCRResponse> deleteOCR(
            @Path("id") int ocrId
    );
}