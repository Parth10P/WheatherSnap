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
                title = "Camera",
                subtitle = "Capture a clean weather image, then review compression before attaching it to the report.",
                actionLabel = "Back",
                onActionClick = onNavigateBack
            )

            AnimatedContent(
                targetState = uiState.hasCapturedImage,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "camera_state"
            ) { hasCapturedImage ->
                when {
                    !hasCameraPermission -> PermissionContent {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
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
                        }
                    )

                    else -> CameraCaptureContent(
                        isCapturing = uiState.isCapturing,
                        lifecycleOwner = lifecycleOwner,
                        onCapture = { viewModel.capturePhoto(context) },
                        onImageCaptureReady = { viewModel.imageCapture = it }
                    )
                }
            }
        }
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
    onImageCaptureReady: (ImageCapture) -> Unit
) {
    val context = LocalContext.current

    PanelCard {
        FramedImage(
            modifier = Modifier
                .fillMaxWidth()
                .height(520.dp)
        ) {
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

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 22.dp)
                    .background(
                        color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f),
                        shape = CircleShape
                    )
                    .padding(14.dp)
            ) {
                FilledIconButton(
                    onClick = onCapture,
                    enabled = !isCapturing,
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    if (isCapturing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Capture photo")
                    }
                }
            }
        }
    }
}

@Composable
private fun PreviewContent(
    imagePath: String,
    originalSize: Long,
    compressedSize: Long,
    onRetake: () -> Unit,
    onUsePhoto: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(WeatherSnapDimens.sectionSpacing)
    ) {
        PanelCard {
            FramedImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(model = File(imagePath)),
                    contentDescription = "Captured photo preview",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

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
