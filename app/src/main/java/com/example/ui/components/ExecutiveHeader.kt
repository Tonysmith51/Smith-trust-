package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalTrustColors

@Composable
fun ExecutiveHeader(
    onOpenColorCustomizer: () -> Unit,
    onResetDefaults: () -> Unit,
    onOpenGeminiCopilot: (() -> Unit)? = null,
    onOpenNotifications: (() -> Unit)? = null,
    onLockVault: (() -> Unit)? = null,
    onToggleThemeMode: (() -> Unit)? = null,
    isLightMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val tokens = LocalTrustColors.current
    var showResetDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(
                1.dp,
                tokens.border,
                RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
            )
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)),
        color = tokens.surfaceElevated
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(tokens.gold, tokens.accent)
                                )
                            )
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(tokens.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Trust Crest",
                            tint = tokens.gold,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "ANTHONY SMITH",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = tokens.textPrimary,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(tokens.gold.copy(alpha = 0.2f))
                                    .border(0.5.dp, tokens.gold.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "TRUST",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = tokens.gold
                                )
                            }
                        }

                        Text(
                            text = "Sovereign Family Office • IRC §671 Grantor Trust",
                            style = MaterialTheme.typography.bodySmall,
                            color = tokens.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Gemini Copilot Button
                    if (onOpenGeminiCopilot != null) {
                        IconButton(
                            onClick = onOpenGeminiCopilot,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(tokens.accent.copy(alpha = 0.15f))
                                .testTag("header_gemini_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Gemini AI Copilot",
                                tint = tokens.accent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    // Push Notification Button
                    if (onOpenNotifications != null) {
                        IconButton(
                            onClick = onOpenNotifications,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("header_notifications_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = tokens.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Biometric Lock Vault Button
                    if (onLockVault != null) {
                        IconButton(
                            onClick = onLockVault,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("header_lock_vault_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Lock Vault",
                                tint = tokens.gold,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    if (onToggleThemeMode != null) {
                        IconButton(
                            onClick = onToggleThemeMode,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("header_toggle_theme_mode")
                        ) {
                            Icon(
                                imageVector = if (isLightMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = if (isLightMode) "Switch to Dark Mode" else "Switch to Light Mode",
                                tint = if (isLightMode) tokens.accent else tokens.gold,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onOpenColorCustomizer,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("theme_customizer_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Color Customizer",
                            tint = tokens.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { showInfoDialog = true },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("trust_info_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Trust Details",
                            tint = tokens.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text(
                    "Reset to Blueprint Defaults?",
                    color = tokens.textPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "This will restore all 8 tabs, the \$100M core allocations, 11 cards, 7 properties, fleet vehicles, and business simulations back to the pristine Smith Master Trust baseline.",
                    color = tokens.textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetDefaults()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = tokens.accent),
                    modifier = Modifier.testTag("confirm_reset_button")
                ) {
                    Text("Reset Blueprint", color = MaterialTheme.colorScheme.onPrimary)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showResetDialog = false },
                    modifier = Modifier.testTag("cancel_reset_button")
                ) {
                    Text("Cancel", color = tokens.textSecondary)
                }
            },
            containerColor = tokens.surface,
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = tokens.gold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Anthony Smith Master Trust", color = tokens.textPrimary, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "• Entity Structure: Irrevocable Sovereign Grantor Trust (IRC §671)",
                        color = tokens.textSecondary,
                        fontSize = 13.sp
                    )
                    Text(
                        "• Sovereign Jurisdiction: Leelanau County, Michigan & Delaware Court of Chancery",
                        color = tokens.textSecondary,
                        fontSize = 13.sp
                    )
                    Text(
                        "• Primary Grantor & Beneficiary: Anthony Smith",
                        color = tokens.textSecondary,
                        fontSize = 13.sp
                    )
                    Text(
                        "• Total Protected AUM: \$100M+ Fixed Income Core Vault + Real Estate & Cash Reserves",
                        color = tokens.textSecondary,
                        fontSize = 13.sp
                    )
                    Text(
                        "• Treasury Sweeps: Automated daily yield harvest sweeping to J.P. Morgan Private Bank",
                        color = tokens.textSecondary,
                        fontSize = 13.sp
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showInfoDialog = false },
                    modifier = Modifier.testTag("close_info_dialog")
                ) {
                    Text("Dismiss", color = tokens.accent)
                }
            },
            containerColor = tokens.surface,
            shape = RoundedCornerShape(20.dp)
        )
    }
}
