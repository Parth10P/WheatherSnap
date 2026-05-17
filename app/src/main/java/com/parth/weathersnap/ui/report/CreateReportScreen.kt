package com.parth.weathersnap.ui.report

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.parth.weathersnap.ui.components.EmptyStateCard
import com.parth.weathersnap.ui.components.FramedImage
import com.parth.weathersnap.ui.components.HeaderCard
import com.parth.weathersnap.ui.components.InlineStatusCard
import com.parth.weathersnap.ui.components.MetricCard
import com.parth.weathersnap.ui.components.PanelCard
import com.parth.weathersnap.ui.components.PrimaryActionButton
import com.parth.weathersnap.ui.components.SecondaryActionButton
import com.parth.weathersnap.ui.components.WeatherSnapBackground
import com.parth.weathersnap.ui.theme.WeatherSnapDimens
import com.parth.weathersnap.utils.ImageCompressor
import com.parth.weathersnap.utils.formatPressure
import com.parth.weathersnap.utils.formatTemperature
import com.parth.weathersnap.utils.formatWindSpeed
import java.io.File

@Composable
fun CreateReportScreen(
    onNavigateToCamera: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToSavedReports: () -> Unit,
    viewModel: CreateReportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateToSavedReports()
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
                vertical = WeatherSnapDimens.screenPadding
            ),
            verticalArrangement = Arrangement.spacedBy(WeatherSnapDimens.sectionSpacing)
        ) {
            item { SnackbarHost(hostState = snackbarHostState) }

            item {
                HeaderCard(
                    title = "Create Report",
                    subtitle = "Attach notes and camera evidence for ${uiState.cityName}",
                    actionLabel = "Back",
                    onActionClick = onNavigateBack
                )
            }

            item { WeatherSummarySection(uiState) }

            item {
                PanelCard {
                    Text(
                        text = "Notes",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    InlineStatusCard(
                        title = "What to include",
                        message = "Describe the scene, visibility, cloud cover, rainfall, or any detail that helps the report feel complete.",
                        modifier = Modifier.padding(top = 12.dp)
                    )
                    OutlinedTextField(
                        value = uiState.description,
                        onValueChange = viewModel::onDescriptionChanged,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp),
                        minLines = 5,
                        placeholder = {
                            Text("Example: Light wind, scattered clouds, roads are dry and visibility is clear.")
                        },
                        shape = MaterialTheme.shapes.large,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                            focusedContainerColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.18f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.12f)
                        )
                    )
                }
            }

            item {
                PhotoSection(
                    imagePath = uiState.imagePath,
                    originalSize = uiState.originalImageSize,
                    compressedSize = uiState.compressedImageSize,
                    onCapture = onNavigateToCamera
                )
            }

            item {
                PrimaryActionButton(
                    text = if (uiState.isLoading) "Saving Report" else "Save Report",
                    onClick = viewModel::saveReport,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading,
                    icon = if (uiState.isLoading) null else Icons.Default.Save
                )
            }
        }
    }
}

@Composable
private fun WeatherSummarySection(uiState: CreateReportUiState) {
    PanelCard {
        Text(
            text = "Weather Summary",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = uiState.cityName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = uiState.weatherCondition,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            MetricCard(
                label = "Temperature",
                value = formatTemperature(uiState.temperature),
                accent = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.width(132.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                label = "Humidity",
                value = "${uiState.humidity}%",
                accent = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f),
                icon = Icons.Default.WaterDrop
            )
            MetricCard(
                label = "Pressure",
                value = formatPressure(uiState.pressure),
                accent = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.92f),
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Compress
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                label = "Wind",
                value = formatWindSpeed(uiState.windSpeed),
                accent = MaterialTheme.colorScheme.secondary.copy(alpha = 0.82f),
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Air
            )
            MetricCard(
                label = "Condition",
                value = uiState.weatherCondition,
                accent = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Thermostat
            )
        }
    }
}

@Composable
private fun PhotoSection(
    imagePath: String?,
    originalSize: Long,
    compressedSize: Long,
    onCapture: () -> Unit
) {
    AnimatedContent(
        targetState = imagePath,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "photo_section"
    ) { path ->
        if (path == null) {
            PanelCard {
                EmptyStateCard(
                    title = "No photo attached",
                    message = "Capture a weather photo before saving so the report includes visual evidence."
                )
                SecondaryActionButton(
                    text = "Open Camera",
                    onClick = onCapture,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp),
                    icon = Icons.Default.CameraAlt
                )
            }
        } else {
            PanelCard {
                Text(
                    text = "Attached Photo",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                FramedImage(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                        .height(WeatherSnapDimens.imageHeight)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(model = File(path)),
                        contentDescription = "Attached report photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        label = "Original",
                        value = ImageCompressor.formatFileSize(originalSize),
                        accent = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.86f),
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        label = "Compressed",
                        value = ImageCompressor.formatFileSize(compressedSize),
                        accent = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    )
                }

                SecondaryActionButton(
                    text = "Retake Photo",
                    onClick = onCapture,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp),
                    icon = Icons.Default.CameraAlt
                )
            }
        }
    }
}
