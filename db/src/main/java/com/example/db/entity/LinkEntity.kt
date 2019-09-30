package com.example.db.entity

import com.example.core.model.ticketmaster.LinkType

data class LinkEntity(
    val url: String,
    val type: LinkType
)