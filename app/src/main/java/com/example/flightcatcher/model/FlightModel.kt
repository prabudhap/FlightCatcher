package com.example.flightcatcher.model

import com.example.flightcatcher.utils.LocationUtils

data class FlightModel(
    val flightNumber: String,
    val latitude: Double,
    val longitude: Double,
    val altitude: Int? = null
) {

    fun distanceFrom(
        userLat: Double,
        userLon: Double
    ): Double {
        return LocationUtils.distanceInKm(
            userLat,
            userLon,
            latitude,
            longitude
        )
    }

    fun isWithinRadius(
        userLat: Double,
        userLon: Double,
        radiusKm: Double = 5.0
    ): Boolean {
        return distanceFrom(userLat, userLon) <= radiusKm
    }
}
