package com.example.coreandroid.model.event

import android.os.Parcelable
import com.example.core.model.event.IAttraction
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Attraction(
    override val id: String,
    override val name: String?,
    override val url: String?,
    override val links: List<Link>?,
    override val imageUrl: String?,
    override val kind: String?
) : IAttraction, Parcelable {
    constructor(other: IAttraction) : this(
        other.id,
        other.name,
        other.url,
        other.links?.map { Link(it) },
        other.imageUrl,
        other.kind
    )
}
