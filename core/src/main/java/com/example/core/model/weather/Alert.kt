package com.example.core.model.weather

data class Alert(
    val expires: String,
    val time: Int,
    val title: String,
    val uri: String
)