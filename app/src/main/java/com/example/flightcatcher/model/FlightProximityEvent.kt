package com.example.flightcatcher.model

sealed class FlightProximityEvent {
    data class Enter(val flight: FlightModel) : FlightProximityEvent()
    data class Exit(val flight: FlightModel) : FlightProximityEvent()
}
