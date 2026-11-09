package com.example.ui.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.model.AppThemeConfig
import com.example.model.ColorPaletteMode

@Immutable
data class TrustColorTokens(
    val accent: Color,
    val accentSecondary: Color,
    val gold: Color,
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val positive: Color,
    val negative: Color
)

val LocalTrustColors = staticCompositionLocalOf {
    TrustColorTokens(
        accent = RoyalPurple,
        accentSecondary = RoyalPurpleGlow,
        gold = SovereignGold,
        background = ObsidianBackground,
        surface = ObsidianSurface,
        surfaceElevated = ObsidianSurfaceElevated,
        border = ObsidianBorder,
        textPrimary = TextPrimaryDark,
        textSecondary = TextSecondaryDark,
        positive = EmeraldPositive,
        negative = RoseNegative
    )
}

@Composable
fun SmithTrustTheme(
    config: AppThemeConfig = AppThemeConfig(),
    content: @Composable () -> Unit
) {
    val isLight = config.isLightMode

    val accentColor = config.customAccentArgb?.let { Color(it) }
        ?: Color(config.palette.primaryHex)

    val backgroundColor = when {
        isLight -> Color(0xFFF8FAFC)
        config.isTrueAmoled -> Color.Black
        else -> Color(config.palette.backgroundHex)
    }

    val surfaceColor = when {
        isLight -> Color(0xFFFFFFFF)
        config.isTrueAmoled -> Color(0xFF0D0D11)
        else -> Color(config.palette.surfaceHex)
    }

    val surfaceElevatedColor = when {
        isLight -> Color(0xFFF1F5F9)
        else -> when (config.palette) {
            ColorPaletteMode.ROYAL_OBSIDIAN -> Color(0xFF1D1435)
            ColorPaletteMode.EMERALD_SOVEREIGN -> Color(0xFF102D24)
            ColorPaletteMode.IMPERIAL_GOLD -> Color(0xFF2C220E)
            ColorPaletteMode.MIDNIGHT_SAPPHIRE -> Color(0xFF12243C)
            ColorPaletteMode.CRIMSON_DYNASTY -> Color(0xFF33151E)
        }
    }

    val borderColor = if (isLight) Color(0xFFCBD5E1) else accentColor.copy(alpha = 0.28f)
    val textPrimaryColor = if (isLight) Color(0xFF0F172A) else TextPrimaryDark
    val textSecondaryColor = if (isLight) Color(0xFF475569) else TextSecondaryDark
    val goldColor = if (isLight) Color(0xFFB45309) else SovereignGold
    val positiveColor = if (isLight) Color(0xFF047857) else EmeraldPositive
    val negativeColor = if (isLight) Color(0xFFDC2626) else RoseNegative

    val trustTokens = TrustColorTokens(
        accent = accentColor,
        accentSecondary = accentColor.copy(alpha = 0.75f),
        gold = goldColor,
        background = backgroundColor,
        surface = surfaceColor,
        surfaceElevated = surfaceElevatedColor,
        border = borderColor,
        textPrimary = textPrimaryColor,
        textSecondary = textSecondaryColor,
        positive = positiveColor,
        negative = negativeColor
    )

    val colorScheme = if (isLight) {
        lightColorScheme(
            primary = accentColor,
            onPrimary = Color.White,
            primaryContainer = surfaceElevatedColor,
            onPrimaryContainer = textPrimaryColor,
            secondary = goldColor,
            onSecondary = Color.White,
            background = backgroundColor,
            onBackground = textPrimaryColor,
            surface = surfaceColor,
            onSurface = textPrimaryColor,
            surfaceVariant = surfaceElevatedColor,
            onSurfaceVariant = textSecondaryColor,
            outline = borderColor
        )
    } else {
        darkColorScheme(
            primary = accentColor,
            onPrimary = Color.White,
            primaryContainer = surfaceElevatedColor,
            onPrimaryContainer = Color.White,
            secondary = SovereignGold,
            onSecondary = Color.Black,
            background = backgroundColor,
            onBackground = TextPrimaryDark,
            surface = surfaceColor,
            onSurface = TextPrimaryDark,
            surfaceVariant = surfaceElevatedColor,
            onSurfaceVariant = TextSecondaryDark,
            outline = borderColor
        )
    }

    CompositionLocalProvider(LocalTrustColors provides trustTokens) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
