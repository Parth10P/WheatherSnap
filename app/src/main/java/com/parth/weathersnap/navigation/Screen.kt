package com.parth.weathersnap.navigation

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class Screen(val route: String) {
    data object Weather : Screen("weather")

    data object CreateReport : Screen(
        "create_report/{cityName}/{temperature}/{humidity}/{pressure}/{windSpeed}/{weatherCondition}"
    ) {
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
            return "create_report/$encodedCity/$temperature/$humidity/$pressure/$windSpeed/$encodedCondition"
        }
    }

    data object Camera : Screen("camera")
    data object SavedReports : Screen("saved_reports")
}
