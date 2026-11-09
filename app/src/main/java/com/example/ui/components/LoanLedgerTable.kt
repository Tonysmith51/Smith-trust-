package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LoanItem
import com.example.model.LoanPaymentMode
import com.example.ui.theme.LocalTrustColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanLedgerTable(
    loans: List<LoanItem>,
    onUpdateLoan: (LoanItem) -> Unit,
    onAddLoan: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val tokens = LocalTrustColors.current
    var selectedEntityFilter by remember { mutableStateOf("ALL") }
    var viewingLoanDetail by remember { mutableStateOf<LoanItem?>(null) }

    val distinctEntities = remember(loans) {
        listOf("ALL") + loans.map { it.entityObligor }.distinct()
    }

    val filteredLoans = remember(loans, selectedEntityFilter) {
        if (selectedEntityFilter == "ALL") loans
        else loans.filter { it.entityObligor == selectedEntityFilter }
    }

    val totalActiveBalance = remember(filteredLoans) {
        filteredLoans.filter { it.mode != LoanPaymentMode.PAID_OFF }.sumOf { it.balance }
    }

    val totalAnnualDebtService = remember(filteredLoans) {
        filteredLoans.sumOf { it.annualDebtService }
    }

    val blendedRate = remember(filteredLoans, totalActiveBalance) {
        if (totalActiveBalance > 0) {
            filteredLoans.filter { it.mode != LoanPaymentMode.PAID_OFF }
                .sumOf { it.balance * it.apr } / totalActiveBalance
        } else 0.0
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, tokens.border, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .testTag("loan_ledger_table_card"),
        colors = CardDefaults.cardColors(containerColor = tokens.surfaceElevated),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Title & Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(tokens.negative.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TableChart,
                            contentDescription = null,
                            tint = tokens.negative,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "FAMILY ENTITY LOAN LEDGER",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = tokens.textPrimary
                        )
                        Text(
                            text = "Active debts, interest rates & maturity dates across trust entities",
                            style = MaterialTheme.typography.bodySmall,
                            color = tokens.textSecondary
                        )
                    }
                }

                if (onAddLoan != null) {
                    FilledTonalButton(
                        onClick = onAddLoan,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = tokens.accent.copy(alpha = 0.2f),
                            contentColor = tokens.accent
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("ledger_add_loan_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Debt", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Summary Metrics Ribbon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricChip(
                    label = "Active Principal",
                    value = TrustFormatters.currency(totalActiveBalance),
                    color = tokens.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                MetricChip(
                    label = "Blended APR",
                    value = String.format("%.2f%%", blendedRate),
                    color = tokens.gold,
                    modifier = Modifier.weight(0.9f)
                )
                MetricChip(
                    label = "Annual Debt Service",
                    value = "${TrustFormatters.currency(totalAnnualDebtService)}/yr",
                    color = tokens.negative,
                    modifier = Modifier.weight(1.2f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Entity Obligor Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                distinctEntities.forEach { entity ->
                    val isSelected = selectedEntityFilter == entity
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedEntityFilter = entity },
                        label = {
                            Text(
                                text = if (entity == "ALL") "All Entities (${loans.size})"
                                else entity.replace("Smith ", "").replace(" LLC", ""),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = tokens.accent,
                            selectedLabelColor = Color.White,
                            containerColor = tokens.surface,
                            labelColor = tokens.textSecondary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // The Table: Horizontal Scrollable Grid Container
            val tableScrollState = rememberScrollState()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, tokens.border, RoundedCornerShape(12.dp))
                    .background(tokens.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(tableScrollState)
                ) {
                    // Table Header Row
                    Row(
                        modifier = Modifier
                            .background(tokens.surfaceElevated)
                            .padding(vertical = 12.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TableHeaderCell("FAMILY ENTITY (OBLIGOR)", 180.dp)
                        TableHeaderCell("INSTRUMENT & COLLATERAL", 190.dp)
                        TableHeaderCell("PRINCIPAL BALANCE", 130.dp, Alignment.CenterEnd)
                        TableHeaderCell("RATE (APR)", 95.dp, Alignment.Center)
                        TableHeaderCell("MATURITY DATE", 130.dp, Alignment.Center)
                        TableHeaderCell("MONTHLY SERVICE", 125.dp, Alignment.CenterEnd)
                        TableHeaderCell("PAYMENT MODE", 110.dp, Alignment.Center)
                        TableHeaderCell("ACTIONS", 110.dp, Alignment.Center)
                    }

                    Divider(color = tokens.border)

                    // Table Data Rows
                    filteredLoans.forEachIndexed { index, loan ->
                        val isPaidOff = loan.mode == LoanPaymentMode.PAID_OFF || loan.balance <= 0.0
                        val rowBg = if (index % 2 == 0) tokens.surface else tokens.surfaceElevated.copy(alpha = 0.5f)

                        Row(
                            modifier = Modifier
                                .background(rowBg)
                                .clickable { viewingLoanDetail = loan }
                                .padding(vertical = 10.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Entity Obligor
                            Column(modifier = Modifier.width(180.dp)) {
                                Text(
                                    text = loan.entityObligor,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = tokens.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Lender: ${loan.lender}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = tokens.textSecondary,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Instrument & Collateral
                            Column(modifier = Modifier.width(190.dp)) {
                                Text(
                                    text = loan.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = tokens.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Secured: ${loan.collateral}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = tokens.gold,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Principal Balance
                            Box(
                                modifier = Modifier.width(130.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Text(
                                    text = TrustFormatters.currency(loan.balance),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Black,
                                    color = if (isPaidOff) tokens.positive else tokens.textPrimary
                                )
                            }

                            // Interest Rate (APR)
                            Box(
                                modifier = Modifier.width(95.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = String.format("%.2f%%", loan.apr),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = tokens.gold
                                )
                            }

                            // Maturity Date
                            Box(
                                modifier = Modifier.width(130.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Event,
                                        contentDescription = null,
                                        tint = tokens.textSecondary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = loan.maturityDate,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = tokens.textPrimary,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            // Monthly Debt Service
                            Box(
                                modifier = Modifier.width(125.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Text(
                                    text = if (isPaidOff) "$0 /mo" else "${TrustFormatters.currency(loan.monthlyPayment)} /mo",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPaidOff) tokens.positive else tokens.negative
                                )
                            }

                            // Payment Mode Badge
                            Box(
                                modifier = Modifier.width(110.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            if (isPaidOff) tokens.positive.copy(alpha = 0.2f)
                                            else tokens.accent.copy(alpha = 0.2f)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = loan.mode.displayName.uppercase(),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (isPaidOff) tokens.positive else tokens.accent
                                    )
                                }
                            }

                            // Actions: Quick Payoff / Details
                            Row(
                                modifier = Modifier.width(110.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (!isPaidOff) {
                                    IconButton(
                                        onClick = {
                                            onUpdateLoan(loan.copy(balance = 0.0, mode = LoanPaymentMode.PAID_OFF))
                                        },
                                        modifier = Modifier.size(28.dp).testTag("table_payoff_${loan.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MoneyOff,
                                            contentDescription = "Pay Off",
                                            tint = tokens.positive,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = { viewingLoanDetail = loan },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Details",
                                        tint = tokens.accent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                        Divider(color = tokens.border.copy(alpha = 0.5f))
                    }

                    // Table Footer / Summary Row
                    Row(
                        modifier = Modifier
                            .background(tokens.surfaceElevated)
                            .padding(vertical = 12.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.width(180.dp)) {
                            Text(
                                text = "TOTAL ACTIVE LEDGER",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Black,
                                color = tokens.textPrimary
                            )
                        }
                        Box(modifier = Modifier.width(190.dp)) {
                            Text(
                                text = "${filteredLoans.count { it.mode != LoanPaymentMode.PAID_OFF }} Active Facilities",
                                style = MaterialTheme.typography.bodySmall,
                                color = tokens.textSecondary
                            )
                        }
                        Box(modifier = Modifier.width(130.dp), contentAlignment = Alignment.CenterEnd) {
                            Text(
                                text = TrustFormatters.currency(totalActiveBalance),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Black,
                                color = tokens.textPrimary
                            )
                        }
                        Box(modifier = Modifier.width(95.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = String.format("%.2f%%", blendedRate),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = tokens.gold
                            )
                        }
                        Box(modifier = Modifier.width(130.dp)) { /* spacing */ }
                        Box(modifier = Modifier.width(125.dp), contentAlignment = Alignment.CenterEnd) {
                            Text(
                                text = "${TrustFormatters.currency(totalAnnualDebtService / 12.0)} /mo",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Black,
                                color = tokens.negative
                            )
                        }
                        Box(modifier = Modifier.width(220.dp)) { /* spacing */ }
                    }
                }
            }
        }
    }

    // Modal: Detailed View & Loan Management
    viewingLoanDetail?.let { loan ->
        LoanDetailDialog(
            loan = loan,
            onDismiss = { viewingLoanDetail = null },
            onUpdate = { updated ->
                onUpdateLoan(updated)
                viewingLoanDetail = null
            }
        )
    }
}

@Composable
private fun TableHeaderCell(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    alignment: Alignment = Alignment.CenterStart
) {
    val tokens = LocalTrustColors.current
    Box(
        modifier = Modifier.width(width),
        contentAlignment = alignment
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            color = tokens.textSecondary,
            fontSize = 10.sp,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun LoanDetailDialog(
    loan: LoanItem,
    onDismiss: () -> Unit,
    onUpdate: (LoanItem) -> Unit
) {
    val tokens = LocalTrustColors.current
    var balanceText by remember { mutableStateOf(loan.balance.toLong().toString()) }
    var aprText by remember { mutableStateOf(loan.apr.toString()) }
    var mode by remember { mutableStateOf(loan.mode) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CreditCard, contentDescription = null, tint = tokens.gold)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(loan.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = tokens.textPrimary)
                    Text(loan.entityObligor, style = MaterialTheme.typography.bodySmall, color = tokens.textSecondary)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StyledCard(backgroundColor = tokens.surfaceElevated) {
                    Text("LENDER & MATURITY SCHEDULE", style = MaterialTheme.typography.labelSmall, color = tokens.textSecondary)
                    Text("• Financial Institution: ${loan.lender}", color = tokens.textPrimary, fontSize = 12.sp)
                    Text("• Maturity / Expiration: ${loan.maturityDate}", color = tokens.textPrimary, fontSize = 12.sp)
                    Text("• Collateral Pledged: ${loan.collateral}", color = tokens.gold, fontSize = 12.sp)
                }

                OutlinedTextField(
                    value = balanceText,
                    onValueChange = { balanceText = it },
                    label = { Text("Principal Balance ($)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = aprText,
                    onValueChange = { aprText = it },
                    label = { Text("Interest Rate (% APR)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LoanPaymentMode.values().forEach { m ->
                        val isSelected = mode == m
                        FilledTonalButton(
                            onClick = { mode = m },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (isSelected) tokens.accent else tokens.surfaceElevated,
                                contentColor = if (isSelected) Color.White else tokens.textSecondary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(m.displayName, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val bal = balanceText.toDoubleOrNull() ?: loan.balance
                    val apr = aprText.toDoubleOrNull() ?: loan.apr
                    onUpdate(loan.copy(balance = bal, apr = apr, mode = mode))
                },
                colors = ButtonDefaults.buttonColors(containerColor = tokens.accent)
            ) {
                Text("Save Changes")
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
