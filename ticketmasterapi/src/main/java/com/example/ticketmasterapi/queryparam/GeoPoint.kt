package com.example.ticketmasterapi.queryparam

import com.github.davidmoten.geo.GeoHash
import com.github.davidmoten.geo.LatLong

class GeoPoint(private val lat: Double, private val lng: Double) {
    override fun toString(): String = GeoHash.encodeHash(LatLong(lat, lng), 9)
}