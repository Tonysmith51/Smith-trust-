package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppThemeConfig
import com.example.model.ColorPaletteMode
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorCustomizerSheet(
    currentConfig: AppThemeConfig,
    onConfigChange: (AppThemeConfig) -> Unit,
    onDismiss: () -> Unit
) {
    val tokens = LocalTrustColors.current
    val scrollState = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = tokens.surface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = tokens.border) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Title Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = tokens.accent,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Customize App Colors & Theme",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = tokens.textPrimary
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_theme_sheet")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = tokens.textSecondary)
                }
            }

            // 0. Display Mode: Light Mode vs Dark Mode
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "DISPLAY MODE",
                    style = MaterialTheme.typography.labelSmall,
                    color = tokens.textSecondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Dark Mode Option
                    val isDark = !currentConfig.isLightMode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDark) tokens.accent.copy(alpha = 0.2f) else tokens.surface)
                            .border(
                                width = if (isDark) 2.dp else 1.dp,
                                color = if (isDark) tokens.accent else tokens.border,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                onConfigChange(currentConfig.copy(isLightMode = false))
                            }
                            .padding(horizontal = 12.dp, vertical = 12.dp)
                            .testTag("theme_dark_mode_option"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DarkMode,
                                contentDescription = null,
                                tint = if (isDark) tokens.accent else tokens.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Dark Mode",
                                fontWeight = if (isDark) FontWeight.Bold else FontWeight.Medium,
                                color = if (isDark) tokens.textPrimary else tokens.textSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }

                    // Light Mode Option
                    val isLight = currentConfig.isLightMode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isLight) tokens.accent.copy(alpha = 0.2f) else tokens.surface)
                            .border(
                                width = if (isLight) 2.dp else 1.dp,
                                color = if (isLight) tokens.accent else tokens.border,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                onConfigChange(currentConfig.copy(isLightMode = true, isTrueAmoled = false))
                            }
                            .padding(horizontal = 12.dp, vertical = 12.dp)
                            .testTag("theme_light_mode_option"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LightMode,
                                contentDescription = null,
                                tint = if (isLight) tokens.accent else tokens.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Light Mode",
                                fontWeight = if (isLight) FontWeight.Bold else FontWeight.Medium,
                                color = if (isLight) tokens.textPrimary else tokens.textSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // 1. Sovereign Palette Presets
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "SOVEREIGN PALETTES",
                    style = MaterialTheme.typography.labelSmall,
                    color = tokens.textSecondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ColorPaletteMode.values().forEach { mode ->
                        val isSelected = currentConfig.palette == mode && currentConfig.customAccentArgb == null
                        PaletteSelectionItem(
                            palette = mode,
                            isSelected = isSelected,
                            onSelect = {
                                onConfigChange(
                                    currentConfig.copy(
                                        palette = mode,
                                        customAccentArgb = null
                                    )
                                )
                            }
                        )
                    }
                }
            }

            // 2. Custom Accent Swatches
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "CUSTOM ACCENT ACCENT COLOR",
                    style = MaterialTheme.typography.labelSmall,
                    color = tokens.textSecondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                val accentSwatches = listOf(
                    PaletteRoyalPurple to "Royal Purple",
                    PaletteEmerald to "Emerald Mint",
                    PaletteGold to "Imperial Gold",
                    PaletteSapphire to "Electric Sky",
                    PaletteCrimson to "Ruby Rose",
                    PaletteAmethyst to "Orchid Amethyst",
                    PaletteCopper to "Sunset Amber",
                    PaletteTeal to "Nordic Teal"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    accentSwatches.forEach { (color, name) ->
                        val colorArgb = color.toArgb().toLong()
                        val isSelected = currentConfig.customAccentArgb == colorArgb

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable {
                                    onConfigChange(
                                        currentConfig.copy(customAccentArgb = colorArgb)
                                    )
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) Color.White else Color.Transparent,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = name.split(" ").first(),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) tokens.textPrimary else tokens.textSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            // 3. AMOLED Pure Black Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(tokens.surfaceElevated)
                    .border(1.dp, tokens.border, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "AMOLED Pure Pitch Black",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = tokens.textPrimary
                    )
                    Text(
                        text = "Maximizes battery life with 100% black pixels (#000000)",
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.textSecondary
                    )
                }
                Switch(
                    checked = currentConfig.isTrueAmoled,
                    onCheckedChange = { isChecked ->
                        onConfigChange(currentConfig.copy(isTrueAmoled = isChecked))
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = tokens.accent,
                        checkedTrackColor = tokens.accent.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.testTag("amoled_toggle")
                )
            }

            // Reset Button
            OutlinedButton(
                onClick = {
                    onConfigChange(AppThemeConfig())
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reset_theme_button"),
                border = androidx.compose.foundation.BorderStroke(1.dp, tokens.border),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = null,
                    tint = tokens.textSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reset Theme to Default Royal Obsidian", color = tokens.textSecondary)
            }
        }
    }
}

@Composable
private fun PaletteSelectionItem(
    palette: ColorPaletteMode,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val tokens = LocalTrustColors.current
    val accent = Color(palette.primaryHex)
    val surface = Color(palette.surfaceHex)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(surface)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) accent else tokens.border,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onSelect)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(accent)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = palette.displayName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = tokens.textPrimary
            )
        }

        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(accent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
