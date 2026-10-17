package com.annhien.womenshealth.core.design

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ─── Color Palette ────────────────────────────────────────────────────────
// Design language: sage green + dusty lavender + warm neutrals
// Mature, calm, and accessible. Not pink-dominant.
// All colors checked for WCAG 2.1 AA contrast compliance.

object AnNhienColors {
    // Sage green (primary) - exactly matching web simulator
    val Sage500 = Color(0xFF3D684E)   // Primary
    val Sage100 = Color(0xFFD8E7DB)   // Primary container
    val Sage900 = Color(0xFF00210E)   // On primary container

    // Dusty lavender (secondary)
    val Lavender500 = Color(0xFF5F5B7D)  // Secondary
    val Lavender100 = Color(0xFFE5E0F9)  // Secondary container
    val Lavender900 = Color(0xFF1C1836)

    // Dusty rose / terracotta (tertiary)
    val Terracotta500 = Color(0xFF854E62)  // Tertiary
    val Terracotta100 = Color(0xFFFFD9E2)  // Tertiary container
    val Terracotta900 = Color(0xFF3B0B1D)

    // Surfaces & Backgrounds
    val BackgroundLight = Color(0xFFF8FAF8)
    val SurfaceLight = Color(0xFFFFFFFF)
    val SurfaceVariantLight = Color(0xFFF0F4F0)
    val Neutral900 = Color(0xFF191C1A)
    val Neutral700 = Color(0xFF424942)
    val Neutral500 = Color(0xFF727972)
    val Neutral300 = Color(0xFFE0E5E0)
    val Neutral200 = Color(0xFFE8ECE8)

    // Period / Error Red
    val PeriodRed = Color(0xFFBA1A1A)
    val PeriodRedContainer = Color(0xFFFFDAD6)
    val OnPeriodRedContainer = Color(0xFF410002)

    // Dark mode
    val Sage300 = Color(0xFFA2D3AF)
    val Sage700 = Color(0xFF244F37)
    val Lavender300 = Color(0xFFC8C2EA)
    val Lavender700 = Color(0xFF474364)
    val Terracotta300 = Color(0xFFF8B4CB)
    val Terracotta700 = Color(0xFF6B374A)
    val DarkBackground = Color(0xFF111412)
    val DarkSurface = Color(0xFF191C1A)
    val DarkSurfaceVariant = Color(0xFF222623)
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
    onTertiaryContainer = AnNhienColors.Terracotta900,

    background = AnNhienColors.BackgroundLight,
    onBackground = AnNhienColors.Neutral900,
    surface = AnNhienColors.SurfaceLight,
    onSurface = AnNhienColors.Neutral900,
    surfaceVariant = AnNhienColors.SurfaceVariantLight,
    onSurfaceVariant = AnNhienColors.Neutral700,

    outline = AnNhienColors.Neutral500,
    outlineVariant = AnNhienColors.Neutral300,

    error = AnNhienColors.PeriodRed,
    onError = Color.White,
    errorContainer = AnNhienColors.PeriodRedContainer,
    onErrorContainer = AnNhienColors.OnPeriodRedContainer
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
    tertiaryContainer = AnNhienColors.Terracotta700,
    onTertiaryContainer = AnNhienColors.Terracotta100,

    background = AnNhienColors.DarkBackground,
    onBackground = Color(0xFFE1E3DF),
    surface = AnNhienColors.DarkSurface,
    onSurface = Color(0xFFE1E3DF),
    surfaceVariant = AnNhienColors.DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFC1C7C1),

    outline = Color(0xFF8B938B),
    outlineVariant = Color(0xFF303531),

    error = Color(0xFFFFB4AB),
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

// ─── Utilities Palette (Synchronized with Web Simulator) ───────────────────

enum class UtilityColorCategory {
    SAGE, ROSE, LAVENDER, TERRACOTTA, SUN, WATER, NEUTRAL
}

object UtilityThemeColors {
    // Sage (.icon-sage)
    val SageContainerLight = Color(0xFFD8E7DB)
    val SageIconLight = Color(0xFF3D684E)
    val SageContainerDark = Color(0xFF244F37)
    val SageIconDark = Color(0xFFA2D3AF)

    // Rose (.icon-rose)
    val RoseContainerLight = Color(0xFFFFD9E2)
    val RoseIconLight = Color(0xFF854E62)
    val RoseContainerDark = Color(0xFF6B374A)
    val RoseIconDark = Color(0xFFF8B4CB)

    // Lavender (.icon-lavender)
    val LavenderContainerLight = Color(0xFFE5E0F9)
    val LavenderIconLight = Color(0xFF5F5B7D)
    val LavenderContainerDark = Color(0xFF474364)
    val LavenderIconDark = Color(0xFFC8C2EA)

    // Terracotta (.icon-terracotta)
    val TerracottaContainerLight = Color(0xFFFFE0D2)
    val TerracottaIconLight = Color(0xFFA83800)
    val TerracottaContainerDark = Color(0xFF5C2000)
    val TerracottaIconDark = Color(0xFFFFB596)

    // Sun (.icon-sun)
    val SunContainerLight = Color(0xFFFFF0C2)
    val SunIconLight = Color(0xFFB38600)
    val SunContainerDark = Color(0xFF564100)
    val SunIconDark = Color(0xFFFFDE7B)

    // Water (.icon-water)
    val WaterContainerLight = Color(0xFFD9EEFF)
    val WaterIconLight = Color(0xFF0066B2)
    val WaterContainerDark = Color(0xFF00497D)
    val WaterIconDark = Color(0xFF9ECAFF)
}

@Composable
fun getUtilityCategoryColors(category: UtilityColorCategory): Pair<Color, Color> {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    return when (category) {
        UtilityColorCategory.SAGE -> if (isDark)
            UtilityThemeColors.SageContainerDark to UtilityThemeColors.SageIconDark
        else
            UtilityThemeColors.SageContainerLight to UtilityThemeColors.SageIconLight

        UtilityColorCategory.ROSE -> if (isDark)
            UtilityThemeColors.RoseContainerDark to UtilityThemeColors.RoseIconDark
        else
            UtilityThemeColors.RoseContainerLight to UtilityThemeColors.RoseIconLight

        UtilityColorCategory.LAVENDER -> if (isDark)
            UtilityThemeColors.LavenderContainerDark to UtilityThemeColors.LavenderIconDark
        else
            UtilityThemeColors.LavenderContainerLight to UtilityThemeColors.LavenderIconLight

        UtilityColorCategory.TERRACOTTA -> if (isDark)
            UtilityThemeColors.TerracottaContainerDark to UtilityThemeColors.TerracottaIconDark
        else
            UtilityThemeColors.TerracottaContainerLight to UtilityThemeColors.TerracottaIconLight

        UtilityColorCategory.SUN -> if (isDark)
            UtilityThemeColors.SunContainerDark to UtilityThemeColors.SunIconDark
        else
            UtilityThemeColors.SunContainerLight to UtilityThemeColors.SunIconLight

        UtilityColorCategory.WATER -> if (isDark)
            UtilityThemeColors.WaterContainerDark to UtilityThemeColors.WaterIconDark
        else
            UtilityThemeColors.WaterContainerLight to UtilityThemeColors.WaterIconLight

        UtilityColorCategory.NEUTRAL -> if (isDark)
            MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurface
        else
            MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurface
    }
}

