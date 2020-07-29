package com.eventful.core.android.model.event

import android.os.Parcelable
import com.eventful.core.model.event.ILink
import com.eventful.core.model.event.LinkType
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Link(override val url: String, override val type: LinkType) : ILink, Parcelable {
    constructor(other: ILink) : this(other.url, other.type)
}
