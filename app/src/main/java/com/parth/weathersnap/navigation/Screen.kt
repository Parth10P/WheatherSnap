package com.parth.weathersnap.navigation

/**
 * Screen - Sealed class defining all navigation destinations in the app.
 *
 * Each screen has a unique [route] string used by the Navigation component.
 * Using a sealed class ensures type-safety when navigating between screens.
 */
sealed class Screen(val route: String) {
    data object Weather : Screen("weather_screen")
    data object CreateReport : Screen("create_report_screen")
    data object Camera : Screen("camera_screen")
    data object SavedReports : Screen("saved_reports_screen")
}
