package com.eventful.db.entity

import com.eventful.core.model.event.IPriceRange

data class PriceRangeEntity(
    override val type: String,
    override val currency: String,
    override val min: Double,
    override val max: Double
) : IPriceRange {
    constructor(other: IPriceRange) : this(other.type, other.currency, other.min, other.max)
}
