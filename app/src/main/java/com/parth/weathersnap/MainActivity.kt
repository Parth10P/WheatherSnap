package com.parth.weathersnap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.parth.weathersnap.navigation.WeatherSnapNavHost
import com.parth.weathersnap.ui.theme.WheatherSnapTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * MainActivity - Single activity that hosts the Compose Navigation graph.
 *
 * @AndroidEntryPoint enables Hilt dependency injection in this activity.
 * All screens are managed via Jetpack Compose Navigation.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WheatherSnapTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WeatherSnapNavHost(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}