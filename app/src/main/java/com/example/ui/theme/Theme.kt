package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = NaturalAccentGreenMedium,
    secondary = NaturalTextMuted,
    tertiary = NaturalAccentGreenSoft,
    background = NaturalTextPrimary,
    surface = NaturalTextSlate,
    onPrimary = NaturalTextPrimary,
    onSecondary = NaturalBackground,
    onTertiary = NaturalTextPrimary,
    onBackground = NaturalBackground,
    onSurface = NaturalBackground
)

private val LightColorScheme = lightColorScheme(
    primary = NaturalPrimaryGreen,
    secondary = NaturalTextMuted,
    tertiary = NaturalAccentGreenMedium,
    background = NaturalBackground,
    surface = NaturalBackground,
    onPrimary = NaturalBackground,
    onSecondary = NaturalTextPrimary,
    onTertiary = NaturalTextPrimary,
    onBackground = NaturalTextPrimary,
    onSurface = NaturalTextPrimary,
    surfaceVariant = NaturalAccentGreenLight,
    onSurfaceVariant = NaturalTextSlate,
    outline = NaturalBorderGray,
    error = NaturalAlertRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disable dynamic colors to enforce the beautiful Natural Tones theme
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
