package com.example.flightcatcher.viewmodel

import androidx.lifecycle.ViewModel
import com.example.flightcatcher.model.FlightModel
import com.example.flightcatcher.utils.GeoUtils

class FlightViewModel : ViewModel() {

    /**
     * Returns flights within 5 km radius of user location
     */
    fun getNearbyFlights(
        userLat: Double,
        userLon: Double,
        flights: List<FlightModel>
    ): List<FlightModel> {

        return flights.filter { flight ->
            GeoUtils.isFlightWithin5Km(
                userLat = userLat,
                userLon = userLon,
                flight = flight,
                radiusKm = 5.0
            )
        }
    }
}
