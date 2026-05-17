package com.parth.weathersnap.utils

import com.parth.weathersnap.data.remote.GeocodingResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun GeocodingResult.formattedName(): String = buildString {
    append(name)
    admin1?.takeIf { it.isNotBlank() }?.let { append(", $it") }
    if (country.isNotBlank()) {
        append(", ")
        append(country)
    }
}

fun formatTemperature(value: Double): String = "${value.toInt()}°C"

fun formatPressure(value: Double): String = "${value.toInt()} hPa"

fun formatWindSpeed(value: Double): String = String.format(Locale.getDefault(), "%.2f m/s", value)

fun formatTimestamp(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    return formatter.format(Date(timestamp))
}
