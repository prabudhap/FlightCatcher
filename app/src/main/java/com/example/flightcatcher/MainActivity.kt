package com.example.flightcatcher

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
import com.example.flightcatcher.utils.NotificationUtils
import com.example.flightcatcher.viewmodel.FlightViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.flightcatcher.model.FlightModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest


class MainActivity : ComponentActivity() {

    private lateinit var locationManager: LocationManager
    private lateinit var fusedClient: FusedLocationProviderClient
    private lateinit var binding: ActivityMainBinding
    private lateinit var flightViewModel: FlightViewModel
    private val flightList = mutableListOf<FlightModel>()
    
    // Flight update interval (30 seconds)
    private val FLIGHT_UPDATE_INTERVAL_MS = 30_000L






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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fusedClient = LocationServices.getFusedLocationProviderClient(this)
        flightViewModel = ViewModelProvider(this)[FlightViewModel::class.java]

        // Observe flights from ViewModel
        observeFlights()
        
        // Start fetching flights periodically
        startFlightUpdates()

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
        stopLocationUpdates()
    }

    override fun onStop() {
        super.onStop()
        stopLocationUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
    }

    private fun stopLocationUpdates() {
        if (::locationManager.isInitialized) {
            try {
                locationManager.removeUpdates(locationListener)
            } catch (e: Exception) {
                Log.e("FlightCatcher", "Error removing location updates", e)
            }
        }
        if (::fusedClient.isInitialized) {
            try {
                fusedClient.removeLocationUpdates(fusedCallback)
            } catch (e: Exception) {
                Log.e("FlightCatcher", "Error removing fused location updates", e)
            }
        }
    }

    private fun requestLocationPermission() {
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }
    
    /**
     * Observe flights from ViewModel and update local list
     */
    private fun observeFlights() {
        lifecycleScope.launch {
            flightViewModel.flights.collectLatest { flights ->
                flightList.clear()
                flightList.addAll(flights)
                Log.d("FlightCatcher", "Updated flight list: ${flights.size} flights")
            }
        }
    }
    
    /**
     * Start periodic flight updates from API
     */
    private fun startFlightUpdates() {
        // Fetch immediately
        flightViewModel.fetchFlights()
        
        // Then fetch periodically
        lifecycleScope.launch {
            while (true) {
                delay(FLIGHT_UPDATE_INTERVAL_MS)
                flightViewModel.fetchFlights()
            }
        }
    }

    private fun handleNearbyFlights(location: Location) {
        flightViewModel.detectNearbyFlights(
            userLat = location.latitude,
            userLon = location.longitude,
            flights = flightList
        ) { flight ->
            NotificationUtils.showFlightNotification(this, flight)
        }
    }

    private val fusedCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation ?: return

            val lat = location.latitude
            val lon = location.longitude

            Log.d("FlightCatcher", "FUSED → Lat=$lat, Lon=$lon")

            updateUI(location)
            handleNearbyFlights(location)
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
            val currentLat = location.latitude
            val currentLon = location.longitude

            
            flightViewModel.detectNearbyFlights(
                userLat = currentLat,
                userLon = currentLon,
                flights = flightList
            ) { flight ->
                NotificationUtils.showFlightNotification(this@MainActivity, flight)

            }
        }

    }
}

