package com.example.flightcatcher.utils

import com.example.flightcatcher.model.FlightModel
import kotlin.math.*

object GeoUtils {

    private const val EARTH_RADIUS_KM = 6371.0

    fun distanceInKm(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(lat1)) *
                cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2.0)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return EARTH_RADIUS_KM * c
    }

    fun isFlightWithin5Km(
        userLat: Double,
        userLon: Double,
        flight: FlightModel,
        radiusKm: Double
    ): Boolean {
        val distance = distanceInKm(
            userLat,
            userLon,
            flight.latitude,
            flight.longitude
        )

        return distance <= radiusKm
    }
}
