package com.example.coreandroid.ticketmaster

import android.os.Parcelable
import com.example.core.model.ticketmaster.ILink
import com.example.core.model.ticketmaster.LinkType
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Link(override val url: String, override val type: LinkType) : ILink, Parcelable