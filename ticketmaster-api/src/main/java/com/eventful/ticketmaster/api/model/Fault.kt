package com.eventful.ticketmaster.api.model

import com.google.gson.annotations.SerializedName

data class Fault(
    val detail: Detail?,
    @SerializedName("faultstring")
    val faultString: String?
)
