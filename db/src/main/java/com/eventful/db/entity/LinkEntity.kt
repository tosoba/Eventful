package com.eventful.db.entity

import com.eventful.core.model.event.ILink
import com.eventful.core.model.event.LinkType

data class LinkEntity(override val url: String, override val type: LinkType) : ILink {
    constructor(other: ILink) : this(other.url, other.type)
}
