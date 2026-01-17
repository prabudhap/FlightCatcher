# FlightCatcher ✈️

FlightCatcher is a Kotlin-based Android application that tracks your real-time location and notifies you when an aircraft passes within a **5 km radius** of your position.

The app is designed to work on **both Google-enabled Android devices and Google-free ROMs like LineageOS**, using a flexible location strategy.

---

## 🔍 What Problem This Solves

Most flight-tracking apps:
- Require Google Play Services
- Stop working reliably in the background
- Do not provide proximity-based alerts

FlightCatcher focuses on **location-based flight proximity detection**, even when:
- The screen is off
- The device is running LineageOS or de-Googled Android
- Location updates need to work continuously

---

## 🚀 Features

- 📍 Real-time latitude & longitude updates
- 🔄 Dual location provider strategy:
  - **GPS + Last Known Location** (LineageOS / Google-free devices)
  - **Fused Location + GPS + Last Known Location** (Standard Android)
- 📡 Continuous location monitoring
- 🔔 Popup notification when a flight enters a 5 km radius
- 🔐 Runtime permission handling
- ⚡ Lifecycle-aware location updates

---

## 🧠 Location Strategy

| Device Type | Location Method |
|------------|-----------------|
| LineageOS / No GMS | GPS_PROVIDER + Last Known Location |
| Standard Android | Fused Location Provider + GPS fallback |

This ensures:
- Compatibility across ROMs
- Faster first location fix
- Graceful fallback when providers are unavailable

---

## 🛠 Tech Stack

- **Language:** Kotlin
- **Platform:** Android SDK
- **Location APIs:**
  - `LocationManager` (GPS)
  - `FusedLocationProviderClient` (Google Play Services)
- **Architecture:** Activity-based (service planned)
- **Target Devices:** Android phones, LineageOS devices

---

## 📦 Installation

### Clone the repository
```bash
git clone https://github.com/prabudhap/FlightCatcher.git
