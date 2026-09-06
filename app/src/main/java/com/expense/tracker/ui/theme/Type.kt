package com.expense.tracker.ui.theme

import android.content.Context
import android.util.Log
import androidx.annotation.FontRes
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import com.expense.tracker.R

private const val TAG = "SafeTypography"

// Safe font verification helper - catches any Resources.NotFoundException or parsing issues
private fun canLoadFont(context: Context, @FontRes resId: Int): Boolean {
    return try {
        ResourcesCompat.getFont(context, resId) != null
    } catch (e: Throwable) {
        Log.w(TAG, "Failed to load font resource ID $resId, using system fallback", e)
        false
    }
}

// Global FontFamily references with safe system fallbacks
var Manrope: FontFamily = FontFamily.SansSerif
    private set

var Inter: FontFamily = FontFamily.SansSerif
    private set

var SpaceGrotesk: FontFamily = FontFamily.SansSerif
    private set

// Backward compatibility alias
val IbmPlexSans: FontFamily
    get() = Inter

var AppTypography: Typography = createTypography(FontFamily.SansSerif, FontFamily.SansSerif, FontFamily.SansSerif)
    private set

val Typography: Typography
    get() = AppTypography

/**
 * Initializes font families safely at application startup.
 * If any custom font cannot be retrieved on the target device,
 * it cleanly falls back to Android's system sans-serif without crashing.
 */
fun initSafeFonts(context: Context) {
    Manrope = try {
        val fonts = mutableListOf<Font>()
        if (canLoadFont(context, R.font.manrope_regular)) {
            fonts.add(Font(R.font.manrope_regular, FontWeight.Normal))
        }
        if (canLoadFont(context, R.font.manrope_medium)) {
            fonts.add(Font(R.font.manrope_medium, FontWeight.Medium))
        }
        if (canLoadFont(context, R.font.manrope_semibold)) {
            fonts.add(Font(R.font.manrope_semibold, FontWeight.SemiBold))
        }
        if (fonts.isNotEmpty()) {
            FontFamily(fonts)
        } else {
            FontFamily.SansSerif
        }
    } catch (e: Throwable) {
        Log.w(TAG, "Failed to initialize Manrope FontFamily", e)
        FontFamily.SansSerif
    }

    Inter = try {
        val fonts = mutableListOf<Font>()
        if (canLoadFont(context, R.font.inter_regular)) {
            fonts.add(Font(R.font.inter_regular, FontWeight.Normal))
        }
        if (canLoadFont(context, R.font.inter_medium)) {
            fonts.add(Font(R.font.inter_medium, FontWeight.Medium))
        }
        if (canLoadFont(context, R.font.inter_semibold)) {
            fonts.add(Font(R.font.inter_semibold, FontWeight.SemiBold))
        }
        if (fonts.isNotEmpty()) {
            FontFamily(fonts)
        } else {
            FontFamily.SansSerif
        }
    } catch (e: Throwable) {
        Log.w(TAG, "Failed to initialize Inter FontFamily", e)
        FontFamily.SansSerif
    }

    SpaceGrotesk = try {
        val fonts = mutableListOf<Font>()
        if (canLoadFont(context, R.font.space_grotesk_regular)) {
            fonts.add(Font(R.font.space_grotesk_regular, FontWeight.Normal))
        }
        if (canLoadFont(context, R.font.space_grotesk_medium)) {
            fonts.add(Font(R.font.space_grotesk_medium, FontWeight.Medium))
        }
        if (canLoadFont(context, R.font.space_grotesk_semibold)) {
            fonts.add(Font(R.font.space_grotesk_semibold, FontWeight.SemiBold))
        }
        if (canLoadFont(context, R.font.space_grotesk_bold)) {
            fonts.add(Font(R.font.space_grotesk_bold, FontWeight.Bold))
        }
        if (fonts.isNotEmpty()) {
            FontFamily(fonts)
        } else {
            FontFamily.SansSerif
        }
    } catch (e: Throwable) {
        Log.w(TAG, "Failed to initialize SpaceGrotesk FontFamily", e)
        FontFamily.SansSerif
    }

    AppTypography = createTypography(Manrope, Inter, SpaceGrotesk)
}

fun createTypography(
    manrope: FontFamily,
    inter: FontFamily,
    spaceGrotesk: FontFamily
): Typography {
    return Typography(
        displayLarge = TextStyle(
            fontFamily = spaceGrotesk,
            fontWeight = FontWeight.SemiBold,
            fontSize = 40.sp,
            lineHeight = 48.sp,
            letterSpacing = (-0.5).sp,
            fontFeatureSettings = "tnum"
        ),
        headlineLarge = TextStyle(
            fontFamily = spaceGrotesk,
            fontWeight = FontWeight.SemiBold,
            fontSize = 32.sp,
            lineHeight = 38.sp,
            letterSpacing = (-0.5).sp,
            fontFeatureSettings = "tnum"
        ),
        headlineMedium = TextStyle(
            fontFamily = spaceGrotesk,
            fontWeight = FontWeight.SemiBold,
            fontSize = 24.sp,
            lineHeight = 30.sp,
            letterSpacing = (-0.3).sp,
            fontFeatureSettings = "tnum"
        ),
        headlineSmall = TextStyle(
            fontFamily = spaceGrotesk,
            fontWeight = FontWeight.Medium,
            fontSize = 20.sp,
            lineHeight = 26.sp,
            fontFeatureSettings = "tnum"
        ),
        titleLarge = TextStyle(
            fontFamily = manrope,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            lineHeight = 26.sp,
            letterSpacing = (-0.2).sp
        ),
        titleMedium = TextStyle(
            fontFamily = manrope,
            fontWeight = FontWeight.SemiBold,
            fontSize = 17.sp,
            lineHeight = 22.sp
        ),
        titleSmall = TextStyle(
            fontFamily = manrope,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            lineHeight = 20.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = inter,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = inter,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp
        ),
        bodySmall = TextStyle(
            fontFamily = inter,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp
        ),
        labelLarge = TextStyle(
            fontFamily = inter,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp
        ),
        labelMedium = TextStyle(
            fontFamily = inter,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp
        ),
        labelSmall = TextStyle(
            fontFamily = inter,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 14.sp
        )
    )
}
