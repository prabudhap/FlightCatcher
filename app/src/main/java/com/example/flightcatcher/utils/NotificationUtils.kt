package com.example.flightcatcher.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.flightcatcher.model.FlightModel
import com.example.flightcatcher.R


object NotificationUtils {

    fun showFlightNotification(context: Context, flight: FlightModel) {

        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager

        val channelId = "flight_alerts"

        val channel = NotificationChannel(
            channelId,
            "Flight Alerts",
            NotificationManager.IMPORTANCE_HIGH
        )
        manager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Flight nearby ✈️")
            .setContentText("Flight ${flight.callsign} is within 5 km")
            .setAutoCancel(true)
            .build()

        manager.notify(flight.id.hashCode(), notification)
    }
}
