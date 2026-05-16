package com.parth.weathersnap.navigation

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Screen - Sealed class defining all navigation destinations.
 *
 * Each screen has a unique [route] string used by the Navigation component.
 * Using a sealed class ensures type-safety when navigating.
 *
 * WHY NAVIGATION ARGUMENTS:
 *   - WeatherScreen → CreateReportScreen: passes weather data (city, temp, etc.)
 *   - CameraScreen → CreateReportScreen: passes captured image path + sizes
 *   - These are passed as URL-encoded route arguments
 */
sealed class Screen(val route: String) {
    data object Weather : Screen("weather_screen")

    data object CreateReport : Screen(
        "create_report_screen/{cityName}/{temperature}/{humidity}/{pressure}/{windSpeed}/{weatherCondition}"
    ) {
        /**
         * Build the route with actual weather data values.
         * URL-encode strings to handle special characters and spaces.
         */
        fun createRoute(
            cityName: String,
            temperature: Double,
            humidity: Int,
            pressure: Double,
            windSpeed: Double,
            weatherCondition: String
        ): String {
            val encodedCity = URLEncoder.encode(cityName, StandardCharsets.UTF_8.toString())
            val encodedCondition = URLEncoder.encode(weatherCondition, StandardCharsets.UTF_8.toString())
            return "create_report_screen/$encodedCity/$temperature/$humidity/$pressure/$windSpeed/$encodedCondition"
        }
    }

    data object Camera : Screen("camera_screen")
    data object SavedReports : Screen("saved_reports_screen")
}
