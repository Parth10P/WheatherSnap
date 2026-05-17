# WeatherSnap

WeatherSnap is a Kotlin Android app for searching live weather, creating photo-backed reports, and reviewing saved reports locally. The app is built with Jetpack Compose, MVVM, Hilt, Retrofit, Room, StateFlow, Coroutines, Navigation Compose, CameraX, and Material 3.

## Features

- City autocomplete using the Open-Meteo geocoding API
- Current weather lookup using Open-Meteo forecast data
- In-memory suggestion caching for repeated searches
- Report creation with notes and a custom CameraX capture flow
- JPEG image compression with original and compressed file-size display
- Local persistence with Room and a saved reports screen

## Architecture

```
com.parth.weathersnap
├── data
│   ├── local
│   ├── remote
│   └── repository
├── di
├── navigation
├── ui
│   ├── camera
│   ├── components
│   ├── report
│   ├── savedreports
│   ├── theme
│   └── weather
└── utils
```

## Build

```bash
./gradlew assembleDebug
```

The app requires Android SDK 26 or higher and uses no API keys.
