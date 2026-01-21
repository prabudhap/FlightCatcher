package com.example.flightcatcher.data

import com.example.flightcatcher.model.FlightModel

class FlightRepository {

    /**
     * Will fetch flights from API later
     */
    suspend fun getLiveFlights(): List<FlightModel> {
        // TODO: Replace with real API call
        return emptyList()
    }
}
