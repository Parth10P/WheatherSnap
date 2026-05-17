package com.parth.weathersnap.ui.theme

import androidx.compose.ui.graphics.Color

// ── Primary: lime-yellow buttons / accents ──
val PrimaryLight = Color(0xFFD9E89A)
val OnPrimaryLight = Color(0xFF0F1A03)          // very dark green – high contrast on lime buttons
val PrimaryContainerLight = Color(0xFF4D5A1C)
val OnPrimaryContainerLight = Color(0xFFF2F8D5)

// ── Secondary: teal-ish accents ──
val SecondaryLight = Color(0xFFC0E0D8)
val OnSecondaryLight = Color(0xFF0D1F19)
val SecondaryContainerLight = Color(0xFF33534B)
val OnSecondaryContainerLight = Color(0xFFE5F4EF)

// ── Tertiary: olive-green accents ──
val TertiaryLight = Color(0xFF9AB86F)
val OnTertiaryLight = Color(0xFF0F1A05)
val TertiaryContainerLight = Color(0xFF495D2C)
val OnTertiaryContainerLight = Color(0xFFE9F3D6)

// ── Surfaces & backgrounds ──
val ErrorLight = Color(0xFFFFB4AB)
val BackgroundLight = Color(0xFF18210F)
val SurfaceLight = Color(0xFF11180D)
val OnSurfaceLight = Color(0xFFF5F7EE)           // bright off-white – main readable text
val SurfaceVariantLight = Color(0xFF414235)
val OnSurfaceVariantLight = Color(0xFFDCDFCF)    // brighter muted white – secondary text
val OutlineLight = Color(0xFF8A8E7C)             // brighter outline for borders / labels

// ── Dark scheme mirrors the "light" values (always-dark app) ──
val PrimaryDark = PrimaryLight
val OnPrimaryDark = OnPrimaryLight
val PrimaryContainerDark = PrimaryContainerLight
val OnPrimaryContainerDark = OnPrimaryContainerLight

val SecondaryDark = SecondaryLight
val OnSecondaryDark = OnSecondaryLight
val SecondaryContainerDark = SecondaryContainerLight
val OnSecondaryContainerDark = OnSecondaryContainerLight

val TertiaryDark = TertiaryLight
val OnTertiaryDark = OnTertiaryLight
val TertiaryContainerDark = TertiaryContainerLight
val OnTertiaryContainerDark = OnTertiaryContainerLight

val ErrorDark = ErrorLight
val BackgroundDark = BackgroundLight
val SurfaceDark = SurfaceLight
val OnSurfaceDark = OnSurfaceLight
val SurfaceVariantDark = SurfaceVariantLight
val OnSurfaceVariantDark = OnSurfaceVariantLight
val OutlineDark = OutlineLight
