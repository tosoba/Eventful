package com.eventful.ticketmaster.api.model

import com.eventful.core.model.event.IPriceRange

data class PriceRange(
    override val type: String,
    override val currency: String,
    override val min: Double,
    override val max: Double
) : IPriceRange
