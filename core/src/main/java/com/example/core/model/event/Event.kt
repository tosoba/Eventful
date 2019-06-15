package com.example.core.model.event

import com.google.gson.annotations.SerializedName

data class Event(
    val category: String,
    val country: String,
    val description: String,
    val duration: Int,
    val end: String,
    @SerializedName("first_seen") val firstSeen: String,
    val id: String,
    val labels: List<String>,
    @SerializedName("local_rank") val localRank: Int,
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