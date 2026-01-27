package com.example.flightcatcher.api.model

import com.google.gson.annotations.SerializedName

/**
 * Response from OpenSky Network API
 * Format: { "time": 1234567890, "states": [[...], [...]] }
 */
data class OpenSkyResponse(
    val time: Long?,
    val states: List<List<Any?>>?
)

/**
 * Flight state array indices from OpenSky Network API
 * Index mapping:
 * 0: icao24 (String)
 * 1: callsign (String or null)
 * 2: origin_country (String)
 * 3: time_position (Long or null)
 * 4: last_contact (Long)
 * 5: longitude (Double or null)
 * 6: latitude (Double or null)
 * 7: baro_altitude (Double or null)
 * 8: on_ground (Boolean)
 * 9: velocity (Double or null)
 * 10: true_track (Double or null)
 * 11: vertical_rate (Double or null)
 * 12: sensors (List<Int> or null)
 * 13: geo_altitude (Double or null)
 * 14: squawk (String or null)
 * 15: spi (Boolean)
 * 16: position_source (Int)
 */
object FlightStateIndices {
    const val ICAO24 = 0
    const val CALLSIGN = 1
    const val LONGITUDE = 5
    const val LATITUDE = 6
    const val BARO_ALTITUDE = 7
    const val GEO_ALTITUDE = 13
}

