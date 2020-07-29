package com.eventful.core.model.event

interface IPriceRange {
    val type: String
    val currency: String
    val min: Double
    val max: Double
}
