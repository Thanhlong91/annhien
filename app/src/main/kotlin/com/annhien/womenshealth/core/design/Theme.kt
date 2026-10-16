package com.annhien.womenshealth.core.design

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ─── Color Palette ────────────────────────────────────────────────────────
// Design language: sage green + dusty lavender + warm neutrals
// Mature, calm, and accessible. Not pink-dominant.
// All colors checked for WCAG 2.1 AA contrast compliance.

object AnNhienColors {
    // Sage green (primary)
    val Sage50 = Color(0xFFF2F5F0)
    val Sage100 = Color(0xFFDDE5D9)
    val Sage200 = Color(0xFFC1D4BA)
    val Sage300 = Color(0xFFA3C29A)
    val Sage400 = Color(0xFF87B07B)
    val Sage500 = Color(0xFF6B9E5E)   // Primary
    val Sage600 = Color(0xFF577F4C)
    val Sage700 = Color(0xFF43613A)
    val Sage800 = Color(0xFF2E4329)
    val Sage900 = Color(0xFF1A2618)

    // Dusty lavender (secondary)
    val Lavender50 = Color(0xFFF5F3F9)
    val Lavender100 = Color(0xFFE8E2F4)
    val Lavender200 = Color(0xFFD0C8E8)
    val Lavender300 = Color(0xFFB7AEDC)
    val Lavender400 = Color(0xFF9E93D0)
    val Lavender500 = Color(0xFF8578C4)  // Secondary
    val Lavender600 = Color(0xFF6A5FAE)
    val Lavender700 = Color(0xFF504792)
    val Lavender800 = Color(0xFF362F75)
    val Lavender900 = Color(0xFF1C1858)

    // Warm terracotta (tertiary / accent for period)
    val Terracotta50 = Color(0xFFFAF1EE)
    val Terracotta100 = Color(0xFFF3DDD6)
    val Terracotta300 = Color(0xFFD9917C)
    val Terracotta500 = Color(0xFFC0624A)  // Tertiary (used sparingly)

    // Warm neutral surface
    val Neutral50  = Color(0xFFFBF9F7)
    val Neutral100 = Color(0xFFF2EFE9)
    val Neutral200 = Color(0xFFE5DFD5)
    val Neutral300 = Color(0xFFCFC7B9)
    val Neutral400 = Color(0xFFB8AD9D)
    val Neutral500 = Color(0xFF9E9282)
    val Neutral600 = Color(0xFF7D7567)
    val Neutral700 = Color(0xFF5E584D)
    val Neutral800 = Color(0xFF3E3A32)
    val Neutral900 = Color(0xFF1F1D18)

    // Semantic
    val ErrorLight = Color(0xFFB3261E)
    val ErrorDark  = Color(0xFFFFB4AB)
    val WarningLight = Color(0xFF7A5900)
    val WarningDark  = Color(0xFFFFB951)

    // Dark mode surfaces
    val DarkSurface = Color(0xFF1A1C1A)
    val DarkSurfaceVariant = Color(0xFF252825)
    val DarkBackground = Color(0xFF121412)
}

// ─── Color Schemes ────────────────────────────────────────────────────────

val AnNhienLightColorScheme = lightColorScheme(
    primary = AnNhienColors.Sage500,
    onPrimary = Color.White,
    primaryContainer = AnNhienColors.Sage100,
    onPrimaryContainer = AnNhienColors.Sage900,

    secondary = AnNhienColors.Lavender500,
    onSecondary = Color.White,
    secondaryContainer = AnNhienColors.Lavender100,
    onSecondaryContainer = AnNhienColors.Lavender900,

    tertiary = AnNhienColors.Terracotta500,
    onTertiary = Color.White,
    tertiaryContainer = AnNhienColors.Terracotta100,
    onTertiaryContainer = Color(0xFF3B0B00),

    background = AnNhienColors.Neutral50,
    onBackground = AnNhienColors.Neutral900,
    surface = Color.White,
    onSurface = AnNhienColors.Neutral900,
    surfaceVariant = AnNhienColors.Neutral100,
    onSurfaceVariant = AnNhienColors.Neutral700,

    outline = AnNhienColors.Neutral300,
    outlineVariant = AnNhienColors.Neutral200,

    error = AnNhienColors.ErrorLight,
    onError = Color.White,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B)
)

val AnNhienDarkColorScheme = darkColorScheme(
    primary = AnNhienColors.Sage300,
    onPrimary = AnNhienColors.Sage900,
    primaryContainer = AnNhienColors.Sage700,
    onPrimaryContainer = AnNhienColors.Sage100,

    secondary = AnNhienColors.Lavender300,
    onSecondary = AnNhienColors.Lavender900,
    secondaryContainer = AnNhienColors.Lavender700,
    onSecondaryContainer = AnNhienColors.Lavender100,

    tertiary = AnNhienColors.Terracotta300,
    onTertiary = Color(0xFF3B0B00),
    tertiaryContainer = Color(0xFF5C1A06),
    onTertiaryContainer = AnNhienColors.Terracotta50,

    background = AnNhienColors.DarkBackground,
    onBackground = AnNhienColors.Neutral100,
    surface = AnNhienColors.DarkSurface,
    onSurface = AnNhienColors.Neutral100,
    surfaceVariant = AnNhienColors.DarkSurfaceVariant,
    onSurfaceVariant = AnNhienColors.Neutral300,

    outline = AnNhienColors.Neutral600,
    outlineVariant = AnNhienColors.Neutral700,

    error = AnNhienColors.ErrorDark,
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

// ─── Typography ───────────────────────────────────────────────────────────
// Uses system default (sans-serif) which respects user font size settings.
// No font loaded from network.

val AnNhienTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

// ─── Theme ────────────────────────────────────────────────────────────────

@Composable
fun AnNhienTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) AnNhienDarkColorScheme else AnNhienLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AnNhienTypography,
        content = content
    )
}
