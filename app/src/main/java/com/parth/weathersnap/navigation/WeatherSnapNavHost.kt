package com.parth.weathersnap.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.parth.weathersnap.ui.camera.CameraScreen
import com.parth.weathersnap.ui.report.CreateReportScreen
import com.parth.weathersnap.ui.savedreports.SavedReportsScreen
import com.parth.weathersnap.ui.weather.WeatherScreen

/**
 * WeatherSnapNavHost - Central navigation graph for the entire app.
 *
 * Defines all composable destinations and handles navigation
 * between screens. The WeatherScreen is the start destination.
 *
 * @param modifier Modifier passed from the parent (e.g., Scaffold padding)
 * @param navController The NavHostController managing navigation state
 */
@Composable
fun WeatherSnapNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Weather.route,
        modifier = modifier
    ) {
        // Weather Screen - Home / Start destination
        composable(route = Screen.Weather.route) {
            WeatherScreen(
                onNavigateToCreateReport = {
                    navController.navigate(Screen.CreateReport.route)
                },
                onNavigateToSavedReports = {
                    navController.navigate(Screen.SavedReports.route)
                }
            )
        }

        // Create Report Screen
        composable(route = Screen.CreateReport.route) {
            CreateReportScreen(
                onNavigateToCamera = {
                    navController.navigate(Screen.Camera.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Camera Screen
        composable(route = Screen.Camera.route) {
            CameraScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Saved Reports Screen
        composable(route = Screen.SavedReports.route) {
            SavedReportsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
