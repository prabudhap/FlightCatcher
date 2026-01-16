package com.example.flightcatcher
import android.widget.TextView
import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat


class MainActivity : ComponentActivity() {

    private lateinit var locationManager: LocationManager
    private lateinit var tvLatitude: TextView
    private lateinit var tvLongitude: TextView
    private var alertShown = false
    private var lastDistance = Float.MAX_VALUE



    // Modern permission request
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                startLocationUpdates()
            } else {
                Toast.makeText(this, "Location permission denied!", Toast.LENGTH_SHORT).show()
            }
        }

    // Mock flights for testing
    private val flights = listOf(
        Location("").apply { latitude = 12.9716; longitude = 77.5946 }, // Bangalore
        Location("").apply { latitude = 13.0827; longitude = 80.2707 }  // Chennai
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Toast.makeText(this, "FlightCatcher started", Toast.LENGTH_SHORT).show()
        tvLatitude = findViewById(R.id.tvLatitude)
        tvLongitude = findViewById(R.id.tvLongitude)


        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        Log.d("FlightCatcher", "GPS enabled = ${locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)}")
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startLocationUpdates()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    override fun onPause() {
        super.onPause()
        if (::locationManager.isInitialized) {
            locationManager.removeUpdates(locationListener)
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun startLocationUpdates() {
        if (!hasLocationPermission()) return

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "Please enable GPS", Toast.LENGTH_SHORT).show()
            return
        }

        locationManager.requestLocationUpdates(
            LocationManager.NETWORK_PROVIDER,
            2000L,
            2f,
            locationListener
        )
    }


    private val locationListener = object : LocationListener {

        override fun onLocationChanged(location: Location) {

            for (flight in flights) {
                val distance = location.distanceTo(flight)

                if (distance <= 5000 && !alertShown) {
                    alertShown = true

                    Toast.makeText(
                        this@MainActivity,
                        "✈ Flight within 5 km",
                        Toast.LENGTH_SHORT
                    ).show()

                    Log.d("FlightCatcher", "Flight alert triggered at $distance m")
                }

                // Reset when user moves away
                if (distance > 6000) {
                    alertShown = false
                }

                lastDistance = distance
            }

        }

    }
}

