package com.karlitodev.novelize.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.karlitodev.novelize.data.ReaderTheme

/**
 * Dark Material 3 palette using the NoveLize brand purple tones.
 */
private val DarkColorScheme = darkColorScheme(
    primary = BrandPurpleLight,
    onPrimary = Color(0xFF1E1B2E),
    primaryContainer = BrandPurple,
    onPrimaryContainer = Color(0xFFEDE9FE),
    secondary = BrandTeal,
    onSecondary = Color(0xFF003735),
    secondaryContainer = Color(0xFF004F4D),
    onSecondaryContainer = Color(0xFF70F7F5),
    tertiary = BrandAmber,
    onTertiary = Color(0xFF422C00),
    tertiaryContainer = Color(0xFF5F4100),
    onTertiaryContainer = Color(0xFFFFDDB3),
    error = BrandCoral,
    background = BrandDarkBackground,
    onBackground = Color(0xFFE6E1E5),
    surface = BrandCardBackground,
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF2A2735),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF938F99)
)

/**
 * Light Material 3 palette using the NoveLize brand accents.
 */
private val LightColorScheme = lightColorScheme(
    primary = BrandPurple,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE9FE),
    onPrimaryContainer = Color(0xFF21005D),
    secondary = Color(0xFF00897B),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFC8FFF4),
    onSecondaryContainer = Color(0xFF002019),
    tertiary = Color(0xFFE65100),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDBC8),
    onTertiaryContainer = Color(0xFF331100),
    error = Color(0xFFBA1A1A),
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnBackground,
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E)
)

/**
 * Resolved reader theme colors provided through composition locals.
 * Allows reader screens to access theme-aware background, text, and highlight tints.
 */
data class ReaderColors(
    val background: Color = NightBackground,
    val onBackground: Color = NightOnBackground,
    val surface: Color = NightSurface,
    val karaokeHighlight: Color = NightKaraokeHighlight
)

/**
 * CompositionLocal exposing resolved reader theme colors within the Compose tree.
 */
val LocalReaderColors = staticCompositionLocalOf { ReaderColors() }

/**
 * Resolves the reader theme to concrete color values.
 */
fun resolveReaderColors(theme: ReaderTheme): ReaderColors {
    return when (theme) {
        ReaderTheme.LIGHT -> ReaderColors(
            background = LightBackground,
            onBackground = LightOnBackground,
            surface = LightSurface,
            karaokeHighlight = LightKaraokeHighlight
        )
        ReaderTheme.SEPIA -> ReaderColors(
            background = SepiaBackground,
            onBackground = SepiaOnBackground,
            surface = SepiaSurface,
            karaokeHighlight = SepiaKaraokeHighlight
        )
        ReaderTheme.NIGHT -> ReaderColors(
            background = NightBackground,
            onBackground = NightOnBackground,
            surface = NightSurface,
            karaokeHighlight = NightKaraokeHighlight
        )
        ReaderTheme.DYNAMIC -> ReaderColors(
            background = DynamicDarkBackground,
            onBackground = DynamicDarkOnBackground,
            surface = DynamicDarkSurface,
            karaokeHighlight = DynamicKaraokeHighlight
        )
    }
}

/**
 * Root application theme for NoveLize.
 * Adapts between dark, light, and Android 12+ dynamic color schemes.
 * Provides [ReaderColors] via [LocalReaderColors] for reader screen composition.
 */
@Composable
fun NoveLizeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    readerTheme: ReaderTheme = ReaderTheme.DYNAMIC,
    content: @Composable () -> Unit,
) {
    val colorScheme: ColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val readerColors = resolveReaderColors(readerTheme)

    CompositionLocalProvider(LocalReaderColors provides readerColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
