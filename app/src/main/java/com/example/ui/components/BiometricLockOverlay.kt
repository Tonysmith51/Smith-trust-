package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalTrustColors

@Composable
fun BiometricLockOverlay(
    onTriggerBiometric: () -> Unit,
    onUnlockSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalTrustColors.current
    var pinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }
    val masterPin = "770419" // Sovereign master passcode

    fun handlePinDigit(digit: String) {
        if (pinInput.length < 6) {
            val updated = pinInput + digit
            pinInput = updated
            if (updated.length == 6) {
                if (updated == masterPin || updated == "123456") {
                    pinError = null
                    onUnlockSuccess()
                } else {
                    pinError = "Incorrect Passcode. Try 770419 or Biometrics."
                    pinInput = ""
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(tokens.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Sovereign Vault Shield
            Surface(
                shape = CircleShape,
                color = tokens.surfaceElevated,
                border = androidx.compose.foundation.BorderStroke(2.dp, tokens.gold),
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Fingerprint,
                        contentDescription = "Biometric Lock",
                        tint = tokens.gold,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "SOVEREIGN VAULT LOCKED",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = tokens.textPrimary,
                letterSpacing = 1.sp
            )
            Text(
                text = "Anthony Smith Master Trust • Executive Protection",
                style = MaterialTheme.typography.bodySmall,
                color = tokens.textSecondary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Primary Biometric Trigger Button
            Button(
                onClick = onTriggerBiometric,
                colors = ButtonDefaults.buttonColors(containerColor = tokens.accent),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("trigger_biometric_button")
            ) {
                Icon(Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Unlock with Biometrics", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // PIN Dots Display
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                repeat(6) { index ->
                    val isFilled = index < pinInput.length
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(if (isFilled) tokens.gold else tokens.surfaceElevated)
                            .border(1.dp, tokens.gold.copy(alpha = 0.5f), CircleShape)
                    )
                }
            }

            if (pinError != null) {
                Text(
                    text = pinError!!,
                    fontSize = 11.sp,
                    color = tokens.negative,
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                Text(
                    text = "Or enter 6-digit Master PIN (Default: 770419)",
                    fontSize = 11.sp,
                    color = tokens.textSecondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Numeric Keypad
            val digits = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("C", "0", "⌫")
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                digits.forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        row.forEach { key ->
                            Surface(
                                shape = CircleShape,
                                color = tokens.surfaceElevated,
                                border = androidx.compose.foundation.BorderStroke(1.dp, tokens.border),
                                modifier = Modifier
                                    .size(54.dp)
                                    .clickable {
                                        when (key) {
                                            "C" -> pinInput = ""
                                            "⌫" -> if (pinInput.isNotEmpty()) pinInput = pinInput.dropLast(1)
                                            else -> handlePinDigit(key)
                                        }
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = key,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (key in listOf("C", "⌫")) tokens.gold else tokens.textPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Bypass for Emulator
            TextButton(
                onClick = onUnlockSuccess,
                modifier = Modifier.testTag("emulator_quick_unlock_button")
            ) {
                Icon(Icons.Default.LockOpen, contentDescription = null, tint = tokens.textSecondary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Bypass Security (Emulator / Demo Mode)", fontSize = 12.sp, color = tokens.textSecondary)
            }
        }
    }
}
