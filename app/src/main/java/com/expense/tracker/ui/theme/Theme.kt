package com.expense.tracker.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = BrandViolet,
    onPrimary = Color.White,
    primaryContainer = SurfaceSecondary,
    onPrimaryContainer = TextPrimary,
    secondary = SkyBlue,
    onSecondary = Color.White,
    tertiary = FreshGreen,
    onTertiary = Color.White,
    background = BrandBackground,
    onBackground = TextPrimary,
    surface = SurfacePrimary,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceSecondary,
    onSurfaceVariant = TextSecondary,
    outline = SurfaceBorder,
    error = CoralRed
)

private val DarkColorScheme = darkColorScheme(
    primary = BrandViolet,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1E2029),
    onPrimaryContainer = Color(0xFFEDEDED),
    secondary = SkyBlue,
    onSecondary = Color.White,
    tertiary = FreshGreen,
    onTertiary = Color.White,
    background = Color(0xFF121318),
    onBackground = Color(0xFFEDEDED),
    surface = Color(0xFF1A1B22),
    onSurface = Color(0xFFEDEDED),
    surfaceVariant = Color(0xFF232530),
    onSurfaceVariant = Color(0xFFA0A3AF),
    outline = Color(0xFF2E3140),
    error = CoralRed
)

@Composable
fun ExpenseTrackerTheme(
    darkTheme: Boolean = false, // Light theme by default
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.surface.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

