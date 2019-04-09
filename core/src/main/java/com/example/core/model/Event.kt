package com.example.core.model


data class Event(
    val category: String,
    val country: String,
    val description: String,
    val duration: Int,
    val end: String,
    val firstSeen: String,
    val id: String,
    val labels: List<String>,
    val localRank: Int,
    val location: List<Double>,
    val rank: Int,
    val relevance: Double,
    val scope: String,
    val start: String,
    val state: String,
    val timezone: String?,
    val title: String,
    val updated: String
)