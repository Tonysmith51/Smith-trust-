package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TrustState
import com.example.model.BankAccount
import com.example.model.BankInstitutionCatalog
import com.example.model.BankInstitutionPreset
import com.example.ui.components.*
import com.example.ui.theme.LocalTrustColors
import com.example.util.BankLauncherHelper
import java.util.UUID

@Composable
fun BanksScreen(
    state: TrustState,
    onAddAccount: (BankAccount) -> Unit,
    onUpdateAccount: (BankAccount) -> Unit,
    onDeleteAccount: (String) -> Unit,
    isSyncing: Boolean = false,
    syncFeedback: String? = null,
    onSyncLiveFeeds: (() -> Unit)? = null,
    onRefreshAccount: ((String) -> Unit)? = null,
    onClearSyncFeedback: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val tokens = LocalTrustColors.current
    var editingAccount by remember { mutableStateOf<BankAccount?>(null) }
    var isAddingAccount by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("All") }

    val filteredAccounts = remember(state.bankAccounts, selectedFilter) {
        when (selectedFilter) {
            "Liquid CapEx" -> state.bankAccounts.filter { it.isPrimarySweep || it.category.contains("Liquid", ignoreCase = true) || it.purposeNotes.contains("houses", ignoreCase = true) }
            "Tax Escrow" -> state.bankAccounts.filter { it.category.contains("T-Bills", ignoreCase = true) || it.accountName.contains("Tax", ignoreCase = true) }
            "Custodial" -> state.bankAccounts.filter { it.category.contains("Custodial", ignoreCase = true) || it.accountName.contains("Minor", ignoreCase = true) }
            else -> state.bankAccounts
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Portfolio Liquidity & Escrow Summary Card
        item {
            StyledCard(
                backgroundColor = tokens.surfaceElevated,
                borderColor = tokens.accent.copy(alpha = 0.4f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TOTAL CASH & T-BILLS RESERVES",
                            style = MaterialTheme.typography.labelSmall,
                            color = tokens.gold,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = TrustFormatters.currency(state.totalBankBalance),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = tokens.textPrimary
                        )
                    }
                    MetricChip(
                        label = "Blended APY",
                        value = TrustFormatters.percent(state.blendedBankApy),
                        color = tokens.positive
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricChip(
                        label = "Daily Interest Yield",
                        value = "+${TrustFormatters.currency(state.totalBankInterest / 365.0)}/day",
                        color = tokens.positive,
                        modifier = Modifier.weight(1f)
                    )
                    MetricChip(
                        label = "Annual Total Yield",
                        value = "+${TrustFormatters.currency(state.totalBankInterest)}/yr",
                        color = tokens.gold,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Strategic Allocations Breakdown
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(tokens.surface)
                        .border(0.5.dp, tokens.border, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "PORTFOLIO ALLOCATION ARCHITECTURE",
                            style = MaterialTheme.typography.labelSmall,
                            color = tokens.textSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            letterSpacing = 0.5.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(tokens.gold))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Liquid CapEx Fund (J.P. Morgan)", style = MaterialTheme.typography.bodySmall, color = tokens.textPrimary, fontSize = 11.sp)
                            }
                            Text("$31,761,543 (50.9%)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = tokens.gold, fontSize = 11.sp)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(tokens.positive))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("April 15, 2027 Tax Escrow (T-Bills)", style = MaterialTheme.typography.bodySmall, color = tokens.textPrimary, fontSize = 11.sp)
                            }
                            Text("$29,101,957 (46.6%)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = tokens.positive, fontSize = 11.sp)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(tokens.accent))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Vault Yield Tax Reinvestment (T-Bills)", style = MaterialTheme.typography.bodySmall, color = tokens.textPrimary, fontSize = 11.sp)
                            }
                            Text("$1,310,000 (2.1%)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = tokens.accent, fontSize = 11.sp)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFFCE93D8)))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Aunt Amy's Minors Escrow (6 Kids)", style = MaterialTheme.typography.bodySmall, color = tokens.textPrimary, fontSize = 11.sp)
                            }
                            Text("$114,000 (0.2%)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color(0xFFCE93D8), fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Live Banking Sync & Gateway Status Bar
        item {
            LiveGatewayStatusBar(
                isSyncing = isSyncing,
                syncFeedback = syncFeedback,
                onSync = { onSyncLiveFeeds?.invoke() },
                onClearFeedback = { onClearSyncFeedback?.invoke() }
            )
        }

        // Linked Banking Apps & Portals Quick Launch Hub
        item {
            LinkedInstitutionsHub(
                onOpenAppOrPortal = { packageName, webUrl, name ->
                    BankLauncherHelper.openBankAppOrPortal(context, packageName, webUrl, name)
                },
                onLinkNewBank = { isAddingAccount = true }
            )
        }

        // Filter Chips Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "All" to "All Reserves (${state.bankAccounts.size})",
                    "Liquid CapEx" to "Liquid CapEx & JPM",
                    "Tax Escrow" to "Tax Escrow & T-Bills",
                    "Custodial" to "Minors Custodial"
                ).forEach { (key, label) ->
                    val isSelected = selectedFilter == key
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = key },
                        label = { Text(label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = tokens.accent.copy(alpha = 0.25f),
                            selectedLabelColor = tokens.accent,
                            containerColor = tokens.surface,
                            labelColor = tokens.textSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) tokens.accent else tokens.border
                        )
                    )
                }
            }
        }

        // Section Title & Add Account Button
        item {
            SectionHeader(
                title = "Treasury Accounts & Tax Escrows",
                subtitle = "J.P. Morgan Treasury Money Market, T-Bills Ladders & Custodial Escrows",
                icon = Icons.Default.AccountBalance,
                action = {
                    FilledTonalButton(
                        onClick = { isAddingAccount = true },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = tokens.accent.copy(alpha = 0.2f),
                            contentColor = tokens.accent
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("add_bank_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Link Account", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        items(filteredAccounts, key = { it.id }) { account ->
            BankAccountCard(
                account = account,
                onEdit = { editingAccount = account },
                onDelete = { onDeleteAccount(account.id) },
                onRefresh = { onRefreshAccount?.invoke(account.id) },
                onOpenApp = {
                    BankLauncherHelper.openBankAppOrPortal(
                        context,
                        account.appPackageName,
                        account.webPortalUrl,
                        account.institution
                    )
                },
                onOpenWebsite = {
                    BankLauncherHelper.openWebPortal(
                        context,
                        account.webPortalUrl,
                        "Opening ${account.institution} online banking portal..."
                    )
                }
            )
        }
    }

    // Add or Edit Dialog
    if (isAddingAccount || editingAccount != null) {
        BankEditDialog(
            account = editingAccount,
            onDismiss = {
                isAddingAccount = false
                editingAccount = null
            },
            onSave = { savedAccount ->
                if (editingAccount != null) {
                    onUpdateAccount(savedAccount)
                } else {
                    onAddAccount(savedAccount)
                }
                isAddingAccount = false
                editingAccount = null
            }
        )
    }
}

@Composable
private fun LiveGatewayStatusBar(
    isSyncing: Boolean,
    syncFeedback: String?,
    onSync: () -> Unit,
    onClearFeedback: () -> Unit
) {
    val tokens = LocalTrustColors.current
    val infiniteTransition = rememberInfiniteTransition(label = "sync_spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    ElevatedCard(
        modifier = Modifier.fillMaxWidth().testTag("live_gateway_status_bar"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = tokens.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, tokens.border, RoundedCornerShape(14.dp))
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(tokens.positive)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "LIVE SECURE BANKING GATEWAY",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = tokens.positive,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = if (isSyncing) "Handshaking with J.P. Morgan & Treasury Direct..." else "Connected to J.P. Morgan, Chase & Fed Direct",
                            style = MaterialTheme.typography.bodySmall,
                            color = tokens.textPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Button(
                    onClick = onSync,
                    enabled = !isSyncing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = tokens.accent,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(36.dp).testTag("sync_live_feeds_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Sync",
                        modifier = Modifier
                            .size(16.dp)
                            .then(if (isSyncing) Modifier.rotate(rotation) else Modifier)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isSyncing) "Syncing..." else "Sync Feeds",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            AnimatedVisibility(visible = !syncFeedback.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(tokens.positive.copy(alpha = 0.12f))
                        .border(0.5.dp, tokens.positive.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = tokens.positive, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = syncFeedback ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = tokens.textPrimary,
                                fontSize = 11.sp
                            )
                        }
                        IconButton(
                            onClick = onClearFeedback,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = tokens.textSecondary, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LinkedInstitutionsHub(
    onOpenAppOrPortal: (packageName: String, webUrl: String, institutionName: String) -> Unit,
    onLinkNewBank: () -> Unit
) {
    val tokens = LocalTrustColors.current

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "LINKED BANKING APPS & SECURE WEBSITES",
                style = MaterialTheme.typography.labelSmall,
                color = tokens.gold,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                letterSpacing = 0.5.sp
            )
            Text(
                text = "Tap to Launch",
                style = MaterialTheme.typography.labelSmall,
                color = tokens.textSecondary,
                fontSize = 10.sp
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // J.P. Morgan Private Bank
            BankingQuickLaunchChip(
                name = "J.P. Morgan",
                sub = "Private Bank",
                badge = "Primary Liquidity",
                badgeColor = tokens.gold,
                packageName = "com.jpmorgan.privatebank",
                webUrl = "https://privatebank.jpmorgan.com/gl/en/log-in",
                onLaunch = onOpenAppOrPortal
            )

            // Chase Mobile
            BankingQuickLaunchChip(
                name = "Chase",
                sub = "Mobile & Sapphire",
                badge = "Credit & Operating",
                badgeColor = tokens.accent,
                packageName = "com.chase.sig.android",
                webUrl = "https://secure.chase.com",
                onLaunch = onOpenAppOrPortal
            )

            // U.S. TreasuryDirect
            BankingQuickLaunchChip(
                name = "TreasuryDirect",
                sub = "Fed Debt Portal",
                badge = "T-Bills Escrows",
                badgeColor = tokens.positive,
                packageName = "gov.treasury.treasurydirect",
                webUrl = "https://www.treasurydirect.gov/RS/PWOLGate",
                onLaunch = onOpenAppOrPortal
            )

            // Link Another Bank Action Card
            Box(
                modifier = Modifier
                    .width(130.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(tokens.surface)
                    .border(1.dp, tokens.border, RoundedCornerShape(12.dp))
                    .clickable(onClick = onLinkNewBank)
                    .padding(12.dp)
                    .testTag("hub_link_another_bank"),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(tokens.accent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AddLink, contentDescription = null, tint = tokens.accent, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("+ Link Bank", fontWeight = FontWeight.Bold, color = tokens.textPrimary, fontSize = 11.sp)
                    Text("Any Institution", color = tokens.textSecondary, fontSize = 9.sp)
                }
            }
        }
    }
}

@Composable
private fun BankingQuickLaunchChip(
    name: String,
    sub: String,
    badge: String,
    badgeColor: Color,
    packageName: String,
    webUrl: String,
    onLaunch: (packageName: String, webUrl: String, institutionName: String) -> Unit
) {
    val tokens = LocalTrustColors.current
    val context = LocalContext.current
    val isInstalled = remember(packageName) { BankLauncherHelper.isAppInstalled(context, packageName) }

    Box(
        modifier = Modifier
            .width(150.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(tokens.surface)
            .border(1.dp, badgeColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .clickable { onLaunch(packageName, webUrl, name) }
            .padding(12.dp)
            .testTag("quick_launch_${name.lowercase().replace(" ", "_")}")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(badgeColor.copy(alpha = 0.18f))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(badge, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = badgeColor)
                }
                Icon(
                    imageVector = if (isInstalled) Icons.Default.PhoneAndroid else Icons.Default.OpenInNew,
                    contentDescription = null,
                    tint = tokens.textSecondary,
                    modifier = Modifier.size(14.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = name,
                fontWeight = FontWeight.Black,
                color = tokens.textPrimary,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = sub,
                color = tokens.textSecondary,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isInstalled) "Open App" else "Open Website",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = badgeColor
                )
                Spacer(modifier = Modifier.width(3.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = badgeColor,
                    modifier = Modifier.size(10.dp)
                )
            }
        }
    }
}

@Composable
private fun BankAccountCard(
    account: BankAccount,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onRefresh: () -> Unit,
    onOpenApp: () -> Unit,
    onOpenWebsite: () -> Unit
) {
    val tokens = LocalTrustColors.current
    val context = LocalContext.current
    val isAppInstalled = remember(account.appPackageName) {
        BankLauncherHelper.isAppInstalled(context, account.appPackageName)
    }

    val isTBillTaxEscrow = account.accountName.contains("April 15", ignoreCase = true) || account.maturityDate?.contains("2027") == true
    val isVaultTaxSweep = account.accountName.contains("Vault Yield", ignoreCase = true)
    val isLiquidReserve = account.isPrimarySweep || account.category.contains("Liquid", ignoreCase = true)

    val cardBorderColor = when {
        isLiquidReserve -> tokens.gold.copy(alpha = 0.5f)
        isTBillTaxEscrow -> tokens.positive.copy(alpha = 0.5f)
        isVaultTaxSweep -> tokens.accent.copy(alpha = 0.5f)
        else -> tokens.border
    }

    StyledCard(borderColor = cardBorderColor) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Header tags row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    if (account.isPrimarySweep) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(tokens.gold.copy(alpha = 0.2f))
                                .border(0.5.dp, tokens.gold.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = tokens.gold, modifier = Modifier.size(10.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("PRIMARY LIQUID SWEEP", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = tokens.gold)
                            }
                        }
                    }

                    if (isTBillTaxEscrow) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(tokens.positive.copy(alpha = 0.18f))
                                .border(0.5.dp, tokens.positive.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = tokens.positive, modifier = Modifier.size(10.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("TAX ESCROW • DUE APR 15, 2027", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = tokens.positive)
                            }
                        }
                    }

                    if (isVaultTaxSweep) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(tokens.accent.copy(alpha = 0.18f))
                                .border(0.5.dp, tokens.accent.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = tokens.accent, modifier = Modifier.size(10.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("37% VAULT INTEREST TAX SWEEP", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = tokens.accent)
                            }
                        }
                    }
                }

                Text(
                    text = account.institution,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = tokens.textPrimary
                )

                Text(
                    text = "${account.accountName} • ${account.accountNumberMask}",
                    style = MaterialTheme.typography.bodySmall,
                    color = tokens.textSecondary
                )
            }

            Row {
                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier.size(32.dp).testTag("refresh_bank_${account.id}")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh Feed", tint = tokens.accent, modifier = Modifier.size(16.dp))
                }
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(32.dp).testTag("edit_bank_${account.id}")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = tokens.textSecondary, modifier = Modifier.size(16.dp))
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp).testTag("delete_bank_${account.id}")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = tokens.negative.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Live Feed Status Indicator Pill
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(if (account.isLiveConnected) tokens.positive else tokens.gold)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = account.lastSyncTime.ifBlank { "Live Connected" },
                style = MaterialTheme.typography.labelSmall,
                color = tokens.textSecondary,
                fontSize = 10.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Balance & APY
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = "RESERVE BALANCE",
                    style = MaterialTheme.typography.labelSmall,
                    color = tokens.textSecondary,
                    fontSize = 10.sp
                )
                Text(
                    text = TrustFormatters.currency(account.balance),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = tokens.textPrimary
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${TrustFormatters.percent(account.apy)} APY",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = tokens.positive
                )
                Text(
                    text = "+${TrustFormatters.currency(account.dailyInterest)}/day • +${TrustFormatters.currency(account.annualInterest)}/yr",
                    style = MaterialTheme.typography.bodySmall,
                    color = tokens.textSecondary,
                    fontSize = 11.sp
                )
            }
        }

        // Direct App & Website Portal Launch Bar
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Open App Button
            Button(
                onClick = onOpenApp,
                colors = ButtonDefaults.buttonColors(
                    containerColor = tokens.accent.copy(alpha = 0.15f),
                    contentColor = tokens.accent
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f).height(36.dp).testTag("open_app_button_${account.id}"),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Icon(
                    imageVector = if (isAppInstalled) Icons.Default.PhoneAndroid else Icons.Default.Launch,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isAppInstalled) "Open App" else "Launch Portal",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }

            // Open Website Button
            OutlinedButton(
                onClick = onOpenWebsite,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = tokens.textPrimary),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.horizontalGradient(listOf(tokens.border, tokens.border))),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f).height(36.dp).testTag("open_website_button_${account.id}"),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Open Website", fontSize = 11.sp, fontWeight = FontWeight.Medium, maxLines = 1)
            }
        }

        // Purpose & Maturity Details Callout
        if (account.purposeNotes.isNotBlank() || !account.maturityDate.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(tokens.surface)
                    .border(0.5.dp, tokens.border.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (!account.maturityDate.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (account.maturityDate.contains("April 15, 2027")) Icons.Default.CalendarToday else Icons.Default.FlashOn,
                                contentDescription = null,
                                tint = if (account.maturityDate.contains("April 15, 2027")) tokens.positive else tokens.gold,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Maturity / Liquidity: ${account.maturityDate}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (account.maturityDate.contains("April 15, 2027")) tokens.positive else tokens.gold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    if (account.purposeNotes.isNotBlank()) {
                        Text(
                            text = account.purposeNotes,
                            style = MaterialTheme.typography.bodySmall,
                            color = tokens.textSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }

                    // Strategic helper tags
                    if (isLiquidReserve) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            AssistChip(
                                onClick = {},
                                label = { Text("Cars & Fleet CapEx", fontSize = 10.sp) },
                                leadingIcon = { Icon(Icons.Default.DirectionsCar, contentDescription = null, modifier = Modifier.size(12.dp)) },
                                modifier = Modifier.height(26.dp)
                            )
                            AssistChip(
                                onClick = {},
                                label = { Text("Real Estate Cash", fontSize = 10.sp) },
                                leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(12.dp)) },
                                modifier = Modifier.height(26.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BankEditDialog(
    account: BankAccount?,
    onDismiss: () -> Unit,
    onSave: (BankAccount) -> Unit
) {
    val context = LocalContext.current
    val tokens = LocalTrustColors.current

    var institution by remember { mutableStateOf(account?.institution ?: "") }
    var accountName by remember { mutableStateOf(account?.accountName ?: "") }
    var accountMask by remember { mutableStateOf(account?.accountNumberMask ?: "•••• ") }
    var balanceText by remember { mutableStateOf(account?.balance?.toLong()?.toString() ?: "1000000") }
    var apyText by remember { mutableStateOf(account?.apy?.toString() ?: "4.85") }
    var category by remember { mutableStateOf(account?.category ?: "Treasury Money Market") }
    var maturityDate by remember { mutableStateOf(account?.maturityDate ?: "") }
    var purposeNotes by remember { mutableStateOf(account?.purposeNotes ?: "") }
    var webPortalUrl by remember { mutableStateOf(account?.webPortalUrl ?: "") }
    var appPackageName by remember { mutableStateOf(account?.appPackageName ?: "") }
    var isPrimary by remember { mutableStateOf(account?.isPrimarySweep ?: false) }

    fun applyPreset(preset: BankInstitutionPreset) {
        institution = preset.name
        if (accountName.isBlank() || accountName == "Treasury Cash Reserves") {
            accountName = preset.defaultAccountName
        }
        if (webPortalUrl.isBlank() || webPortalUrl.contains("chase.com") || webPortalUrl.contains("jpmorgan.com")) {
            webPortalUrl = preset.webPortalUrl
        }
        if (appPackageName.isBlank()) {
            appPackageName = preset.appPackageName
        }
        category = preset.defaultCategory
        apyText = preset.defaultApy.toString()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (account != null) "Edit Account & App Link" else "Link Bank & Institution",
                color = tokens.textPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Preset Selector
                Text(
                    text = "SELECT INSTITUTION PRESET",
                    style = MaterialTheme.typography.labelSmall,
                    color = tokens.gold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    BankInstitutionCatalog.presets.take(7).forEach { preset ->
                        FilterChip(
                            selected = institution.contains(preset.shortName, ignoreCase = true),
                            onClick = { applyPreset(preset) },
                            label = { Text(preset.shortName, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = tokens.accent.copy(alpha = 0.25f),
                                selectedLabelColor = tokens.accent,
                                containerColor = tokens.surface,
                                labelColor = tokens.textSecondary
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = institution,
                    onValueChange = { institution = it },
                    label = { Text("Financial Institution") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("bank_institution_input")
                )
                OutlinedTextField(
                    value = accountName,
                    onValueChange = { accountName = it },
                    label = { Text("Account Name / Fund") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = accountMask,
                    onValueChange = { accountMask = it },
                    label = { Text("Account Number Mask (e.g. •••• 9821)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = balanceText,
                    onValueChange = { balanceText = it },
                    label = { Text("Live Balance ($)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("bank_balance_input")
                )
                OutlinedTextField(
                    value = apyText,
                    onValueChange = { apyText = it },
                    label = { Text("APY Rate (%)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Direct Portal & App Link Inputs
                Text(
                    text = "APP LINKING & ONLINE BANKING WEBSITE",
                    style = MaterialTheme.typography.labelSmall,
                    color = tokens.gold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
                OutlinedTextField(
                    value = webPortalUrl,
                    onValueChange = { webPortalUrl = it },
                    label = { Text("Official Web Portal URL (https://...)") },
                    placeholder = { Text("https://secure.chase.com") },
                    leadingIcon = { Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("bank_web_portal_input")
                )
                OutlinedTextField(
                    value = appPackageName,
                    onValueChange = { appPackageName = it },
                    label = { Text("Android App Package Name") },
                    placeholder = { Text("com.chase.sig.android or com.jpmorgan.privatebank") },
                    leadingIcon = { Icon(Icons.Default.PhoneAndroid, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("bank_app_package_input")
                )

                // Test Launch Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = {
                            BankLauncherHelper.openWebPortal(context, webPortalUrl)
                        },
                        modifier = Modifier.weight(1f).height(36.dp)
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Test Website", fontSize = 11.sp)
                    }

                    FilledTonalButton(
                        onClick = {
                            BankLauncherHelper.openBankAppOrPortal(context, appPackageName, webPortalUrl, institution)
                        },
                        modifier = Modifier.weight(1f).height(36.dp)
                    ) {
                        Icon(Icons.Default.PhoneAndroid, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Test App", fontSize = 11.sp)
                    }
                }

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category (e.g. Treasury Money Market, T-Bills)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = maturityDate,
                    onValueChange = { maturityDate = it },
                    label = { Text("Maturity Date (e.g. April 15, 2027 or Daily)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = purposeNotes,
                    onValueChange = { purposeNotes = it },
                    label = { Text("Purpose Notes (e.g. Houses, Cars, Tax Escrow)") },
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = isPrimary,
                        onCheckedChange = { isPrimary = it },
                        colors = CheckboxDefaults.colors(checkedColor = tokens.accent)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Designate as Primary Liquid Sweep Target", color = tokens.textPrimary, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val bal = balanceText.toDoubleOrNull() ?: (account?.balance ?: 0.0)
                    val apy = apyText.toDoubleOrNull() ?: (account?.apy ?: 0.0)
                    val updated = BankAccount(
                        id = account?.id ?: UUID.randomUUID().toString(),
                        institution = institution.ifBlank { "J.P. Morgan Private Bank" },
                        accountName = accountName.ifBlank { "Treasury Cash Reserves" },
                        accountNumberMask = accountMask.ifBlank { "•••• 0000" },
                        balance = bal,
                        apy = apy,
                        isPrimarySweep = isPrimary,
                        category = category.ifBlank { "Treasury Money Market" },
                        maturityDate = maturityDate.ifBlank { null },
                        purposeNotes = purposeNotes,
                        webPortalUrl = webPortalUrl.ifBlank { "https://privatebank.jpmorgan.com/gl/en/log-in" },
                        appPackageName = appPackageName.ifBlank { "com.jpmorgan.privatebank" },
                        lastSyncTime = account?.lastSyncTime ?: "Live Connected • Just now",
                        isLiveConnected = true
                    )
                    onSave(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = tokens.accent),
                modifier = Modifier.testTag("save_bank_button")
            ) {
                Text("Save Account")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = tokens.textSecondary)
            }
        },
        containerColor = tokens.surface,
        shape = RoundedCornerShape(20.dp)
    )
}
