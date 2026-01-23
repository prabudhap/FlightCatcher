package com.example.flightcatcher.viewmodel

import androidx.lifecycle.ViewModel
import com.example.flightcatcher.model.FlightModel
import com.example.flightcatcher.model.FlightProximityEvent
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
        radiusKm: Double = 5.0,
        onFlightEnter: ((FlightModel) -> Unit)? = null
    ): List<FlightProximityEvent> {

        val events = mutableListOf<FlightProximityEvent>()

        flights.forEach { flight ->
            val isInsideRadius = GeoUtils.isFlightWithin5Km(
                userLat,
                userLon,
                flight,
                radiusKm
            )

            val flightId = flight.id

            if (isInsideRadius && !activeFlightsInRadius.contains(flightId)) {
                activeFlightsInRadius.add(flightId)
                events.add(FlightProximityEvent.Enter(flight))
                onFlightEnter?.invoke(flight)
            }

            if (!isInsideRadius && activeFlightsInRadius.contains(flightId)) {
                activeFlightsInRadius.remove(flightId)
                events.add(FlightProximityEvent.Exit(flight))
            }
        }

        return events
    }

}
