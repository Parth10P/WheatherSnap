package com.parth.weathersnap.ui.weather

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.parth.weathersnap.data.remote.GeocodingResult
import com.parth.weathersnap.ui.components.EmptyStateCard
import com.parth.weathersnap.ui.components.HeaderCard
import com.parth.weathersnap.ui.components.InlineStatusCard
import com.parth.weathersnap.ui.components.LoadingPlaceholderCard
import com.parth.weathersnap.ui.components.MetricCard
import com.parth.weathersnap.ui.components.PanelCard
import com.parth.weathersnap.ui.components.PrimaryActionButton
import com.parth.weathersnap.ui.components.WeatherSnapBackground
import com.parth.weathersnap.ui.theme.WeatherSnapDimens
import com.parth.weathersnap.utils.formatPressure
import com.parth.weathersnap.utils.formatTemperature
import com.parth.weathersnap.utils.formatWindSpeed
import com.parth.weathersnap.utils.formattedName

@Composable
fun WeatherScreen(
    onNavigateToCreateReport: (cityName: String, temperature: Double, humidity: Int, pressure: Double, windSpeed: Double, weatherCondition: String) -> Unit,
    onNavigateToSavedReports: () -> Unit,
    viewModel: WeatherViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    WeatherSnapBackground(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = WeatherSnapDimens.screenPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                top = WeatherSnapDimens.screenPadding,
                bottom = WeatherSnapDimens.screenPadding
            ),
            verticalArrangement = Arrangement.spacedBy(WeatherSnapDimens.sectionSpacing)
        ) {
            item { SnackbarHost(hostState = snackbarHostState) }

            item {
                HeaderCard(
                    title = "WeatherSnap",
                    subtitle = "Live weather reports with camera evidence",
                    actionLabel = "Reports",
                    onActionClick = onNavigateToSavedReports
                )
            }

            item {
                SearchPanel(
                    query = uiState.searchQuery,
                    suggestions = uiState.suggestions,
                    showSuggestions = uiState.showSuggestions,
                    isSearching = uiState.isSearching,
                    onQueryChanged = viewModel::onSearchQueryChanged,
                    onSearch = {
                        viewModel.dismissSuggestions()
                        viewModel.onSearchRequested()
                    },
                    onSuggestionSelected = viewModel::onCitySelected
                )
            }

            when (val state = uiState.weatherState) {
                WeatherContentState.Empty -> {
                    item {
                        EmptyStateCard(
                            title = "Search for a city",
                            message = "Enter a city name to load current conditions, prepare a report, and continue to the camera flow."
                        )
                    }
                }

                WeatherContentState.Loading -> {
                    item {
                        LoadingPlaceholderCard(
                            title = "Loading weather conditions",
                            subtitle = "Fetching the latest weather details for your selected city."
                        )
                    }
                }

                is WeatherContentState.Success -> {
                    item {
                        WeatherSummaryCard(
                            weather = state.weather,
                            onCreateReport = {
                                onNavigateToCreateReport(
                                    state.weather.cityName,
                                    state.weather.temperature,
                                    state.weather.humidity,
                                    state.weather.pressure,
                                    state.weather.windSpeed,
                                    state.weather.weatherCondition
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchPanel(
    query: String,
    suggestions: List<GeocodingResult>,
    showSuggestions: Boolean,
    isSearching: Boolean,
    onQueryChanged: (String) -> Unit,
    onSearch: () -> Unit,
    onSuggestionSelected: (GeocodingResult) -> Unit
) {
    PanelCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChanged,
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.bodyLarge,
                label = { Text("City") },
                placeholder = { Text("Search by city name") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                trailingIcon = {
                    if (isSearching) {
                        CircularProgressIndicator(
                            modifier = Modifier.width(18.dp),
                            strokeWidth = 2.dp
                        )
                    }
                },
                shape = MaterialTheme.shapes.large,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                    focusedContainerColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.18f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.12f)
                )
            )

            PrimaryActionButton(
                text = "Search",
                onClick = onSearch
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        InlineStatusCard(
            title = "Suggestions",
            message = if (query.length >= 3) {
                "Matching cities appear below. Tap one to fetch live weather."
            } else {
                "Enter at least 3 letters to start city suggestions."
            },
            modifier = Modifier.padding(top = 14.dp)
        )

        AnimatedVisibility(
            visible = showSuggestions,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier = Modifier.padding(top = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                suggestions.forEach { city ->
                    SuggestionRow(city = city, onClick = { onSuggestionSelected(city) })
                }
            }
        }
    }
}

@Composable
private fun SuggestionRow(
    city: GeocodingResult,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.12f)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(
                text = city.name.replace("+", " "),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = city.formattedName().replace("+", " "),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun WeatherSummaryCard(
    weather: WeatherDetails,
    onCreateReport: () -> Unit
) {
    PanelCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = weather.cityName.replace("+", " "),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = weather.weatherCondition.replace("+", " "),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            MetricCard(
                label = "Temperature",
                value = formatTemperature(weather.temperature),
                accent = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.width(132.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                label = "Humidity",
                value = "${weather.humidity}%",
                accent = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f),
                icon = Icons.Default.WaterDrop
            )
            MetricCard(
                label = "Wind",
                value = formatWindSpeed(weather.windSpeed),
                accent = MaterialTheme.colorScheme.secondary.copy(alpha = 0.82f),
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Air
            )
            MetricCard(
                label = "Pressure",
                value = formatPressure(weather.pressure),
                accent = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.94f),
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Compress
            )
        }

        InlineStatusCard(
            title = "Report readiness",
            message = "The current weather snapshot is ready. Continue to create a report with notes and a camera image.",
            modifier = Modifier.padding(top = 18.dp)
        )

        PrimaryActionButton(
            text = "Create Report",
            onClick = onCreateReport,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp),
            icon = Icons.AutoMirrored.Filled.ArrowForward
        )
    }
}
