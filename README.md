# WeatherSnap

WeatherSnap is a modern Android application that allows users to check the weather, create custom weather reports with photo attachments, and save them locally. It utilizes a custom CameraX implementation for capturing photos and compresses the images to optimize storage.

## Tech Stack
* **Language:** Kotlin
* **UI Toolkit:** Jetpack Compose (Material Design 3)
* **Dependency Injection:** Dagger Hilt
* **Local Storage:** Room Database
* **Networking:** Retrofit & OkHttp
* **Camera:** CameraX

## Prerequisites
* Android Studio Ladybug (or newer recommended)
* JDK 17+
* Android SDK (API level 24+)

## Setup Instructions

1. **Open the Project:**
   Open Android Studio, select **File > Open**, and navigate to the `WheatherSnap` directory.

2. **Sync Dependencies:**
   Allow Android Studio to sync the project with Gradle files. This will download all the necessary dependencies (Compose, Hilt, Room, Retrofit, etc.).

## Running the App

### Using Android Studio
1. Connect a physical Android device via USB/Wi-Fi, or start an Android Emulator.
2. Select the `app` configuration in the toolbar.
3. Click the **Run** button (green play icon) or press `Shift + F10`.

### Using the Command Line
Alternatively, you can build and install the debug APK directly from the terminal in the root directory:
```bash
./gradlew installDebug
```

## Features
* **Real-time Weather:** Fetches live weather data using Open-Meteo API.
* **Custom Camera:** Full-screen custom camera built with CameraX.
* **Image Compression:** Compresses captured photos to save space while retaining quality.
* **Local Reports:** Saves weather reports (with images and notes) locally using Room Database.
