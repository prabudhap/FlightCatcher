package com.example.flightcatcher.api

import android.util.Log
import com.example.flightcatcher.api.model.FlightStateIndices
import com.example.flightcatcher.api.model.OpenSkyResponse
import com.example.flightcatcher.model.FlightModel
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object OpenSkyApiClient {
    
    private const val BASE_URL = "https://opensky-network.org/api/"
    
    private val gson: Gson = GsonBuilder()
        .setLenient()
        .create()
    
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
    
    val apiService: OpenSkyApiService = retrofit.create(OpenSkyApiService::class.java)
    
    /**
     * Convert OpenSky API response to FlightModel list
     */
    fun convertToFlightModels(response: OpenSkyResponse): List<FlightModel> {
        val flights = mutableListOf<FlightModel>()
        
        response.states?.forEach { state ->
            try {
                // Extract data from state array
                val icao24 = state[FlightStateIndices.ICAO24] as? String ?: return@forEach
                val callsign = state[FlightStateIndices.CALLSIGN] as? String
                val longitude = (state[FlightStateIndices.LONGITUDE] as? Number)?.toDouble()
                val latitude = (state[FlightStateIndices.LATITUDE] as? Number)?.toDouble()
                
                // Only add flights with valid coordinates
                if (latitude != null && longitude != null) {
                    val altitude = (state[FlightStateIndices.BARO_ALTITUDE] as? Number)?.toInt()
                        ?: (state[FlightStateIndices.GEO_ALTITUDE] as? Number)?.toInt()
                    
                    val flight = FlightModel(
                        id = icao24,
                        flightNumber = callsign ?: icao24,
                        latitude = latitude,
                        longitude = longitude,
                        altitude = altitude,
                        callsign = callsign
                    )
                    flights.add(flight)
                }
            } catch (e: Exception) {
                // Skip invalid states
                Log.e("OpenSkyApiClient", "Error parsing flight state: ${e.message}")
            }
        }
        
        return flights
    }
}

