package com.parth.weathersnap.ui.report

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import com.parth.weathersnap.ui.components.BorderedActionChip
import com.parth.weathersnap.ui.components.EmptyStateCard
import com.parth.weathersnap.ui.components.FramedImage
import com.parth.weathersnap.ui.components.HeaderCard
import com.parth.weathersnap.ui.components.MetricCard
import com.parth.weathersnap.ui.components.PanelCard
import com.parth.weathersnap.ui.components.WeatherSnapBackground
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SnackbarHost(hostState = snackbarHostState)

            HeaderCard(
                title = "Create Report",
                subtitle = "Attach notes and camera evidence for ${uiState.cityName}",
                actionLabel = "Back",
                onActionClick = onNavigateBack
            )

            WeatherSummarySection(uiState)

            PanelCard {
                Text(
                    text = "Notes",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = viewModel::onDescriptionChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    placeholder = {
                        Text("Describe what the weather looks like and anything notable on the ground.")
                    },
                    shape = MaterialTheme.shapes.large,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedContainerColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.18f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.14f)
                    )
                )
            }

            PhotoSection(
                imagePath = uiState.imagePath,
                originalSize = uiState.originalImageSize,
                compressedSize = uiState.compressedImageSize,
                onCapture = onNavigateToCamera
            )

            Button(
                onClick = viewModel::saveReport,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !uiState.isLoading,
                shape = MaterialTheme.shapes.extraLarge,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Report", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
private fun WeatherSummarySection(uiState: CreateReportUiState) {
    PanelCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = uiState.cityName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = uiState.weatherCondition,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            MetricCard(
                label = "Temperature",
                value = formatTemperature(uiState.temperature),
                accent = MaterialTheme.colorScheme.tertiary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
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

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
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
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                EmptyStateCard(
                    title = "No photo attached",
                    message = "Capture a photo from the camera screen to include evidence with the report."
                )
                BorderedActionChip(
                    text = "Open Camera",
                    onClick = onCapture,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            PanelCard {
                Text(
                    text = "Attached Photo",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))
                FramedImage(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(model = File(path)),
                        contentDescription = "Attached report photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                Spacer(modifier = Modifier.height(14.dp))
                BorderedActionChip(
                    text = "Retake Photo",
                    onClick = onCapture,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
