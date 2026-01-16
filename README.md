# FlightCatcher
FlightCatcher ✈️

A Kotlin-based Android app that monitors flights in real-time and notifies you when a flight comes within a 5 km radius of your location. Designed to work on both LineageOS (no Google Services) and standard Android devices with Google Play Services.

Features

Dual-mode location tracking:

GPS + last known location (for LineageOS / Google-free devices)

Fused Location + GPS + last known location (for standard Android)

Real-time flight proximity detection (5 km radius).

Pop-up notifications when a nearby flight is detected.

Handles runtime location permissions dynamically.

Optimized for battery efficiency with proper lifecycle management.

Future Plans

Move location tracking to a foreground service to work reliably in the background.

Integrate FlightRadar24 or other flight APIs to get live flight data.

Add user settings to customize detection radius and notification preferences.

Technologies

Kotlin

Android SDK

Google Play Services (Fused Location Provider)

LineageOS-compatible GPS tracking
