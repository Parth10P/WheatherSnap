package com.parth.weathersnap.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * ImageCompressor - Utility class for compressing captured images.
 *
 * WHY WE COMPRESS:
 *   - CameraX captures full-resolution photos (can be 5-15 MB each)
 *   - Storing uncompressed images would waste device storage
 *   - For a weather report, we don't need ultra-high quality images
 *
 * HOW IT WORKS:
 *   1. Read the original image file
 *   2. Compress it using JPEG compression at the specified quality level
 *   3. Save the compressed image to a new file in app's internal storage
 *   4. Return the path + both file sizes for display
 */
object ImageCompressor {

    /**
     * Data class holding compression results.
     *
     * @param compressedPath Path to the new compressed image file
     * @param originalSize Size of the original image in bytes
     * @param compressedSize Size of the compressed image in bytes
     */
    data class CompressionResult(
        val compressedPath: String,
        val originalSize: Long,
        val compressedSize: Long
    )

    /**
     * Compress an image file and save to app's internal storage.
     *
     * @param context Android context (needed for filesDir access)
     * @param originalPath Path to the original (uncompressed) image
     * @param quality JPEG compression quality (0-100, lower = smaller file)
     * @return CompressionResult with paths and sizes, or null if compression fails
     */
    fun compressImage(
        context: Context,
        originalPath: String,
        quality: Int = Constants.COMPRESSION_QUALITY
    ): CompressionResult? {
        return try {
            val originalFile = File(originalPath)
            val originalSize = originalFile.length()

            // Decode the original bitmap
            val bitmap = BitmapFactory.decodeFile(originalPath) ?: return null

            // Create a unique filename for the compressed image
            val compressedFileName = "report_${UUID.randomUUID()}.jpg"
            val compressedFile = File(context.filesDir, compressedFileName)

            // Compress and save
            FileOutputStream(compressedFile).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            }

            // Clean up the bitmap to free memory
            bitmap.recycle()

            val compressedSize = compressedFile.length()

            CompressionResult(
                compressedPath = compressedFile.absolutePath,
                originalSize = originalSize,
                compressedSize = compressedSize
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Format file size to human-readable string.
     * Example: 1536000 -> "1.5 MB"
     */
    fun formatFileSize(sizeInBytes: Long): String {
        return when {
            sizeInBytes < 1024 -> "$sizeInBytes B"
            sizeInBytes < 1024 * 1024 -> String.format("%.1f KB", sizeInBytes / 1024.0)
            else -> String.format("%.1f MB", sizeInBytes / (1024.0 * 1024.0))
        }
    }
}
