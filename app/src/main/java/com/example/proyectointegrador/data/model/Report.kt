package com.example.proyectointegrador.data.model

data class Report(
    val id: Int? = null,
    val title: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val imageUrl: String? = null
)
