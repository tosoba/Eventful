package com.example.ticketmasterapi.model

import com.example.core.model.event.IPriceRange

data class PriceRange(
    override val type: String,
    override val currency: String,
    override val min: Double,
    override val max: Double
) : IPriceRange