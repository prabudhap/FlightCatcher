package com.example.flightcatcher.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.flightcatcher.R
import com.google.android.gms.location.*
import android.os.Looper
import com.google.android.gms.location.Priority


class FlightMonitorService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    override fun onCreate() {
        super.onCreate()

        startForeground(1, createNotification())

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                for (location in result.locations) {
                    Log.d(
                        "Flightcatcher",
                        "Lat=${location.latitude}, Lon=${location.longitude}"
                    )
                }
            }
        }

        startLocationUpdates()
    }

    private fun startLocationUpdates() {
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5_000L
        )
            .setMinUpdateIntervalMillis(3_000L)
            .build()

        fusedLocationClient.requestLocationUpdates(
            request,
            locationCallback,
            Looper.getMainLooper()
        )
    }



    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotification(): Notification {
        val channelId = "flightcatcher_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Flightcatcher",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Flightcatcher running")
            .setContentText("Tracking your location")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
    }
}
