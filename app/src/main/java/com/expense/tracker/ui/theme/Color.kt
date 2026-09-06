package com.expense.tracker.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ==========================================
// 1. LIGHT PALETTE - Warm Fintech Identity
// ==========================================
val DeepTeal = Color(0xFF167C80)              // Primary brand color
val WarmOffWhite = Color(0xFFF7F5F0)          // Main background
val WarmWhite = Color(0xFFFFFDF9)             // Card / surface
val Charcoal = Color(0xFF202322)              // Primary text
val MutedWarmGray = Color(0xFF747773)         // Secondary text
val TextMutedLight = Color(0xFF9EA19D)        // Tertiary / hint text

// Functional & Semantic Accents
val IncomeGreen = Color(0xFF35A875)           // Income / positive
val ExpenseCoral = Color(0xFFE56B61)          // Expense / negative
val WarmAmber = Color(0xFFE8A83E)             // Savings / accent
val SoftSky = Color(0xFF6FA8C9)               // UPI / sky accent
val SoftMint = Color(0xFFBFE4D0)              // Cash / mint accent

// Soft Pastel Backgrounds (Light)
val SoftCoralBg = Color(0xFFF8D9D4)
val SoftAmberBg = Color(0xFFF8E8C7)
val SoftTealBg = Color(0xFFD8ECEA)
val SoftSkyBg = Color(0xFFE5F1F8)
val SoftMintBg = Color(0xFFE8F6EE)

// Surface & Dividers (Light)
val SurfaceBorderLight = Color(0xFFEAE7E0)
val SurfaceSecondaryLight = Color(0xFFEFECE5)

// ==========================================
// 2. DARK PALETTE - Dedicated Warm Dark
// ==========================================
val DarkBackground = Color(0xFF151716)
val DarkSurface = Color(0xFF202321)
val DarkElevatedSurface = Color(0xFF292C2A)
val DarkPrimaryText = Color(0xFFF4F2EA)
val DarkSecondaryText = Color(0xFFA7AAA5)
val DarkPrimaryBrand = Color(0xFF58B9B7)
val DarkIncome = Color(0xFF55C28A)
val DarkExpense = Color(0xFFF08076)
val DarkAmber = Color(0xFFE8B65B)

// Soft Accents & Backgrounds (Dark)
val DarkSoftTeal = Color(0xFF244745)
val DarkSoftGreen = Color(0xFF253F33)
val DarkSoftCoral = Color(0xFF482B29)
val DarkSoftSky = Color(0xFF213B4D)
val DarkSoftAmber = Color(0xFF473722)
val DarkBorder = Color(0xFF2F3230)

// ==========================================
// 3. UNIFIED DESIGN SYSTEM SEMANTIC TOKENS
// ==========================================
data class AppColors(
    val primary: Color,
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val surfaceSecondary: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val border: Color,
    val income: Color,
    val expense: Color,
    val amber: Color,
    val sky: Color,
    val mint: Color,
    val softTealBg: Color,
    val softGreenBg: Color,
    val softAmberBg: Color,
    val softCoralBg: Color,
    val isDark: Boolean
)

val LightAppColors = AppColors(
    primary = DeepTeal,
    background = WarmOffWhite,
    surface = WarmWhite,
    surfaceElevated = Color.White,
    surfaceSecondary = SurfaceSecondaryLight,
    textPrimary = Charcoal,
    textSecondary = MutedWarmGray,
    textMuted = TextMutedLight,
    border = SurfaceBorderLight,
    income = IncomeGreen,
    expense = ExpenseCoral,
    amber = WarmAmber,
    sky = SoftSky,
    mint = SoftMint,
    softTealBg = SoftTealBg,
    softGreenBg = SoftMintBg,
    softAmberBg = SoftAmberBg,
    softCoralBg = SoftCoralBg,
    isDark = false
)

val DarkAppColors = AppColors(
    primary = DarkPrimaryBrand,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceElevated = DarkElevatedSurface,
    surfaceSecondary = DarkElevatedSurface,
    textPrimary = DarkPrimaryText,
    textSecondary = DarkSecondaryText,
    textMuted = Color(0xFF7E827D),
    border = DarkBorder,
    income = DarkIncome,
    expense = DarkExpense,
    amber = DarkAmber,
    sky = Color(0xFF86BCD9),
    mint = Color(0xFF8CD4B0),
    softTealBg = DarkSoftTeal,
    softGreenBg = DarkSoftGreen,
    softAmberBg = DarkSoftAmber,
    softCoralBg = DarkSoftCoral,
    isDark = true
)

val LocalAppColors = staticCompositionLocalOf { LightAppColors }

// ==========================================
// 4. BACKWARD COMPATIBILITY ALIASES
// ==========================================
val BrandBackground get() = WarmOffWhite
val SurfacePrimary get() = WarmWhite
val SurfaceSecondary get() = SurfaceSecondaryLight
val SurfaceBorder get() = SurfaceBorderLight

val TextPrimary get() = Charcoal
val TextSecondary get() = MutedWarmGray
val TextMuted get() = TextMutedLight

val BrandViolet get() = DeepTeal
val SkyBlue get() = SoftSky
val FreshGreen get() = IncomeGreen
val CoralRed get() = ExpenseCoral
val SoftPink = Color(0xFFD6778D)

val UpiTint get() = SoftTealBg
val CashTint get() = SoftMintBg
val SavingsTint get() = SoftAmberBg

val LedgerInk get() = Charcoal
val LedgerPaper get() = WarmWhite
val LedgerPaperVariant get() = SurfaceSecondaryLight
val LedgerDivider get() = SurfaceBorderLight
val RupeeGold get() = DeepTeal
val CurrencyGreen get() = IncomeGreen
val StampIndigo get() = DeepTeal
val SealRed get() = ExpenseCoral
val PrimaryText get() = Charcoal
val SecondaryText get() = MutedWarmGray
val MutedText get() = TextMutedLight
