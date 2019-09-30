package com.example.db.entity

import com.example.core.model.ticketmaster.ILink
import com.example.core.model.ticketmaster.LinkType

data class LinkEntity(
    override val url: String,
    override val type: LinkType
) : ILink {
    constructor(other: ILink) : this(other.url, other.type)
}