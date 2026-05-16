package com.parth.weathersnap.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.parth.weathersnap.ui.camera.CameraScreen
import com.parth.weathersnap.ui.report.CreateReportScreen
import com.parth.weathersnap.ui.report.CreateReportViewModel
import com.parth.weathersnap.ui.savedreports.SavedReportsScreen
import com.parth.weathersnap.ui.weather.WeatherScreen

/**
 * WeatherSnapNavHost - Central navigation graph for the entire app.
 *
 * DATA FLOW:
 *   1. WeatherScreen → searchs city → shows weather → FAB navigates to CreateReport
 *      (passes weather data as route arguments)
 *   2. CreateReportScreen → camera button → CameraScreen
 *      (image path passed back via savedStateHandle)
 *   3. CameraScreen → captures + compresses → navigates back with image data
 *   4. CreateReportScreen → saves to Room → navigates back to Weather
 *   5. WeatherScreen → top bar icon → SavedReportsScreen
 *
 * WHY savedStateHandle for image data:
 *   Navigation Compose lets us pass results back to the PREVIOUS screen
 *   via the previous back stack entry's savedStateHandle. This avoids
 *   complex shared ViewModels or result launchers.
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
        // ==================== Weather Screen ====================
        composable(route = Screen.Weather.route) {
            WeatherScreen(
                onNavigateToCreateReport = { cityName, temperature, humidity, pressure, windSpeed, weatherCondition ->
                    navController.navigate(
                        Screen.CreateReport.createRoute(
                            cityName, temperature, humidity, pressure, windSpeed, weatherCondition
                        )
                    )
                },
                onNavigateToSavedReports = {
                    navController.navigate(Screen.SavedReports.route)
                }
            )
        }

        // ==================== Create Report Screen ====================
        composable(
            route = Screen.CreateReport.route,
            arguments = listOf(
                navArgument("cityName") { type = NavType.StringType },
                navArgument("temperature") { type = NavType.StringType },
                navArgument("humidity") { type = NavType.StringType },
                navArgument("pressure") { type = NavType.StringType },
                navArgument("windSpeed") { type = NavType.StringType },
                navArgument("weatherCondition") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            // Get the ViewModel (SavedStateHandle auto-populates from nav arguments)
            val viewModel: CreateReportViewModel = hiltViewModel()

            // Check if CameraScreen returned image data
            val savedState = backStackEntry.savedStateHandle
            val imagePath = savedState.get<String>("imagePath")
            val originalSize = savedState.get<Long>("originalSize")
            val compressedSize = savedState.get<Long>("compressedSize")

            if (imagePath != null && originalSize != null && compressedSize != null) {
                viewModel.setImageData(imagePath, originalSize, compressedSize)
                // Clear to avoid re-processing on recomposition
                savedState.remove<String>("imagePath")
                savedState.remove<Long>("originalSize")
                savedState.remove<Long>("compressedSize")
            }

            CreateReportScreen(
                onNavigateToCamera = {
                    navController.navigate(Screen.Camera.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                viewModel = viewModel
            )
        }

        // ==================== Camera Screen ====================
        composable(route = Screen.Camera.route) {
            CameraScreen(
                onImageCaptured = { imagePath, originalSize, compressedSize ->
                    // Pass image data back to CreateReportScreen
                    navController.previousBackStackEntry?.savedStateHandle?.apply {
                        set("imagePath", imagePath)
                        set("originalSize", originalSize)
                        set("compressedSize", compressedSize)
                    }
                    navController.popBackStack()
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // ==================== Saved Reports Screen ====================
        composable(route = Screen.SavedReports.route) {
            SavedReportsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
