package com.example.ui.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.BankLauncherHelper
import com.example.data.MockVaultAccountDataProvider
import com.example.data.TrustState
import com.example.data.VaultAccountDataProvider
import com.example.model.CompoundingSimState
import com.example.model.LoanItem
import com.example.model.VaultAccount
import com.example.model.VaultAccountCategory
import com.example.model.VaultLeg
import com.example.ui.components.*
import com.example.ui.theme.LocalTrustColors
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultScreen(
    state: TrustState,
    onUpdateLeg: (index: Int, leg: VaultLeg) -> Unit,
    onUpdateCompounding: (CompoundingSimState) -> Unit,
    onUpdateLoan: ((LoanItem) -> Unit)? = null,
    onUpdateAccount: ((VaultAccount) -> Unit)? = null,
    onAddAccount: ((VaultAccount) -> Unit)? = null,
    onDeleteAccount: ((String) -> Unit)? = null,
    accountDataProvider: VaultAccountDataProvider = remember { MockVaultAccountDataProvider() },
    modifier: Modifier = Modifier
) {
    val tokens = LocalTrustColors.current
    val accounts by accountDataProvider.accountsFlow.collectAsState()
    var selectedCategory by remember { mutableStateOf(VaultAccountCategory.ALL) }
    var selectedAccountDetail by remember { mutableStateOf<VaultAccount?>(null) }
    var editingAccount by remember { mutableStateOf<VaultAccount?>(null) }
    var isAddingAccount by remember { mutableStateOf(false) }
    var editingLegIndex by remember { mutableStateOf<Int?>(null) }

    val totalVaultAccountsBalance = remember(accounts) { accounts.sumOf { it.balance } }
    val totalAnnualHarvest = remember(accounts) { accounts.sumOf { it.annualYield } }
    val blendedYield = remember(totalVaultAccountsBalance, totalAnnualHarvest) {
        if (totalVaultAccountsBalance > 0) (totalAnnualHarvest / totalVaultAccountsBalance) * 100.0 else 0.0
    }

    val filteredAccounts = remember(accounts, selectedCategory) {
        if (selectedCategory == VaultAccountCategory.ALL) {
            accounts
        } else {
            accounts.filter { it.category == selectedCategory }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Core Vault Master Portfolio Dashboard Card (Material 3 Elevated Card)
        item {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("core_vault_summary_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = tokens.surfaceElevated
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, tokens.gold.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(tokens.positive)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "CORE VAULT MASTER BALANCE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = tokens.gold,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = TrustFormatters.currency(totalVaultAccountsBalance),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = tokens.textPrimary
                            )
                        }

                        MetricChip(
                            label = "Blended Yield",
                            value = TrustFormatters.percent(blendedYield),
                            color = tokens.positive
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Secondary Summary Metrics Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricChip(
                            label = "Annual Inflow",
                            value = "+${TrustFormatters.compactCurrency(totalAnnualHarvest)}/yr",
                            color = tokens.accent,
                            modifier = Modifier.weight(1f)
                        )
                        MetricChip(
                            label = "Monthly Harvest",
                            value = "+${TrustFormatters.currency(totalAnnualHarvest / 12.0)}/mo",
                            color = tokens.gold,
                            modifier = Modifier.weight(1f)
                        )
                        MetricChip(
                            label = "Accounts",
                            value = "${accounts.size} Active",
                            color = tokens.textPrimary,
                            modifier = Modifier.weight(0.7f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Portfolio Category Allocation Distribution Bar
                    val fixedTotal = remember(accounts) { accounts.filter { it.category == VaultAccountCategory.FIXED_INCOME }.sumOf { it.balance } }
                    val cashTotal = remember(accounts) { accounts.filter { it.category == VaultAccountCategory.LIQUID_CASH }.sumOf { it.balance } }
                    val collateralTotal = remember(accounts) { accounts.filter { it.category == VaultAccountCategory.STRATEGIC_COLLATERAL }.sumOf { it.balance } }
                    val escrowTotal = remember(accounts) { accounts.filter { it.category == VaultAccountCategory.ESCROW_RESERVES }.sumOf { it.balance } }

                    val fixedPct = if (totalVaultAccountsBalance > 0) (fixedTotal / totalVaultAccountsBalance).toFloat().coerceIn(0f, 1f) else 0.6f
                    val cashPct = if (totalVaultAccountsBalance > 0) (cashTotal / totalVaultAccountsBalance).toFloat().coerceIn(0f, 1f) else 0.38f
                    val collatPct = if (totalVaultAccountsBalance > 0) (collateralTotal / totalVaultAccountsBalance).toFloat().coerceIn(0f, 1f) else 0.01f
                    val escrowPct = if (totalVaultAccountsBalance > 0) (escrowTotal / totalVaultAccountsBalance).toFloat().coerceIn(0f, 1f) else 0.01f

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Portfolio Asset Allocation",
                                style = MaterialTheme.typography.bodySmall,
                                color = tokens.textSecondary,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "Fixed Income ${(fixedPct * 100).toInt()}% • Banks & Cash ${(cashPct * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = tokens.textPrimary,
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(tokens.border)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(fixedPct.coerceAtLeast(0.01f))
                                    .background(tokens.gold)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(cashPct.coerceAtLeast(0.01f))
                                    .background(tokens.accent)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(collatPct.coerceAtLeast(0.01f))
                                    .background(tokens.positive)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(escrowPct.coerceAtLeast(0.01f))
                                    .background(tokens.textSecondary)
                            )
                        }
                    }
                }
            }
        }

        // 2. J.P. Morgan Treasury Account Monthly Spending Trends (D3 Line Chart)
        item {
            val jpmBalance = remember(state.bankAccounts) {
                state.bankAccounts.find { it.id == "bank_jpm_liquid" }?.balance ?: 31_761_543.0
            }
            JPMorganSpendingTrendChart(
                treasuryBalance = jpmBalance
            )
        }

        // 3. Interactive Category Filter Chips & Add Account Button
        item {
            SectionHeader(
                title = "Account Balances & Breakdown",
                subtitle = "Summary balances across custodians, money markets & fixed reserves",
                icon = Icons.Default.AccountBalance,
                action = {
                    FilledTonalButton(
                        onClick = { isAddingAccount = true },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = tokens.accent.copy(alpha = 0.2f),
                            contentColor = tokens.accent
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("add_vault_account_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Account", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VaultAccountCategory.values().forEach { category ->
                    val isSelected = selectedCategory == category
                    val count = if (category == VaultAccountCategory.ALL) accounts.size
                    else accounts.count { it.category == category }

                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = category },
                        label = {
                            Text(
                                text = "${category.displayName} ($count)",
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = tokens.accent,
                            selectedLabelColor = Color.White,
                            containerColor = tokens.surfaceElevated,
                            labelColor = tokens.textSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) tokens.accent else tokens.border,
                            selectedBorderColor = tokens.accent
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("filter_chip_${category.name.lowercase()}")
                    )
                }
            }
        }

        // 4. Material 3 Account Summary Cards List with Edit & Delete Actions
        items(filteredAccounts, key = { it.id }) { account ->
            VaultAccountSummaryCard(
                account = account,
                totalVaultBalance = totalVaultAccountsBalance,
                onClick = { selectedAccountDetail = account },
                onEdit = { editingAccount = account },
                onDelete = {
                    if (onDeleteAccount != null) {
                        onDeleteAccount(account.id)
                    } else {
                        accountDataProvider.deleteAccount(account.id)
                    }
                }
            )
        }

        // 5. Family Entity Loan Ledger Table
        item {
            Spacer(modifier = Modifier.height(4.dp))
            LoanLedgerTable(
                loans = state.loans,
                onUpdateLoan = { onUpdateLoan?.invoke(it) }
            )
        }

        // 6. Section: 3-Legged Fixed Income Allocation Controls
        item {
            Spacer(modifier = Modifier.height(8.dp))
            SectionHeader(
                title = "3-Legged Fixed Income Engine",
                subtitle = "Fine-tune capital allocation and yield rates in real time",
                icon = Icons.Default.Shield
            )
        }

        items(state.vaultLegs.size) { index ->
            val leg = state.vaultLegs[index]
            VaultLegCard(
                leg = leg,
                onUpdate = { updatedLeg -> onUpdateLeg(index, updatedLeg) },
                onEditExact = { editingLegIndex = index }
            )
        }

        // 7. Section: Automated Cash Sweep Breakdown
        item {
            SectionHeader(
                title = "Automated Cash Sweep Mechanism",
                subtitle = "Direct scheduled cash distributions into J.P. Morgan Private Bank",
                icon = Icons.Default.Schedule
            )

            StyledCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint = tokens.gold,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "J.P. Morgan Private Bank Master Operating",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = tokens.textPrimary
                        )
                        Text(
                            text = "Primary Sweep Target • Acct •••• 9821",
                            style = MaterialTheme.typography.bodySmall,
                            color = tokens.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SweepCadenceBox(
                        cadence = "Daily Sweep",
                        amount = TrustFormatters.currency(state.dailyCashSweep),
                        modifier = Modifier.weight(1f)
                    )
                    SweepCadenceBox(
                        cadence = "Monthly Sweep",
                        amount = TrustFormatters.currency(state.monthlyCashSweep),
                        modifier = Modifier.weight(1f)
                    )
                    SweepCadenceBox(
                        cadence = "Quarterly Sweep",
                        amount = TrustFormatters.currency(state.quarterlyCashSweep),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 8. Section: Sovereign Compounding Visualizer
        item {
            SectionHeader(
                title = "Sovereign Compounding Visualizer",
                subtitle = "Model multi-generational wealth preservation & lifestyle spend",
                icon = Icons.Default.TrendingUp
            )

            CompoundingVisualizerCard(
                state = state,
                onUpdate = onUpdateCompounding
            )
        }
    }

    // Account Detail Dialog
    selectedAccountDetail?.let { detail ->
        AccountDetailDialog(
            account = detail,
            totalVaultBalance = totalVaultAccountsBalance,
            onDismiss = { selectedAccountDetail = null },
            onEdit = {
                editingAccount = detail
                selectedAccountDetail = null
            }
        )
    }

    // Edit or Add Vault Account Dialog
    if (editingAccount != null || isAddingAccount) {
        EditVaultAccountDialog(
            account = editingAccount,
            onDismiss = {
                editingAccount = null
                isAddingAccount = false
            },
            onSave = { saved ->
                if (editingAccount != null) {
                    if (onUpdateAccount != null) {
                        onUpdateAccount(saved)
                    } else {
                        accountDataProvider.updateAccount(saved)
                    }
                } else {
                    if (onAddAccount != null) {
                        onAddAccount(saved)
                    } else {
                        accountDataProvider.addAccount(saved)
                    }
                }
                editingAccount = null
                isAddingAccount = false
            }
        )
    }

    // Edit Fixed Income Leg Dialog
    editingLegIndex?.let { index ->
        val leg = state.vaultLegs.getOrNull(index)
        if (leg != null) {
            EditVaultLegDialog(
                leg = leg,
                onDismiss = { editingLegIndex = null },
                onSave = { updatedLeg ->
                    onUpdateLeg(index, updatedLeg)
                    // Also sync with accountDataProvider fixed income item
                    val matchingId = when (index) {
                        0 -> "va_tbills"
                        1 -> "va_treasuries"
                        2 -> "va_munis"
                        else -> null
                    }
                    if (matchingId != null) {
                        val existing = accounts.find { it.id == matchingId }
                        if (existing != null) {
                            accountDataProvider.updateAccount(existing.copy(
                                name = updatedLeg.name,
                                balance = updatedLeg.allocation,
                                apy = updatedLeg.apy
                            ))
                        }
                    }
                    editingLegIndex = null
                }
            )
        }
    }
}

@Composable
private fun VaultAccountSummaryCard(
    account: VaultAccount,
    totalVaultBalance: Double,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val tokens = LocalTrustColors.current
    val sharePct = if (totalVaultBalance > 0) ((account.balance / totalVaultBalance) * 100) else 0.0

    val categoryColor = when (account.category) {
        VaultAccountCategory.FIXED_INCOME -> tokens.gold
        VaultAccountCategory.LIQUID_CASH -> tokens.accent
        VaultAccountCategory.STRATEGIC_COLLATERAL -> tokens.positive
        VaultAccountCategory.ESCROW_RESERVES -> tokens.accentSecondary
        VaultAccountCategory.ALL -> tokens.textPrimary
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("account_card_${account.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = tokens.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, tokens.border, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            // Header: Account Title, Institution & Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = account.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = tokens.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${account.institution} • ${account.accountMask}",
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(categoryColor.copy(alpha = 0.18f))
                            .border(0.5.dp, categoryColor.copy(alpha = 0.45f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = account.status.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = categoryColor
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(30.dp).testTag("edit_vault_account_${account.id}")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Account", tint = tokens.textSecondary, modifier = Modifier.size(15.dp))
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(30.dp).testTag("delete_vault_account_${account.id}")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Account", tint = tokens.negative.copy(alpha = 0.7f), modifier = Modifier.size(15.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Body: Balance & Earnings
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "ACCOUNT BALANCE",
                        style = MaterialTheme.typography.labelSmall,
                        color = tokens.textSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = TrustFormatters.currency(account.balance),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = tokens.textPrimary,
                        fontSize = 20.sp
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${TrustFormatters.percent(account.apy)} APY",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            color = tokens.positive
                        )
                        if (account.isTaxExempt) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(tokens.gold.copy(alpha = 0.2f))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text("TAX-FREE", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = tokens.gold)
                            }
                        }
                    }
                    Text(
                        text = "+${TrustFormatters.currency(account.monthlyYield)}/mo",
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.textSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer: Share of Total Portfolio & Progress Indicator
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Vault Portfolio Share",
                        style = MaterialTheme.typography.labelSmall,
                        color = tokens.textSecondary,
                        fontSize = 10.sp
                    )
                    Text(
                        text = String.format("%.2f%%", sharePct),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = categoryColor,
                        fontSize = 10.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { ((sharePct / 100.0).toFloat()).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = categoryColor,
                    trackColor = tokens.border
                )
            }
        }
    }
}

@Composable
private fun VaultLegCard(
    leg: VaultLeg,
    onUpdate: (VaultLeg) -> Unit,
    onEditExact: () -> Unit
) {
    val tokens = LocalTrustColors.current

    StyledCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = leg.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = tokens.textPrimary
                    )
                    IconButton(
                        onClick = onEditExact,
                        modifier = Modifier.size(28.dp).testTag("edit_leg_exact_${leg.id}")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Exact Figures", tint = tokens.textSecondary, modifier = Modifier.size(14.dp))
                    }
                }
                Text(
                    text = leg.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = tokens.textSecondary
                )
            }
            Text(
                text = "+${TrustFormatters.currency(leg.annualIncome)}/yr",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = tokens.positive
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Allocation Slider
        LabeledSlider(
            label = "Capital Allocation",
            valueText = TrustFormatters.compactCurrency(leg.allocation),
            value = (leg.allocation / 1_000_000.0).toFloat(),
            onValueChange = { newMillions ->
                onUpdate(leg.copy(allocation = newMillions.toDouble() * 1_000_000.0))
            },
            valueRange = 5f..60f,
            steps = 55,
            accentColor = tokens.accent,
            modifier = Modifier.testTag("allocation_slider_${leg.id}")
        )

        Spacer(modifier = Modifier.height(6.dp))

        // APY Slider
        LabeledSlider(
            label = "Annual Yield (APY)",
            valueText = TrustFormatters.percent(leg.apy),
            value = leg.apy.toFloat(),
            onValueChange = { newApy ->
                val rounded = Math.round(newApy * 100.0) / 100.0
                onUpdate(leg.copy(apy = rounded))
            },
            valueRange = 1.0f..7.0f,
            steps = 60,
            accentColor = tokens.gold,
            modifier = Modifier.testTag("apy_slider_${leg.id}")
        )
    }
}

@Composable
private fun AccountDetailDialog(
    account: VaultAccount,
    totalVaultBalance: Double,
    onDismiss: () -> Unit,
    onEdit: () -> Unit
) {
    val context = LocalContext.current
    val tokens = LocalTrustColors.current
    val sharePct = if (totalVaultBalance > 0) ((account.balance / totalVaultBalance) * 100) else 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccountBalance,
                    contentDescription = null,
                    tint = tokens.gold,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = account.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = tokens.textPrimary
                    )
                    Text(
                        text = account.institution,
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.textSecondary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StyledCard(backgroundColor = tokens.surfaceElevated) {
                    Text(
                        text = "TOTAL BALANCE",
                        style = MaterialTheme.typography.labelSmall,
                        color = tokens.textSecondary
                    )
                    Text(
                        text = TrustFormatters.currency(account.balance),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = tokens.textPrimary
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricChip(
                        label = "Yield Rate",
                        value = "${TrustFormatters.percent(account.apy)} APY",
                        color = tokens.positive,
                        modifier = Modifier.weight(1f)
                    )
                    MetricChip(
                        label = "Portfolio Share",
                        value = String.format("%.2f%%", sharePct),
                        color = tokens.gold,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricChip(
                        label = "Annual Harvest",
                        value = "+${TrustFormatters.currency(account.annualYield)}/yr",
                        color = tokens.accent,
                        modifier = Modifier.weight(1f)
                    )
                    MetricChip(
                        label = "Monthly Harvest",
                        value = "+${TrustFormatters.currency(account.monthlyYield)}/mo",
                        color = tokens.gold,
                        modifier = Modifier.weight(1f)
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(tokens.surfaceElevated)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "ACCOUNT SPECIFICATIONS",
                        style = MaterialTheme.typography.labelSmall,
                        color = tokens.textSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Custodian: ${account.institution}",
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.textPrimary,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "• Account Identifier: ${account.accountMask}",
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.textPrimary,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "• Category: ${account.category.displayName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.textPrimary,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "• Status Tier: ${account.status}",
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.textPrimary,
                        fontSize = 11.sp
                    )
                    if (account.isTaxExempt) {
                        Text(
                            text = "• Tax Status: IRC §103 Triple Tax-Exempt Sovereign",
                            style = MaterialTheme.typography.bodySmall,
                            color = tokens.positive,
                            fontSize = 11.sp
                        )
                    }
                    if (account.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = account.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = tokens.textSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                if (account.webPortalUrl.isNotBlank() || account.appPackageName.isNotBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(
                            onClick = {
                                BankLauncherHelper.openBankAppOrPortal(
                                    context,
                                    account.appPackageName,
                                    account.webPortalUrl,
                                    account.institution
                                )
                            },
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Icon(Icons.Default.PhoneAndroid, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Open App", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                BankLauncherHelper.openWebPortal(
                                    context,
                                    account.webPortalUrl,
                                    "Opening ${account.institution} online banking portal..."
                                )
                            },
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Website", fontSize = 11.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onEdit,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("detail_edit_account_button")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit Account")
                }
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = tokens.accent),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("dismiss_account_detail")
                ) {
                    Text("Close")
                }
            }
        },
        containerColor = tokens.surface,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun EditVaultAccountDialog(
    account: VaultAccount?,
    onDismiss: () -> Unit,
    onSave: (VaultAccount) -> Unit
) {
    val tokens = LocalTrustColors.current
    var name by remember { mutableStateOf(account?.name ?: "") }
    var institution by remember { mutableStateOf(account?.institution ?: "") }
    var accountMask by remember { mutableStateOf(account?.accountMask ?: "•••• ") }
    var balanceText by remember { mutableStateOf(account?.balance?.toLong()?.toString() ?: "10000000") }
    var apyText by remember { mutableStateOf(account?.apy?.toString() ?: "4.50") }
    var status by remember { mutableStateOf(account?.status ?: "Active Vault Reserve") }
    var notes by remember { mutableStateOf(account?.notes ?: "") }
    var webPortalUrl by remember { mutableStateOf(account?.webPortalUrl ?: "") }
    var appPackageName by remember { mutableStateOf(account?.appPackageName ?: "") }
    var isTaxExempt by remember { mutableStateOf(account?.isTaxExempt ?: false) }
    var selectedCat by remember { mutableStateOf(account?.category ?: VaultAccountCategory.LIQUID_CASH) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (account != null) "Edit Vault Account Figures" else "Add Account to Vault",
                color = tokens.textPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Account Name / Holding") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("vault_account_name_input")
                )
                OutlinedTextField(
                    value = institution,
                    onValueChange = { institution = it },
                    label = { Text("Custodian / Bank") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = accountMask,
                    onValueChange = { accountMask = it },
                    label = { Text("Account Identifier Mask (e.g. •••• 9821)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = balanceText,
                    onValueChange = { balanceText = it },
                    label = { Text("Exact Balance ($)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("vault_account_balance_input")
                )
                OutlinedTextField(
                    value = apyText,
                    onValueChange = { apyText = it },
                    label = { Text("APY Yield Rate (%)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = status,
                    onValueChange = { status = it },
                    label = { Text("Status Badge (e.g. Liquid CapEx, Due Apr 15 2027)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Purpose & Strategy Notes") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = webPortalUrl,
                    onValueChange = { webPortalUrl = it },
                    label = { Text("Official Web Portal URL (https://...)") },
                    placeholder = { Text("https://privatebank.jpmorgan.com") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = appPackageName,
                    onValueChange = { appPackageName = it },
                    label = { Text("Android App Package Name") },
                    placeholder = { Text("com.jpmorgan.privatebank or com.chase.sig.android") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = isTaxExempt,
                        onCheckedChange = { isTaxExempt = it },
                        colors = CheckboxDefaults.colors(checkedColor = tokens.accent)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Triple Tax-Exempt / State Tax-Free", color = tokens.textPrimary, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val bal = balanceText.toDoubleOrNull() ?: (account?.balance ?: 0.0)
                    val apy = apyText.toDoubleOrNull() ?: (account?.apy ?: 0.0)
                    val updated = VaultAccount(
                        id = account?.id ?: UUID.randomUUID().toString(),
                        name = name.ifBlank { "Vault Account" },
                        institution = institution.ifBlank { "J.P. Morgan Private Bank" },
                        accountMask = accountMask.ifBlank { "•••• 0000" },
                        category = selectedCat,
                        balance = bal,
                        apy = apy,
                        status = status.ifBlank { "Active Reserve" },
                        notes = notes,
                        isTaxExempt = isTaxExempt,
                        webPortalUrl = webPortalUrl,
                        appPackageName = appPackageName
                    )
                    onSave(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = tokens.accent),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("save_vault_account_button")
            ) {
                Text("Save Figure")
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

@Composable
private fun EditVaultLegDialog(
    leg: VaultLeg,
    onDismiss: () -> Unit,
    onSave: (VaultLeg) -> Unit
) {
    val tokens = LocalTrustColors.current
    var name by remember { mutableStateOf(leg.name) }
    var subtitle by remember { mutableStateOf(leg.subtitle) }
    var allocationText by remember { mutableStateOf(leg.allocation.toLong().toString()) }
    var apyText by remember { mutableStateOf(leg.apy.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit Fixed Income Engine Leg",
                color = tokens.textPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Leg Strategy Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = subtitle,
                    onValueChange = { subtitle = it },
                    label = { Text("Strategy Subtitle") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = allocationText,
                    onValueChange = { allocationText = it },
                    label = { Text("Capital Allocation ($)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("leg_allocation_input")
                )
                OutlinedTextField(
                    value = apyText,
                    onValueChange = { apyText = it },
                    label = { Text("APY Yield Rate (%)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("leg_apy_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val alloc = allocationText.toDoubleOrNull() ?: leg.allocation
                    val apy = apyText.toDoubleOrNull() ?: leg.apy
                    val updated = leg.copy(
                        name = name.ifBlank { leg.name },
                        subtitle = subtitle.ifBlank { leg.subtitle },
                        allocation = alloc,
                        apy = apy
                    )
                    onSave(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = tokens.accent),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("save_leg_button")
            ) {
                Text("Save Leg Figures")
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

@Composable
private fun SweepCadenceBox(
    cadence: String,
    amount: String,
    modifier: Modifier = Modifier
) {
    val tokens = LocalTrustColors.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(tokens.surfaceElevated)
            .border(1.dp, tokens.border, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(
            text = cadence,
            style = MaterialTheme.typography.labelSmall,
            color = tokens.textSecondary,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = amount,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Black,
            color = tokens.textPrimary,
            fontSize = 15.sp
        )
    }
}

@Composable
private fun CompoundingVisualizerCard(
    state: TrustState,
    onUpdate: (CompoundingSimState) -> Unit
) {
    val tokens = LocalTrustColors.current
    val sim = state.compoundingSim

    val reinvestedSurplus = (state.pureSurplusAnnual - sim.annualLifestyleSpend).coerceAtLeast(0.0)
    val horizonYears = sim.horizonYears
    val rate = state.blendedVaultApy / 100.0

    // Future value calculation of reinvested surplus
    var compoundedWealth = state.totalVaultCorpus
    for (i in 1..horizonYears) {
        compoundedWealth = (compoundedWealth + reinvestedSurplus) * (1.0 + rate)
    }
    val netWealthGain = compoundedWealth - state.totalVaultCorpus

    StyledCard(
        backgroundColor = tokens.surfaceElevated,
        borderColor = tokens.positive.copy(alpha = 0.35f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "REINVESTED SURPLUS",
                    style = MaterialTheme.typography.labelSmall,
                    color = tokens.positive,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${TrustFormatters.currency(reinvestedSurplus)} /yr",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = tokens.textPrimary
                )
            }
            MetricChip(
                label = "Horizon",
                value = "$horizonYears Years",
                color = tokens.accent
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Lifestyle Spend Slider
        LabeledSlider(
            label = "Annual Personal Lifestyle Spend",
            valueText = TrustFormatters.currency(sim.annualLifestyleSpend) + "/yr",
            value = (sim.annualLifestyleSpend / 10_000.0).toFloat(),
            onValueChange = { newSpend ->
                onUpdate(sim.copy(annualLifestyleSpend = newSpend.toDouble() * 10_000.0))
            },
            valueRange = 0f..150f,
            steps = 30,
            accentColor = tokens.negative
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Horizon Years Slider
        LabeledSlider(
            label = "Compounding Horizon",
            valueText = "$horizonYears Years",
            value = horizonYears.toFloat(),
            onValueChange = { newYears ->
                onUpdate(sim.copy(horizonYears = newYears.toInt()))
            },
            valueRange = 1f..30f,
            steps = 29,
            accentColor = tokens.gold
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Projected Wealth Gain Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            tokens.surface,
                            tokens.positive.copy(alpha = 0.15f)
                        )
                    )
                )
                .border(1.dp, tokens.positive.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Projected Corpus in $horizonYears Yrs:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = tokens.textSecondary
                    )
                    Text(
                        text = TrustFormatters.compactCurrency(compoundedWealth),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = tokens.positive
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Cumulative Freedom Gain:",
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.textSecondary
                    )
                    Text(
                        text = "+${TrustFormatters.compactCurrency(netWealthGain)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = tokens.gold
                    )
                }
            }
        }
    }
}
