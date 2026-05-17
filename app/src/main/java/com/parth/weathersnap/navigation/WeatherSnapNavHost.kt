package com.parth.weathersnap.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
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
        composable(route = Screen.Weather.route) {
            WeatherScreen(
                onNavigateToCreateReport = { cityName, temperature, humidity, pressure, windSpeed, weatherCondition ->
                    navController.navigate(
                        Screen.CreateReport.createRoute(
                            cityName = cityName,
                            temperature = temperature,
                            humidity = humidity,
                            pressure = pressure,
                            windSpeed = windSpeed,
                            weatherCondition = weatherCondition
                        )
                    )
                },
                onNavigateToSavedReports = {
                    navController.navigate(Screen.SavedReports.route)
                }
            )
        }

        composable(
            route = Screen.CreateReport.route,
            arguments = listOf(
                navArgument("cityName") { type = NavType.StringType },
                navArgument("temperature") { type = NavType.StringType },
                navArgument("humidity") { type = NavType.StringType },
                navArgument("pressure") { type = NavType.StringType },
                navArgument("windSpeed") { type = NavType.StringType },
                navArgument("weatherCondition") { type = NavType.StringType }
            ),
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(280)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(280)
                )
            }
        ) { backStackEntry ->
            val viewModel: CreateReportViewModel = hiltViewModel()
            val savedState = backStackEntry.savedStateHandle
            val imagePath = savedState.get<String>("imagePath")
            val originalSize = savedState.get<Long>("originalSize")
            val compressedSize = savedState.get<Long>("compressedSize")

            if (imagePath != null && originalSize != null && compressedSize != null) {
                viewModel.setImageData(imagePath, originalSize, compressedSize)
                savedState.remove<String>("imagePath")
                savedState.remove<Long>("originalSize")
                savedState.remove<Long>("compressedSize")
            }

            CreateReportScreen(
                onNavigateToCamera = { navController.navigate(Screen.Camera.route) },
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSavedReports = {
                    navController.navigate(Screen.SavedReports.route) {
                        popUpTo(Screen.Weather.route)
                        launchSingleTop = true
                    }
                },
                viewModel = viewModel
            )
        }

        composable(
            route = Screen.Camera.route,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(280)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(280)
                )
            }
        ) {
            CameraScreen(
                onImageCaptured = { imagePath, originalSize, compressedSize ->
                    navController.previousBackStackEntry?.savedStateHandle?.apply {
                        set("imagePath", imagePath)
                        set("originalSize", originalSize)
                        set("compressedSize", compressedSize)
                    }
                    navController.popBackStack()
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.SavedReports.route,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(280)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(280)
                )
            }
        ) {
            SavedReportsScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
