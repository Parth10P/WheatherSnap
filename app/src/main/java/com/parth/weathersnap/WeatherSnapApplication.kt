package com.parth.weathersnap

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * WeatherSnapApplication - Entry point for Hilt dependency injection.
 *
 * The @HiltAndroidApp annotation triggers Hilt's code generation,
 * including a base class for the application that serves as the
 * application-level dependency container.
 */
@HiltAndroidApp
class WeatherSnapApplication : Application()
