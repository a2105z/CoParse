package com.coparse.app.data.remote

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface CoparseApi {

    @Multipart
    @POST("v1/documents")
    suspend fun uploadDocument(
        @Part file: MultipartBody.Part,
        @Part("hint_contract_type") hintContractType: RequestBody,
        @Part("hint_role") hintRole: RequestBody,
    ): DocumentCreateResponse

    @GET("v1/jobs/{jobId}")
    suspend fun getJob(@Path("jobId") jobId: String): JobStatusResponse

    @GET("v1/documents/{documentId}/analysis")
    suspend fun getAnalysis(@Path("documentId") documentId: String): AnalysisResponse

    @POST("v1/documents/{documentId}/reanalyze")
    suspend fun reanalyze(
        @Path("documentId") documentId: String,
        @Body body: ReanalyzeRequest,
    ): DocumentCreateResponse
}
