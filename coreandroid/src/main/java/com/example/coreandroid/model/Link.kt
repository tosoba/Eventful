package com.example.coreandroid.model

import android.os.Parcelable
import com.example.core.model.event.ILink
import com.example.core.model.event.LinkType
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Link(override val url: String, override val type: LinkType) : ILink, Parcelable {
    constructor(other: ILink) : this(other.url, other.type)
}