package com.example.flightcatcher.api

import com.example.flightcatcher.api.model.OpenSkyResponse
import retrofit2.http.GET

interface OpenSkyApiService {
    
    /**
     * Get all flights currently tracked by OpenSky Network
     * API: https://opensky-network.org/api/states/all
     */
    @GET("states/all")
    suspend fun getAllFlights(): OpenSkyResponse
}

