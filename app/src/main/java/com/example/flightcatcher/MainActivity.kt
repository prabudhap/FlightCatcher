package com.example.flightcatcher
import android.widget.TextView
import android.Manifest
import android.content.pm.PackageManager
import android.location.*
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.location.LocationResult
import android.os.Looper
import com.example.flightcatcher.databinding.ActivityMainBinding







class MainActivity : ComponentActivity() {

    private lateinit var locationManager: LocationManager
    private lateinit var tvLatitude: TextView
    private lateinit var tvLongitude: TextView
    private var alertShown = false
    private var lastDistance = Float.MAX_VALUE
    private lateinit var latitudeTextView: TextView
    private lateinit var longitudeTextView: TextView
    private lateinit var fusedClient: FusedLocationProviderClient
    private lateinit var binding: ActivityMainBinding



    // Modern permission request
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                startLocationFlow()
            } else {
                Toast.makeText(this, "Location permission denied!", Toast.LENGTH_SHORT).show()
            }
        }


    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
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
        // Initialize binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fusedClient = LocationServices.getFusedLocationProviderClient(this)



        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        Log.d("FlightCatcher", "GPS enabled = ${locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)}")
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startLocationFlow()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (hasLocationPermission()) {

            // 🔹 1. Show something immediately (no waiting)
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)?.let { location: Location ->
                updateUI(location)
            }


            // 🔹 2. Decide provider safely
            if (isGooglePlayAvailable()) {
                startFusedUpdates()
            } else {
                startLocationUpdates()
            }

        } else {
            requestLocationPermission()
        }

    }
    private fun updateUI(location: Location) {
        binding.latitudeTextView.text = "Lat: ${location.latitude}"
        binding.longitudeTextView.text = "Lon: ${location.longitude}"
    }

    private fun isGooglePlayAvailable(): Boolean {
        return try {
            Class.forName("com.google.android.gms.common.GoogleApiAvailability")
            true
        } catch (e: Exception) {
            false
        }
    }


    override fun onPause() {
        super.onPause()
        if (::locationManager.isInitialized) {
            locationManager.removeUpdates(locationListener)
        }
    }

    private fun requestLocationPermission() {
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private val fusedCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val location = locationResult.lastLocation ?: return

            Log.d("FlightCatcher", "Fused Lat=${location.latitude}, Lon=${location.longitude}")

            for (flight in flights) {
                val distance = location.distanceTo(flight)
                if (distance <= 5000) {
                    Toast.makeText(this@MainActivity, "Flight nearby!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun startFusedUpdates() {
        if (!hasLocationPermission()) return

        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000
        ).build()

        fusedClient.requestLocationUpdates(
            request,
            fusedCallback,
            Looper.getMainLooper()
        )
    }
    private fun startLocationFlow() {
        if (isGooglePlayAvailable()) {
            startFusedUpdates()
        } else {
            startLocationUpdates()
        }

        // Instant UI update (cached)
        locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?.let { location ->
                updateUI(location)
            }
    }


    //it is for the gps only implementation
    private fun startLocationUpdates() {
        if (!hasLocationPermission()) return

        val providers = locationManager.getProviders(true)

        when {
            providers.contains(LocationManager.GPS_PROVIDER) -> {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    5000L,
                    5f,
                    locationListener
                )
            }

            providers.contains(LocationManager.PASSIVE_PROVIDER) -> {
                locationManager.requestLocationUpdates(
                    LocationManager.PASSIVE_PROVIDER,
                    5000L,
                    5f,
                    locationListener
                )
            }

            else -> {
                Log.e("FlightCatcher", "No location provider available")
            }
        }
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

