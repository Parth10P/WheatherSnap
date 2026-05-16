package com.parth.weathersnap.ui.camera

import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.parth.weathersnap.utils.Constants
import com.parth.weathersnap.utils.ImageCompressor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * CameraUiState - UI state for the camera screen.
 *
 * @param isCapturing Whether a photo is currently being captured
 * @param capturedImagePath Path to the compressed captured image
 * @param originalSize Original image size in bytes
 * @param compressedSize Compressed image size in bytes
 * @param errorMessage Error message if capture fails
 * @param isCaptured Whether a photo has been successfully captured
 */
data class CameraUiState(
    val isCapturing: Boolean = false,
    val capturedImagePath: String? = null,
    val originalSize: Long = 0L,
    val compressedSize: Long = 0L,
    val errorMessage: String? = null,
    val isCaptured: Boolean = false
)

/**
 * CameraViewModel - Manages photo capture and compression logic.
 *
 * HOW IT WORKS:
 *   1. CameraScreen sets up a CameraX Preview + ImageCapture use case
 *   2. When user taps capture, this ViewModel takes the photo
 *   3. The photo is saved to a temp file, then compressed
 *   4. Compressed path + sizes are stored in state for display
 *   5. User can accept the photo (navigate back with path) or retake
 */
@HiltViewModel
class CameraViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    // ImageCapture use case - shared between ViewModel and Composable
    var imageCapture: ImageCapture? = null

    /**
     * Capture a photo using CameraX ImageCapture.
     *
     * Steps:
     *   1. Create a temp file for the raw photo
     *   2. Use ImageCapture.takePicture() to save to that file
     *   3. Compress the photo using ImageCompressor
     *   4. Update UI state with the compressed path and sizes
     *   5. Delete the original temp file to save space
     *
     * @param context Android context needed for file operations
     */
    fun capturePhoto(context: Context) {
        val capture = imageCapture ?: return

        _uiState.update { it.copy(isCapturing = true, errorMessage = null) }

        // Create a temporary file for the raw photo
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val photoFile = File(context.cacheDir, "TEMP_${timeStamp}.jpg")

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        capture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    // Compress the captured image
                    val result = ImageCompressor.compressImage(
                        context = context,
                        originalPath = photoFile.absolutePath,
                        quality = Constants.COMPRESSION_QUALITY
                    )

                    if (result != null) {
                        _uiState.update {
                            it.copy(
                                isCapturing = false,
                                capturedImagePath = result.compressedPath,
                                originalSize = result.originalSize,
                                compressedSize = result.compressedSize,
                                isCaptured = true
                            )
                        }
                        // Delete the temp original file
                        photoFile.delete()
                    } else {
                        _uiState.update {
                            it.copy(
                                isCapturing = false,
                                errorMessage = "Failed to compress image"
                            )
                        }
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("CameraViewModel", "Photo capture failed", exception)
                    _uiState.update {
                        it.copy(
                            isCapturing = false,
                            errorMessage = "Photo capture failed: ${exception.localizedMessage}"
                        )
                    }
                }
            }
        )
    }

    /**
     * Reset to retake a photo.
     */
    fun retakePhoto() {
        // Delete the previously compressed image
        _uiState.value.capturedImagePath?.let { File(it).delete() }
        _uiState.update {
            CameraUiState() // Reset to initial state
        }
    }

    /**
     * Clear error message.
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
