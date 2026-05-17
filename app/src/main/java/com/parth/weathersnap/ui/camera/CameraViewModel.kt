package com.parth.weathersnap.ui.camera

import android.content.Context
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parth.weathersnap.utils.Constants
import com.parth.weathersnap.utils.ImageCompressor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class CameraUiState(
    val isCapturing: Boolean = false,
    val capturedImagePath: String? = null,
    val originalSize: Long = 0L,
    val compressedSize: Long = 0L,
    val errorMessage: String? = null
) {
    val hasCapturedImage: Boolean = capturedImagePath != null
}

@HiltViewModel
class CameraViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    var imageCapture: ImageCapture? = null

    fun capturePhoto(context: Context) {
        val capture = imageCapture ?: run {
            _uiState.update { it.copy(errorMessage = "Camera is not ready yet.") }
            return
        }

        val photoFile = File(
            context.cacheDir,
            "capture_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.jpg"
        )

        _uiState.update { it.copy(isCapturing = true, errorMessage = null) }

        capture.takePicture(
            ImageCapture.OutputFileOptions.Builder(photoFile).build(),
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    viewModelScope.launch {
                        val result = withContext(Dispatchers.IO) {
                            ImageCompressor.compressImage(
                                context = context,
                                originalPath = photoFile.absolutePath,
                                quality = Constants.COMPRESSION_QUALITY
                            )
                        }

                        photoFile.delete()

                        if (result == null) {
                            _uiState.update {
                                it.copy(
                                    isCapturing = false,
                                    errorMessage = "Unable to compress the captured image."
                                )
                            }
                        } else {
                            _uiState.update {
                                it.copy(
                                    isCapturing = false,
                                    capturedImagePath = result.compressedPath,
                                    originalSize = result.originalSize,
                                    compressedSize = result.compressedSize
                                )
                            }
                        }
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    photoFile.delete()
                    _uiState.update {
                        it.copy(
                            isCapturing = false,
                            errorMessage = exception.localizedMessage ?: "Photo capture failed."
                        )
                    }
                }
            }
        )
    }

    fun retakePhoto() {
        _uiState.value.capturedImagePath?.let { File(it).delete() }
        _uiState.value = CameraUiState()
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
