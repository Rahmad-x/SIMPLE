package com.example.simple.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = SimpleColors.Primary,
    onPrimary = SimpleColors.OnPrimary,
    primaryContainer = SimpleColors.PrimaryContainer,
    onPrimaryContainer = SimpleColors.OnPrimaryContainer,
    secondary = SimpleColors.Secondary,
    onSecondary = SimpleColors.OnSecondary,
    secondaryContainer = SimpleColors.SecondaryContainer,
    onSecondaryContainer = SimpleColors.OnSecondaryContainer,
    tertiary = SimpleColors.Tertiary,
    onTertiary = SimpleColors.OnTertiary,
    tertiaryContainer = SimpleColors.TertiaryContainer,
    onTertiaryContainer = SimpleColors.OnTertiaryContainer,
    error = SimpleColors.Error,
    onError = SimpleColors.OnError,
    errorContainer = SimpleColors.ErrorContainer,
    onErrorContainer = SimpleColors.OnErrorContainer,
    background = SimpleColors.Background,
    onBackground = SimpleColors.OnBackground,
    surface = SimpleColors.Surface,
    onSurface = SimpleColors.OnSurface,
    surfaceVariant = SimpleColors.SurfaceVariant,
    onSurfaceVariant = SimpleColors.OnSurfaceVariant,
    outline = SimpleColors.Border,
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF52D9C4),
    onPrimary = SimpleColors.OnPrimaryContainer,
    primaryContainer = Color(0xFF005048),
    onPrimaryContainer = Color(0xFF7DFFED),
    secondary = SimpleColors.SecondaryContainer,
    onSecondary = SimpleColors.OnSecondaryContainer,
    tertiary = SimpleColors.TertiaryContainer,
    onTertiary = SimpleColors.OnTertiaryContainer,
    background = Color(0xFF121212),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE6E1E5),
)

@Composable
fun SimpleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SimpleTypography,
        content = content,
    )
}