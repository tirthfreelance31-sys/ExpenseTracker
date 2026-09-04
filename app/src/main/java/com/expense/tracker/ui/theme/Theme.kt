package com.expense.tracker.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LedgerColorScheme = darkColorScheme(
    primary = RupeeGold,
    onPrimary = LedgerInk,
    primaryContainer = LedgerPaper,
    onPrimaryContainer = PrimaryText,
    secondary = StampIndigo,
    onSecondary = PrimaryText,
    tertiary = CurrencyGreen,
    onTertiary = PrimaryText,
    background = LedgerInk,
    onBackground = PrimaryText,
    surface = LedgerInk,
    onSurface = PrimaryText,
    surfaceVariant = LedgerPaper,
    onSurfaceVariant = SecondaryText,
    outline = LedgerDivider,
    error = SealRed
)

@Composable
fun ExpenseTrackerTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = LedgerColorScheme.background.toArgb()
            window.navigationBarColor = LedgerColorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = LedgerColorScheme,
        typography = Typography,
        content = content
    )
}
