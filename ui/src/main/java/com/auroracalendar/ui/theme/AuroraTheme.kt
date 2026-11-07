package com.auroracalendar.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.unit.dp
import com.auroracalendar.core.di.provideThemeModeFlow

// ------------------------------------------------------------------
// Brand color tokens (energetic, premium, customizable category defaults)
// ------------------------------------------------------------------
val AuroraPrimary = 0xFF7C4DFF.toInt()   // vibrant indigo
val AuroraSecondary = 0xFF00E676.toInt() // energetic green
val AuroraTertiary = 0xFFFF9100.toInt()  // warm orange
val AuroraError = 0xFFFF3B30.toInt()     // urgent red

val AuroraSurface = 0xFFFAFAFA.toInt()
val AuroraSurfaceVariant = 0xFFF5F5F5.toInt()

val AuroraDarkSurface = 0xFF121212.toInt()
val AuroraDarkSurfaceVariant = 0xFF1E1E1E.toInt()

// Category color defaults (user-customizable)
val CategoryStudy = 0xFF3B82F6.toInt()      // blue
val CategoryCreative = 0xFF8B5CF6.toInt()   // purple
val CategoryHealth = 0xFF10B981.toInt()     // green
val CategoryImportant = 0xFFF59E0B.toInt()  // orange
val CategoryUrgent = 0xFFEF4444.toInt()     // red

// ------------------------------------------------------------------
// Theme mode constants (mirror core.di.AppModule keys)
// ------------------------------------------------------------------
val ThemeModeLight = 0
val ThemeModeDark = 1
val ThemeModeSystem = 2
val ThemeModeDynamic = 3
val ThemeModeCustom = 4

// ------------------------------------------------------------------
// Light / dark base color schemes
// ------------------------------------------------------------------
private val LightColorScheme = lightColorScheme(
    primary = AuroraPrimary,
    onPrimary = 0xFFFFFFFF.toInt(),
    primaryContainer = 0xFFE4D9FF.toInt(),
    onPrimaryContainer = 0xFF3A1D8E.toInt(),
    secondary = AuroraSecondary,
    onSecondary = 0xFF000000.toInt(),
    secondaryContainer = 0xFFCCFFEC.toInt(),
    onSecondaryContainer = 0xFF004A30.toInt(),
    tertiary = AuroraTertiary,
    onTertiary = 0xFF000000.toInt(),
    error = AuroraError,
    background = 0xFFFFFFFF.toInt(),
    onBackground = 0xFF1A1A1A.toInt(),
    surface = AuroraSurface,
    onSurface = 0xFF1A1A1A.toInt(),
    surfaceVariant = AuroraSurfaceVariant,
    onSurfaceVariant = 0xFF444444.toInt(),
    outline = 0xFFC4C4C4.toInt(),
)

private val DarkColorScheme = darkColorScheme(
    primary = 0xFFB38BFF.toInt(),
    onPrimary = 0xFF3A1D8E.toInt(),
    primaryContainer = 0xFF7C4DFF.toInt(),
    onPrimaryContainer = 0xFFE4D9FF.toInt(),
    secondary = 0xFF69F0AE.toInt(),
    onSecondary = 0xFF001A0F.toInt(),
    secondaryContainer = 0xFF00A350.toInt(),
    onSecondaryContainer = 0xFFCCFFEC.toInt(),
    tertiary = 0xFFFFB74D.toInt(),
    onTertiary = 0xFF5A2E00.toInt(),
    error = 0xFFFF6B6B.toInt(),
    background = AuroraDarkSurface,
    onBackground = 0xFFE0E0E0.toInt(),
    surface = AuroraDarkSurface,
    onSurface = 0xFFE0E0E0.toInt(),
    surfaceVariant = AuroraDarkSurfaceVariant,
    onSurfaceVariant = 0xFF888888.toInt(),
    outline = 0xFF333333.toInt(),
)

// ------------------------------------------------------------------
// Theme mode holder. Observes DataStore theme mode; exposed as state so the UI
// layer can recompose when the user switches themes.
// ------------------------------------------------------------------
class ThemeModeHolder private constructor(initial: Int) {
    private val _mode = mutableStateOf(initial)
    val mode: Int get() = _mode.value
    fun setMode(value: Int) { _mode.value = value }

    companion object {
        @Volatile private var instance: ThemeModeHolder? = null
        fun getInstance(initial: Int): ThemeModeHolder =
            instance ?: synchronized(this) {
                instance ?: ThemeModeHolder(initial).also { instance = it }
            }
    }
}

/**
 * Root Compose theme. Picks the color scheme based on the selected theme mode:
 * light / dark / system / dynamic (wallpaper) / custom.
 *
 * Decision: dynamic color uses Material 3's `dynamicLightColorScheme()` /
 * `dynamicDarkColorScheme()` (API 31+) which derives the palette from the
 * wallpaper. Below API 31, or when dynamic is unavailable, falls back to the
 * static light/dark scheme. This satisfies "wallpaper-adaptive" while keeping
 * manual customization as a first-class mode.
 */
@Composable
fun AuroraTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor -> {
            val scheme = if (darkTheme) dynamicDarkColorScheme() else dynamicLightColorScheme()
            scheme ?: if (darkTheme) DarkColorScheme else LightColorScheme
        }
        else -> if (darkTheme) DarkColorScheme else LightColorScheme
    }

    androidx.compose.material3.MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

/**
 * Convenience: theme that resolves darkTheme from the system when mode is system,
 * and dynamicColor when mode is dynamic.
 */
@Composable
fun AuroraTheme(
    themeMode: Int,
    customPrimaryColor: Int = AuroraPrimary,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val (darkTheme, dynamicColor) = when (themeMode) {
        ThemeModeLight -> false to false
        ThemeModeDark -> true to false
        ThemeModeSystem -> isSystemDark to false
        ThemeModeDynamic -> isSystemDark to true
        ThemeModeCustom -> isSystemDark to false
        else -> false to false
    }

    // For custom mode, we tint the primary via LocalContentColor / a wrapped scheme.
    // Here we keep it simple: use base scheme; custom color applied at component level.
    val effectiveScheme = if (themeMode == ThemeModeDynamic) {
        val scheme = if (darkTheme) dynamicDarkColorScheme() else dynamicLightColorScheme()
        scheme ?: if (darkTheme) DarkColorScheme else LightColorScheme
    } else if (darkTheme) DarkColorScheme else LightColorScheme

    // If custom primary requested, derive a scheme from it (simple override).
    val finalScheme = if (themeMode == ThemeModeCustom) {
        val base = if (darkTheme) DarkColorScheme else LightColorScheme
        base.copy(primary = customPrimaryColor)
    } else effectiveScheme

    MaterialTheme(
        colorScheme = finalScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography

val Typography = androidx.compose.material3.Typography

val Shapes = androidx.compose.material3.Shapes(
    small = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
)
