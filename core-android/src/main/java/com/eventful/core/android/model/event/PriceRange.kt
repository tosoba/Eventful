package com.eventful.core.android.model.event

import android.os.Parcelable
import com.eventful.core.model.event.IPriceRange
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PriceRange(
    override val type: String,
    override val currency: String,
    override val min: Double,
    override val max: Double
) : IPriceRange, Parcelable {
    constructor(other: IPriceRange) : this(other.type, other.currency, other.min, other.max)
}
