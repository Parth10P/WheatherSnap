# 🌤️ WeatherSnap

A modern Android weather reporting app built with Jetpack Compose and Material 3 as an internship assignment project.

## 📱 Features

| Feature | Description |
|---------|-------------|
| 🔍 City Search | Autocomplete city search using Open-Meteo Geocoding API |
| 🌡️ Live Weather | Real-time weather data (temperature, humidity, pressure, wind speed) |
| 📝 Create Reports | Save weather reports with personal notes |
| 📸 CameraX Capture | Custom camera screen using CameraX (no camera intent) |
| 🗜️ Image Compression | JPEG compression with original vs compressed size display |
| 💾 Local Storage | Reports saved in Room database with offline access |
| 🎨 Material 3 UI | Polished design with animations, dark mode, Material You support |

## 🏗️ Architecture

```
MVVM (Model-View-ViewModel) with Repository Pattern
```

```
┌──────────────────────────────────────────────────┐
│                    UI Layer                       │
│  WeatherScreen  CreateReportScreen  CameraScreen │
│  SavedReportsScreen                              │
│         ↓ observes StateFlow                     │
│  WeatherVM  CreateReportVM  CameraVM  SavedVM    │
├──────────────────────────────────────────────────┤
│                  Data Layer                       │
│            WeatherRepository                      │
│        (Single Source of Truth)                    │
│         ↙              ↘                         │
│  Remote (Retrofit)   Local (Room DB)              │
│  WeatherApiService   WeatherReportDao             │
│  GeocodingApiService WeatherSnapDatabase          │
├──────────────────────────────────────────────────┤
│                   DI Layer                        │
│  NetworkModule (2 Retrofit instances)             │
│  DatabaseModule (Room + DAO)                      │
│  Hilt → @HiltAndroidApp, @HiltViewModel          │
└──────────────────────────────────────────────────┘
```

## 📂 Package Structure

```
com.parth.weathersnap
├── data
│   ├── remote          → API services & response models
│   │   ├── WeatherApiService.kt
│   │   ├── GeocodingApiService.kt (in same file)
│   │   └── WeatherResponse.kt (all models + weather code mapping)
│   ├── local           → Room DB entities & DAO
│   │   ├── WeatherReportEntity.kt
│   │   ├── WeatherReportDao.kt
│   │   └── WeatherSnapDatabase.kt
│   └── repository      → Repository (mediates remote + local)
│       └── WeatherRepository.kt
├── di                  → Hilt dependency injection modules
│   ├── NetworkModule.kt
│   └── DatabaseModule.kt
├── navigation          → Jetpack Navigation Compose
│   ├── Screen.kt
│   └── WeatherSnapNavHost.kt
├── ui
│   ├── weather         → Weather search + display
│   │   ├── WeatherScreen.kt
│   │   └── WeatherViewModel.kt
│   ├── report          → Create weather report
│   │   ├── CreateReportScreen.kt
│   │   └── CreateReportViewModel.kt
│   ├── camera          → CameraX photo capture
│   │   ├── CameraScreen.kt
│   │   └── CameraViewModel.kt
│   ├── savedreports    → Saved reports list
│   │   ├── SavedReportsScreen.kt
│   │   └── SavedReportsViewModel.kt
│   └── theme           → Material 3 theme
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
├── utils               → Utilities
│   ├── Constants.kt
│   └── ImageCompressor.kt
├── MainActivity.kt
└── WeatherSnapApplication.kt
```

## 🔄 App Flow

```
WeatherScreen (Search City → View Weather)
    │
    ├── FAB (+) → CreateReportScreen (Weather Summary + Notes)
    │                │
    │                ├── Camera Button → CameraScreen (CameraX Preview)
    │                │                      │
    │                │                      └── Capture → Compress → Preview → Use Photo
    │                │                                                    ↓
    │                │                           (passes image path back via savedStateHandle)
    │                │
    │                └── Save Button → Room DB → Navigate Back
    │
    └── Top Bar (📋) → SavedReportsScreen (LazyColumn of Reports)
                           └── Delete individual or Delete All
```

## 🌐 API

**Open-Meteo** (Free, No API Key Required)

| Endpoint | Purpose | Base URL |
|----------|---------|----------|
| Weather | Current conditions by lat/lon | `https://api.open-meteo.com/v1/forecast` |
| Geocoding | City name search/autocomplete | `https://geocoding-api.open-meteo.com/v1/search` |

## 🛠️ Tech Stack

| Technology | Purpose |
|-----------|---------|
| Kotlin | Programming language |
| Jetpack Compose | Declarative UI framework |
| Material 3 | Design system with Material You support |
| Hilt | Dependency injection |
| Retrofit + Gson | HTTP client + JSON parsing |
| Room | Local SQLite database |
| StateFlow | Reactive state management |
| Coroutines | Asynchronous operations |
| Navigation Compose | Screen navigation with argument passing |
| CameraX | Custom camera capture (no intent) |
| Coil | Image loading in Compose |

## 🚀 Setup Instructions

### Prerequisites
- Android Studio Koala or later
- JDK 11+
- Android SDK 26+ (minSdk)
- Physical device or emulator with camera (for CameraX features)

### Steps

1. **Clone the repository:**
   ```bash
   git clone https://github.com/yourusername/WeatherSnap.git
   cd WeatherSnap
   ```

2. **Open in Android Studio:**
   - File → Open → Select the project folder

3. **Sync Gradle:**
   - Android Studio will automatically sync dependencies
   - Or run: `./gradlew build`

4. **Run the app:**
   - Select a device/emulator
   - Click Run (▶️) or: `./gradlew assembleDebug`

5. **No API key needed!**
   - Open-Meteo is free and doesn't require registration

### Build Variants
- **Debug:** `./gradlew assembleDebug`
- **Release:** `./gradlew assembleRelease`
- **Lint Check:** `./gradlew lintDebug`

## 📸 CameraX Implementation

The app uses **CameraX** directly (NOT a camera intent):

1. **Preview:** Live camera viewfinder using `PreviewView` + `AndroidView`
2. **Capture:** `ImageCapture` use case with `CAPTURE_MODE_MINIMIZE_LATENCY`
3. **Compression:** JPEG compression at 50% quality via `Bitmap.compress()`
4. **Size Display:** Shows original vs compressed file sizes

## 💾 Data Persistence

- **Room DB:** Reports stored in `weather_reports` table with auto-generated IDs
- **In-Memory Cache:** City search results cached in `LinkedHashMap` (LRU, max 50 entries)
- **Image Files:** Compressed JPEGs stored in app's internal `filesDir`

## 📐 State Management

Each screen uses a **single UI state data class** observed via `StateFlow`:

```kotlin
data class WeatherUiState(
    val isLoading: Boolean = false,
    val hasWeatherData: Boolean = false,
    val errorMessage: String? = null,
    // ... all screen-specific fields
)
```

ViewModels expose `StateFlow<UiState>`, Composables observe via `collectAsState()`.

## 📄 License

This project is created for educational/internship purposes.
