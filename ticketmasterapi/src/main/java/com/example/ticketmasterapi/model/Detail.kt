package com.example.ticketmasterapi.model

import com.google.gson.annotations.SerializedName

data class Detail(
    @SerializedName("errorcode")
    val errorCode: String
)