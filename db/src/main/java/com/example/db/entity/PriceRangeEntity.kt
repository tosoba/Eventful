package com.example.db.entity

import com.example.core.model.ticketmaster.IPriceRange

data class PriceRangeEntity(
    override val type: String,
    override val currency: String,
    override val min: Double,
    override val max: Double
) : IPriceRange {
    constructor(other: IPriceRange) : this(other.type, other.currency, other.min, other.max)
}