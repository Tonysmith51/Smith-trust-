package com.example.ui.components

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TrustState
import com.example.ui.theme.LocalTrustColors
import com.example.util.NotificationHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCenterSheet(
    state: TrustState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val tokens = LocalTrustColors.current
    var hasPermission by remember { mutableStateOf(NotificationHelper.hasNotificationPermission(context)) }
    var lastTriggeredMessage by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (isGranted) {
            NotificationHelper.initNotificationChannels(context)
            NotificationHelper.sendNotification(
                context = context,
                channelId = NotificationHelper.CHANNEL_SOVEREIGN_ALERTS,
                notificationId = 100,
                title = "Push Notifications Active",
                message = "Anthony Smith Master Trust executive notification channels enabled."
            )
            lastTriggeredMessage = "Notifications enabled! Sent welcome push alert."
        } else {
            lastTriggeredMessage = "Notification permission not granted."
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = tokens.surface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = tokens.gold) },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = tokens.accent.copy(alpha = 0.2f),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = tokens.accent, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Executive Push Notifications",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = tokens.textPrimary
                        )
                        Text(
                            text = if (hasPermission) "Status: Enabled & Active" else "Status: Permission Needed",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (hasPermission) tokens.positive else tokens.gold
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = tokens.textSecondary)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Permission Request Card if not enabled
            if (!hasPermission) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = tokens.gold.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, tokens.gold.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Enable System Alerts",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = tokens.textPrimary
                            )
                            Text(
                                text = "Allow Android system push alerts for cash sweeps & flight checks.",
                                fontSize = 11.sp,
                                color = tokens.textSecondary
                            )
                        }

                        Button(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    hasPermission = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = tokens.gold),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("request_notification_permission_button")
                        ) {
                            Text("Enable", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            if (lastTriggeredMessage != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = tokens.positive.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, tokens.positive.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = lastTriggeredMessage!!,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = tokens.positive,
                        modifier = Modifier.padding(10.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Text(
                text = "Simulate Real-Time Sovereign Alerts:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = tokens.textPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Action 1: Treasury Sweep
            OutlinedButton(
                onClick = {
                    val sent = NotificationHelper.sendSweepNotification(
                        context,
                        amount = 312500.0,
                        accountName = "J.P. Morgan Master Operating (•••• 9821)"
                    )
                    lastTriggeredMessage = if (sent) "✓ Push Notification sent: \$312,500 Sweep Credited" else "Failed to send notification (Check permissions)"
                },
                modifier = Modifier.fillMaxWidth().testTag("simulate_sweep_notification_button"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Simulate \$312.5k Monthly Treasury Sweep Alert", fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action 2: Aircraft Maintenance Alert
            OutlinedButton(
                onClick = {
                    val sent = NotificationHelper.sendMaintenanceDueNotification(
                        context,
                        assetName = "2024 Bell 505 Jet Ranger X (Tail N505ST)",
                        task = "100-Hr Turbomeca Rotor & Turbine Check",
                        dueDate = "Nov 25, 2026"
                    )
                    lastTriggeredMessage = if (sent) "✓ Push Notification sent: Bell 505 Maintenance Alert" else "Failed to send notification"
                },
                modifier = Modifier.fillMaxWidth().testTag("simulate_maintenance_notification_button"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Flight, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Simulate Bell 505 Helicopter Service Due Alert", fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action 3: Family Card Spend Alert
            OutlinedButton(
                onClick = {
                    val sent = NotificationHelper.sendCardSpendNotification(
                        context,
                        cardholder = "Lana",
                        spend = 11400.0,
                        remaining = 7600.0
                    )
                    lastTriggeredMessage = if (sent) "✓ Push Notification sent: Lana's Sapphire Reserve Alert" else "Failed to send notification"
                },
                modifier = Modifier.fillMaxWidth().testTag("simulate_card_notification_button"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Simulate Lana's Sapphire Reserve Spend Alert", fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
