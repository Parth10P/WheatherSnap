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
    const val WEATHER_BASE_URL = "https://api.open-meteo.com/"
    const val GEOCODING_BASE_URL = "https://geocoding-api.open-meteo.com/"
    const val DATABASE_NAME = "weathersnap_database"
    const val COMPRESSION_QUALITY = 50
    const val MAX_CITY_RESULTS = 6
    const val MAX_CACHED_CITIES = 50
}
