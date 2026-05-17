package com.parth.weathersnap.ui.savedreports

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.parth.weathersnap.data.local.WeatherReportEntity
import com.parth.weathersnap.ui.components.BorderedActionChip
import com.parth.weathersnap.ui.components.EmptyStateCard
import com.parth.weathersnap.ui.components.FramedImage
import com.parth.weathersnap.ui.components.HeaderCard
import com.parth.weathersnap.ui.components.LoadingPlaceholderCard
import com.parth.weathersnap.ui.components.MetricCard
import com.parth.weathersnap.ui.components.PanelCard
import com.parth.weathersnap.ui.components.WeatherSnapBackground
import com.parth.weathersnap.ui.theme.WeatherSnapDimens
import com.parth.weathersnap.utils.ImageCompressor
import com.parth.weathersnap.utils.formatPressure
import com.parth.weathersnap.utils.formatTemperature
import com.parth.weathersnap.utils.formatTimestamp
import com.parth.weathersnap.utils.formatWindSpeed
import java.io.File

@Composable
fun SavedReportsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SavedReportsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = { Text("Delete all reports?") },
            text = { Text("This removes every saved report and the stored images attached to them.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAllReports()
                        showDeleteAllDialog = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    WeatherSnapBackground(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(horizontal = WeatherSnapDimens.screenPadding, vertical = WeatherSnapDimens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(WeatherSnapDimens.sectionSpacing)
        ) {
            SnackbarHost(hostState = snackbarHostState)

            HeaderCard(
                title = "Saved Reports",
                subtitle = "${uiState.reports.size} report${if (uiState.reports.size == 1) "" else "s"} stored locally",
                actionLabel = "Back",
                onActionClick = onNavigateBack
            )

            if (uiState.reports.isNotEmpty()) {
                BorderedActionChip(
                    text = "Delete All Reports",
                    onClick = { showDeleteAllDialog = true },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            when {
                uiState.isLoading -> {
                    LoadingPlaceholderCard(
                        title = "Loading saved reports",
                        subtitle = "Reading locally stored reports and preparing the image previews."
                    )
                }

                uiState.reports.isEmpty() -> {
                    EmptyStateCard(
                        title = "No saved reports",
                        message = "Create a weather report with notes and a photo to see it listed here."
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(WeatherSnapDimens.sectionSpacing)
                    ) {
                        items(uiState.reports, key = { it.id }) { report ->
                            ReportCard(
                                report = report,
                                onDelete = { viewModel.deleteReport(report) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportCard(
    report: WeatherReportEntity,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete report?") },
            text = { Text("Delete the saved report for ${report.cityName}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    PanelCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            report.imagePath?.let { path ->
                if (File(path).exists()) {
                    FramedImage(
                        modifier = Modifier.size(100.dp)
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(model = File(path)),
                            contentDescription = "Report image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = report.cityName.replace("+", " "),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = report.weatherCondition.replace("+", " "),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier
                            .size(28.dp)
                            .offset(x = 8.dp, y = (-8).dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete report",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatTemperature(report.temperature),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = "${report.humidity}% hum",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = formatWindSpeed(report.windSpeed),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.82f)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatTimestamp(report.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (report.originalImageSize > 0 && report.compressedImageSize > 0) {
                        Text(
                            text = "${ImageCompressor.formatFileSize(report.originalImageSize)} ➔ ${ImageCompressor.formatFileSize(report.compressedImageSize)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
        }

        if (report.description.isNotBlank()) {
            Text(
                text = report.description,
                modifier = Modifier.padding(top = 12.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
