package com.parth.weathersnap.utils

/**
 * Constants - App-wide constant values.
 *
 * Centralized location for API URLs, keys, and other configuration.
 * This makes it easy to update values across the entire app.
 */
object Constants {

    /**
     * Weather API base URL.
     * TODO: Replace with your actual weather API base URL
     * Example for OpenWeatherMap: "https://api.openweathermap.org/data/2.5/"
     */
    const val BASE_URL = "https://api.openweathermap.org/data/2.5/"

    /**
     * Weather API key.
     * TODO: Replace with your actual API key
     * IMPORTANT: In production, store this securely (e.g., in local.properties or BuildConfig)
     */
    const val API_KEY = "YOUR_API_KEY_HERE"

    /** Database name */
    const val DATABASE_NAME = "weathersnap_database"
}
