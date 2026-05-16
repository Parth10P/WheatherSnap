package com.parth.weathersnap.utils

/**
 * Constants - App-wide constant values.
 *
 * Open-Meteo is a free, open-source weather API that does NOT require an API key.
 * We use two base URLs:
 *   1. Weather API - for current weather data (takes latitude/longitude)
 *   2. Geocoding API - for city name autocomplete (search cities by name)
 */
object Constants {

    /** Base URL for Open-Meteo weather API */
    const val WEATHER_BASE_URL = "https://api.open-meteo.com/"

    /** Base URL for Open-Meteo Geocoding API (city search) */
    const val GEOCODING_BASE_URL = "https://geocoding-api.open-meteo.com/"

    /** Database name */
    const val DATABASE_NAME = "weathersnap_database"

    /** Image compression quality (0-100) */
    const val COMPRESSION_QUALITY = 50

    /** Maximum number of cached city suggestions */
    const val MAX_CACHED_CITIES = 50
}
