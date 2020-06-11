package com.example.db.entity

import com.example.core.model.event.ILink
import com.example.core.model.event.LinkType

data class LinkEntity(
    override val url: String,
    override val type: LinkType
) : ILink {
    constructor(other: ILink) : this(other.url, other.type)
}
