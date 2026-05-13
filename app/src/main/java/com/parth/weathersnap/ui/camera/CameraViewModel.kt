package com.parth.weathersnap.ui.camera

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * CameraUiState - Represents the UI state for the Camera screen.
 *
 * @param isCapturing Whether the camera is currently capturing
 * @param capturedImagePath Path to the captured image (nullable)
 * @param errorMessage Error message if something went wrong (nullable)
 */
data class CameraUiState(
    val isCapturing: Boolean = false,
    val capturedImagePath: String? = null,
    val errorMessage: String? = null
)

/**
 * CameraViewModel - ViewModel for the Camera screen.
 *
 * Manages camera state using StateFlow.
 * CameraX logic will be added here when implementing camera functionality.
 */
@HiltViewModel
class CameraViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    // TODO: Add functions to capture photo, handle permissions, etc.
}
