package com.example.flightcatcher.viewmodel

import androidx.lifecycle.ViewModel
import com.example.flightcatcher.model.FlightModel
import com.example.flightcatcher.utils.GeoUtils

class FlightViewModel : ViewModel() {

    // Flights that have already triggered notification
    private val activeFlightsInRadius = mutableSetOf<String>()

    /**
     * WhatsApp-style proximity detection
     *
     * - Notifies ONLY when flight ENTERS radius
     * - No repeated notifications
     * - Re-notifies if flight exits and re-enters
     */
    fun detectNearbyFlights(
        userLat: Double,
        userLon: Double,
        flights: List<FlightModel>,
        onFlightEntered: (FlightModel) -> Unit
    ) {
        flights.forEach { flight ->

            val isInsideRadius = GeoUtils.isFlightWithin5Km(
                userLat = userLat,
                userLon = userLon,
                flight = flight,
                radiusKm = 5.0
            )

            val flightId = flight.id

            // ENTRY event → notify ONCE
            if (isInsideRadius && !activeFlightsInRadius.contains(flightId)) {
                activeFlightsInRadius.add(flightId)
                onFlightEntered(flight)
            }

            // EXIT event → reset
            if (!isInsideRadius && activeFlightsInRadius.contains(flightId)) {
                activeFlightsInRadius.remove(flightId)
            }
        }
    }
}
