package com.example.proyectointegrador.data.remote

import com.example.proyectointegrador.data.model.Report
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface ReportApi {
    @GET("reports")
    suspend fun getReports(): List<Report>

    @GET("reports/{id}")
    suspend fun getReportById(@Path("id") id: Int): Report

    @Multipart
    @POST("reports")
    suspend fun createReport(
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part("latitude") latitude: RequestBody,
        @Part("longitude") longitude: RequestBody,
        @Part image: MultipartBody.Part?
    ): Report
}
