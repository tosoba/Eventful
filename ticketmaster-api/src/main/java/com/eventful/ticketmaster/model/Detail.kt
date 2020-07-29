package com.eventful.ticketmaster.model

import com.google.gson.annotations.SerializedName

data class Detail(
    @SerializedName("errorcode")
    val errorCode: String
)
