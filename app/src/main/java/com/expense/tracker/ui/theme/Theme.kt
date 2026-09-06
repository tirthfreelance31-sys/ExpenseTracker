package com.expense.tracker.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = DeepTeal,
    onPrimary = Color.White,
    primaryContainer = SoftTealBg,
    onPrimaryContainer = Charcoal,
    secondary = SoftSky,
    onSecondary = Charcoal,
    tertiary = IncomeGreen,
    onTertiary = Color.White,
    background = WarmOffWhite,
    onBackground = Charcoal,
    surface = WarmWhite,
    onSurface = Charcoal,
    surfaceVariant = SurfaceSecondaryLight,
    onSurfaceVariant = MutedWarmGray,
    outline = SurfaceBorderLight,
    outlineVariant = Color(0xFFE5E2DA),
    error = ExpenseCoral,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimaryBrand,
    onPrimary = Color(0xFF0C2423),
    primaryContainer = DarkSoftTeal,
    onPrimaryContainer = DarkPrimaryText,
    secondary = DarkAmber,
    onSecondary = Color(0xFF261D0C),
    tertiary = DarkIncome,
    onTertiary = Color(0xFF092015),
    background = DarkBackground,
    onBackground = DarkPrimaryText,
    surface = DarkSurface,
    onSurface = DarkPrimaryText,
    surfaceVariant = DarkElevatedSurface,
    onSurfaceVariant = DarkSecondaryText,
    outline = DarkBorder,
    outlineVariant = Color(0xFF252826),
    error = DarkExpense,
    onError = Color(0xFF2E0F0C)
)

object AppTheme {
    val colors: AppColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current

    val typography
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.typography
}

@Composable
fun ExpenseTrackerTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val appColors = if (darkTheme) DarkAppColors else LightAppColors
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

    CompositionLocalProvider(
        LocalAppColors provides appColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
