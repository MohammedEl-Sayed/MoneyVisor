package com.moneyvisor.core.designsystem.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = AppAccentDark,
    secondary = LightBlueSurface.copy(alpha = 0.2f),
    tertiary = Pink80,
    background = BackgroundDark,
    surface = SurfaceDark,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = TextSecondaryDark,
    secondaryContainer = Color(0xFF2C2C2C),
    onSecondaryContainer = Color.White
)

private val AmoledColorScheme = darkColorScheme(
    primary = AppAccentDark,
    secondary = LightBlueSurface.copy(alpha = 0.2f),
    tertiary = Pink80,
    background = BackgroundAmoled,
    surface = SurfaceAmoled,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceVariantAmoled,
    onSurfaceVariant = TextSecondaryDark,
    secondaryContainer = SurfaceVariantAmoled,
    onSecondaryContainer = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = BrandBlue,
    secondary = LightBlueSurface,
    tertiary = Pink40,
    background = BackgroundLight,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = BrandBlue,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFFF5F7FA),
    onSurfaceVariant = TextSecondary,
    secondaryContainer = LightBlueSurface,
    onSecondaryContainer = BrandBlue
)

@Composable
fun MoneyVisorTheme(
    themeMode: String = "SYSTEM",
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        "LIGHT" -> false
        "DARK", "AMOLED" -> true
        else -> isSystemInDarkTheme()
    }
    
    val colorScheme = when (themeMode) {
        "AMOLED" -> AmoledColorScheme
        "DARK" -> DarkColorScheme
        "LIGHT" -> LightColorScheme
        else -> if (isSystemInDarkTheme()) DarkColorScheme else LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
