package com.example.flightcatcher.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flightcatcher.data.FlightRepository
import com.example.flightcatcher.model.FlightModel
import com.example.flightcatcher.model.FlightProximityEvent
import com.example.flightcatcher.utils.GeoUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FlightViewModel : ViewModel() {

    private val repository = FlightRepository()
    
    // Flights that have already triggered notification
    private val activeFlightsInRadius = mutableSetOf<String>()
    
    // Current flights from API
    private val _flights = MutableStateFlow<List<FlightModel>>(emptyList())
    val flights: StateFlow<List<FlightModel>> = _flights.asStateFlow()
    
    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    /**
     * Fetch flights from API
     */
    fun fetchFlights() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val fetchedFlights = repository.getLiveFlights()
                _flights.value = fetchedFlights
            } catch (e: Exception) {
                // Error already logged in repository
                _flights.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

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
