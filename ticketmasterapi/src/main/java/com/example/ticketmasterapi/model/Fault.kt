package com.example.ticketmasterapi.model

import com.google.gson.annotations.SerializedName

data class Fault(
    val detail: Detail?,
    @SerializedName("faultstring")
    val faultString: String?
)