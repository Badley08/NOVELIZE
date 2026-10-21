package com.karlitodev.novelize.theme

import androidx.compose.ui.graphics.Color

// Core Brand Palette
val BrandPurple = Color(0xFF6C5CE7)
val BrandPurpleLight = Color(0xFFA29BFE)
val BrandPurpleDark = Color(0xFF4834D4)
val BrandTeal = Color(0xFF00CEC9)
val BrandAmber = Color(0xFFFFB142)
val BrandCoral = Color(0xFFFF7675)
val BrandEmerald = Color(0xFF2ED573)
val BrandNavy = Color(0xFF1E1B2E)
val BrandDarkBackground = Color(0xFF13111C)
val BrandCardBackground = Color(0xFF1E1B2E)

// Material 3 Dark Palette
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

// Material 3 Light Palette
val Purple40 = Color(0xFF6650A4)
val PurpleGrey40 = Color(0xFF625B71)
val Pink40 = Color(0xFF7D5260)

// Reader Themes: Light
val LightBackground = Color(0xFFFAFAF9)
val LightOnBackground = Color(0xFF1C1917)
val LightSurface = Color(0xFFFFFFFF)
val LightKaraokeHighlight = Color(0x336C5CE7)

// Reader Themes: Sepia (Paper texture warmth)
val SepiaBackground = Color(0xFFF4ECD8)
val SepiaOnBackground = Color(0xFF433422)
val SepiaSurface = Color(0xFFEFE6CE)
val SepiaKaraokeHighlight = Color(0x44D4A373)

// Reader Themes: Night (True AMOLED & dark reading)
val NightBackground = Color(0xFF121214)
val NightOnBackground = Color(0xFFE4E4E7)
val NightSurface = Color(0xFF1E1E22)
val NightKaraokeHighlight = Color(0x556C5CE7)

// Dynamic Cover Reader Theme Default
val DynamicDarkBackground = Color(0xFF13111C)
val DynamicDarkOnBackground = Color(0xFFEDE9FE)
val DynamicDarkSurface = Color(0xFF1E1B2E)
val DynamicKaraokeHighlight = Color(0x556C5CE7)

// Text Annotation Highlight Palette
val HighlightYellow = Color(0xFFFFEB3B)
val HighlightEmerald = Color(0xFF69F0AE)
val HighlightCyan = Color(0xFF40C4FF)
val HighlightCoral = Color(0xFFFF8A80)
val HighlightPurple = Color(0xFFE040FB)

/**
 * Four-value tuple representing resolved reader theme colors:
 * background, text on background, karaoke highlight tint, and surface bars.
 */
data class Quad<out A, out B, out C, out D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
