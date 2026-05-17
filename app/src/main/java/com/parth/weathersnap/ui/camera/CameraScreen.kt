package com.parth.weathersnap.ui.camera

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.parth.weathersnap.ui.components.EmptyStateCard
import com.parth.weathersnap.ui.components.FramedImage
import com.parth.weathersnap.ui.components.HeaderCard
import com.parth.weathersnap.ui.components.MetricCard
import com.parth.weathersnap.ui.components.PanelCard
import com.parth.weathersnap.ui.components.PrimaryActionButton
import com.parth.weathersnap.ui.components.SecondaryActionButton
import com.parth.weathersnap.ui.components.WeatherSnapBackground
import com.parth.weathersnap.ui.theme.WeatherSnapDimens
import com.parth.weathersnap.utils.ImageCompressor
import java.io.File

@Composable
fun CameraScreen(
    onImageCaptured: (imagePath: String, originalSize: Long, compressedSize: Long) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: CameraViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.scrim)) {
        AnimatedContent(
            targetState = uiState.hasCapturedImage,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "camera_state",
            modifier = Modifier.fillMaxSize()
        ) { hasCapturedImage ->
            when {
                !hasCameraPermission -> Box(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    PermissionContent {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }

                hasCapturedImage -> PreviewContent(
                    imagePath = uiState.capturedImagePath.orEmpty(),
                    originalSize = uiState.originalSize,
                    compressedSize = uiState.compressedSize,
                    onRetake = viewModel::retakePhoto,
                    onUsePhoto = {
                        uiState.capturedImagePath?.let { path ->
                            onImageCaptured(path, uiState.originalSize, uiState.compressedSize)
                        }
                    },
                    onNavigateBack = onNavigateBack
                )

                else -> CameraCaptureContent(
                    isCapturing = uiState.isCapturing,
                    lifecycleOwner = lifecycleOwner,
                    onCapture = { viewModel.capturePhoto(context) },
                    onImageCaptureReady = { viewModel.imageCapture = it },
                    onNavigateBack = onNavigateBack
                )
            }
        }
        
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 100.dp)
        )
    }
}

@Composable
private fun PermissionContent(onRequestPermission: () -> Unit) {
    PanelCard {
        EmptyStateCard(
            title = "Camera permission required",
            message = "Allow camera access so each report can include a locally captured weather image."
        )
        PrimaryActionButton(
            text = "Allow Camera Access",
            onClick = onRequestPermission,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp)
        )
    }
}

@Composable
private fun CameraCaptureContent(
    isCapturing: Boolean,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    onCapture: () -> Unit,
    onImageCaptureReady: (ImageCapture) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewContext ->
                PreviewView(previewContext).apply {
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { previewView ->
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().apply {
                        surfaceProvider = previewView.surfaceProvider
                    }
                    val imageCapture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build()

                    onImageCaptureReady(imageCapture)

                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageCapture
                    )
                }, ContextCompat.getMainExecutor(context))
            }
        )

        // Top Overlay with Gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.6f),
                            androidx.compose.ui.graphics.Color.Transparent
                        )
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Custom Camera",
                    style = MaterialTheme.typography.headlineMedium,
                    color = androidx.compose.ui.graphics.Color.White,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                )
                
                SecondaryActionButton(
                    text = "Close",
                    onClick = onNavigateBack
                )
            }
        }

        // Bottom Capture Button with Gradient
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            androidx.compose.ui.graphics.Color.Transparent,
                            androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.6f)
                        )
                    )
                )
        ) {
            PrimaryActionButton(
                text = if (isCapturing) "Capturing..." else "Capture",
                onClick = onCapture,
                enabled = !isCapturing,
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(top = 32.dp, bottom = 32.dp, start = 24.dp, end = 24.dp)
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
private fun PreviewContent(
    imagePath: String,
    originalSize: Long,
    compressedSize: Long,
    onRetake: () -> Unit,
    onUsePhoto: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = rememberAsyncImagePainter(model = File(imagePath)),
            contentDescription = "Captured photo preview",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Top Overlay with Gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.6f),
                            androidx.compose.ui.graphics.Color.Transparent
                        )
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Preview",
                    style = MaterialTheme.typography.headlineMedium,
                    color = androidx.compose.ui.graphics.Color.White,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                )
                
                SecondaryActionButton(
                    text = "Close",
                    onClick = onNavigateBack
                )
            }
        }

        // Bottom Overlay with Gradient
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            androidx.compose.ui.graphics.Color.Transparent,
                            androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.8f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(top = 48.dp, bottom = 32.dp, start = 24.dp, end = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CameraMetric(
                        label = "Original",
                        value = ImageCompressor.formatFileSize(originalSize),
                        modifier = Modifier.weight(1f)
                    )
                    CameraMetric(
                        label = "Compressed",
                        value = ImageCompressor.formatFileSize(compressedSize),
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SecondaryActionButton(
                        text = "Retake",
                        onClick = onRetake,
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Refresh
                    )
                    PrimaryActionButton(
                        text = "Use Photo",
                        onClick = onUsePhoto,
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Check
                    )
                }
            }
        }
    }
}

@Composable
private fun CameraMetric(label: String, value: String, modifier: Modifier = Modifier) {
    androidx.compose.material3.Surface(
        modifier = modifier,
        color = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.65f),
        shape = MaterialTheme.shapes.large,
        border = androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.ui.graphics.Color.White.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label,
                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.75f),
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = androidx.compose.ui.graphics.Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
            )
        }
    }
}
