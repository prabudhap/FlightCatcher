package com.example.flightcatcher.data

import android.util.Log
import com.example.flightcatcher.api.OpenSkyApiClient
import com.example.flightcatcher.model.FlightModel

class FlightRepository {

    /**
     * Fetch live flights from OpenSky Network API
     */
    suspend fun getLiveFlights(): List<FlightModel> {
        return try {
            val response = OpenSkyApiClient.apiService.getAllFlights()
            val flights = OpenSkyApiClient.convertToFlightModels(response)
            Log.d("FlightRepository", "Fetched ${flights.size} flights from API")
            flights
        } catch (e: Exception) {
            Log.e("FlightRepository", "Error fetching flights: ${e.message}", e)
            emptyList()
        }
    }
}
