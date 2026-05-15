package com.example.proyectointegrador.data.repository

import com.example.proyectointegrador.data.model.Report
import com.example.proyectointegrador.data.remote.ReportApi
import okhttp3.MultipartBody
import okhttp3.RequestBody

class ReportRepository(private val api: ReportApi) {
    suspend fun getReports(): List<Report> = api.getReports()
    
    suspend fun getReportById(id: Int): Report = api.getReportById(id)
    
    suspend fun createReport(
        title: RequestBody,
        description: RequestBody,
        latitude: RequestBody,
        longitude: RequestBody,
        image: MultipartBody.Part?
    ): Report = api.createReport(title, description, latitude, longitude, image)
}
