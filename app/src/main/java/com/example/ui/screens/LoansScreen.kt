package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TrustState
import com.example.model.LoanItem
import com.example.model.LoanPaymentMode
import com.example.ui.components.*
import com.example.ui.theme.LocalTrustColors
import java.util.UUID

@Composable
fun LoansScreen(
    state: TrustState,
    onAddLoan: (LoanItem) -> Unit,
    onUpdateLoan: (LoanItem) -> Unit,
    onDeleteLoan: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalTrustColors.current
    var editingLoan by remember { mutableStateOf<LoanItem?>(null) }
    var isAddingLoan by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section A: Whole Life Cash-Value Borrowing Facility
        item {
            StyledCard(
                backgroundColor = tokens.surfaceElevated,
                borderColor = tokens.gold.copy(alpha = 0.4f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ILIT POLICY BORROWING FACILITY",
                            style = MaterialTheme.typography.labelSmall,
                            color = tokens.gold,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "90% Non-Taxable Credit Line",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = tokens.textPrimary
                        )
                    }
                    MetricChip(
                        label = "Max Facility",
                        value = TrustFormatters.compactCurrency(state.wholeLifeMaxBorrowLine),
                        color = tokens.gold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                val utilPct = if (state.wholeLifeMaxBorrowLine > 0) {
                    (state.wholeLifePolicyLoanBalance / state.wholeLifeMaxBorrowLine).toFloat().coerceIn(0f, 1f)
                } else 0f

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Line Utilization: ${String.format("%.1f", utilPct * 100)}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = tokens.textSecondary
                        )
                        Text(
                            text = "Remaining: ${TrustFormatters.currency(state.remainingPolicyBorrowCapacity)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = tokens.positive
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { utilPct },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (utilPct > 0.8f) tokens.negative else tokens.gold,
                        trackColor = tokens.border
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricChip(
                        label = "Whole Life Cash Value",
                        value = TrustFormatters.currency(state.totalInsuranceCashValue),
                        color = tokens.textPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricChip(
                        label = "Current Policy Loan",
                        value = TrustFormatters.currency(state.wholeLifePolicyLoanBalance),
                        color = tokens.accent,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Section B: Real Estate & Note Ledger (Table Format)
        item {
            LoanLedgerTable(
                loans = state.loans,
                onUpdateLoan = onUpdateLoan,
                onAddLoan = { isAddingLoan = true }
            )
        }

        items(state.loans, key = { it.id }) { loan ->
            LoanItemCard(
                loan = loan,
                onUpdate = onUpdateLoan,
                onEdit = { editingLoan = loan },
                onDelete = { onDeleteLoan(loan.id) }
            )
        }

        // Debt Service Summary Box
        item {
            StyledCard(backgroundColor = tokens.surface) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TOTAL ACTIVE DEBT SERVICE",
                            style = MaterialTheme.typography.labelSmall,
                            color = tokens.textSecondary
                        )
                        Text(
                            text = "${TrustFormatters.currency(state.totalDebtServiceAnnual)} /yr",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = tokens.negative
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "TOTAL PRINCIPAL DEBT",
                            style = MaterialTheme.typography.labelSmall,
                            color = tokens.textSecondary
                        )
                        Text(
                            text = TrustFormatters.currency(state.totalActiveDebtBalances),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = tokens.textPrimary
                        )
                    }
                }
            }
        }
    }

    if (isAddingLoan || editingLoan != null) {
        LoanEditDialog(
            loan = editingLoan,
            onDismiss = {
                isAddingLoan = false
                editingLoan = null
            },
            onSave = { saved ->
                if (editingLoan != null) {
                    onUpdateLoan(saved)
                } else {
                    onAddLoan(saved)
                }
                isAddingLoan = false
                editingLoan = null
            }
        )
    }
}

@Composable
private fun LoanItemCard(
    loan: LoanItem,
    onUpdate: (LoanItem) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val tokens = LocalTrustColors.current
    val isPaidOff = loan.mode == LoanPaymentMode.PAID_OFF || loan.balance <= 0.0

    StyledCard(
        borderColor = if (isPaidOff) tokens.positive.copy(alpha = 0.5f) else null
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = loan.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = tokens.textPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (isPaidOff) tokens.positive.copy(alpha = 0.2f)
                                else tokens.accent.copy(alpha = 0.2f)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = loan.mode.displayName.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPaidOff) tokens.positive else tokens.accent
                        )
                    }
                }
                Text(
                    text = "${loan.category} • Collateral: ${loan.collateral}",
                    style = MaterialTheme.typography.bodySmall,
                    color = tokens.textSecondary
                )
            }

            Row {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = tokens.textSecondary, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = tokens.negative.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = "REMAINING BALANCE",
                    style = MaterialTheme.typography.labelSmall,
                    color = tokens.textSecondary
                )
                Text(
                    text = TrustFormatters.currency(loan.balance),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = if (isPaidOff) tokens.positive else tokens.textPrimary
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${TrustFormatters.percent(loan.apr)} APR",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = tokens.textPrimary
                )
                Text(
                    text = if (isPaidOff) "\$0 /mo debt service" else "${TrustFormatters.currency(loan.monthlyPayment)} /mo",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isPaidOff) tokens.positive else tokens.negative
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Balance Interactive Slider
        if (!isPaidOff) {
            val maxBalance = Math.max(loan.balance * 1.5, 2_000_000.0)
            LabeledSlider(
                label = "Adjust Balance",
                valueText = TrustFormatters.compactCurrency(loan.balance),
                value = (loan.balance / 10_000.0).toFloat(),
                onValueChange = { newTenK ->
                    onUpdate(loan.copy(balance = newTenK.toDouble() * 10_000.0))
                },
                valueRange = 0f..(maxBalance / 10_000.0).toFloat(),
                steps = 50,
                accentColor = tokens.accent
            )
        }

        // Quick Payoff / Mode Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isPaidOff) {
                OutlinedButton(
                    onClick = {
                        onUpdate(loan.copy(balance = 0.0, mode = LoanPaymentMode.PAID_OFF))
                    },
                    modifier = Modifier.weight(1f).testTag("payoff_loan_${loan.id}"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = tokens.positive),
                    border = androidx.compose.foundation.BorderStroke(1.dp, tokens.positive.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.MoneyOff, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Instant Payoff (\$0)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                OutlinedButton(
                    onClick = {
                        onUpdate(loan.copy(balance = 500000.0, mode = LoanPaymentMode.AMORTIZING))
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = tokens.accent),
                    border = androidx.compose.foundation.BorderStroke(1.dp, tokens.accent.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Reactivate Note", fontSize = 11.sp)
                }
            }

            // Payment Mode Cycle Button
            FilledTonalButton(
                onClick = {
                    val nextMode = when (loan.mode) {
                        LoanPaymentMode.AMORTIZING -> LoanPaymentMode.INTEREST_ONLY
                        LoanPaymentMode.INTEREST_ONLY -> LoanPaymentMode.PAID_OFF
                        LoanPaymentMode.PAID_OFF -> LoanPaymentMode.AMORTIZING
                    }
                    onUpdate(loan.copy(mode = nextMode))
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = tokens.surfaceElevated,
                    contentColor = tokens.textPrimary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Mode: ${loan.mode.displayName}", fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun LoanEditDialog(
    loan: LoanItem?,
    onDismiss: () -> Unit,
    onSave: (LoanItem) -> Unit
) {
    val tokens = LocalTrustColors.current
    var name by remember { mutableStateOf(loan?.name ?: "") }
    var category by remember { mutableStateOf(loan?.category ?: "Mortgage") }
    var collateral by remember { mutableStateOf(loan?.collateral ?: "") }
    var balanceText by remember { mutableStateOf(loan?.balance?.toLong()?.toString() ?: "500000") }
    var aprText by remember { mutableStateOf(loan?.apr?.toString() ?: "5.0") }
    var paymentText by remember { mutableStateOf(loan?.monthlyPayment?.toLong()?.toString() ?: "2500") }
    var mode by remember { mutableStateOf(loan?.mode ?: LoanPaymentMode.AMORTIZING) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (loan != null) "Edit Trust Note" else "Add Trust Note",
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
                    label = { Text("Note / Loan Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category (e.g. Mortgage, Land Note)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = collateral,
                    onValueChange = { collateral = it },
                    label = { Text("Collateral Asset") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
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
                    label = { Text("APR Rate (%)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = paymentText,
                    onValueChange = { paymentText = it },
                    label = { Text("Monthly Payment ($)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val bal = balanceText.toDoubleOrNull() ?: (loan?.balance ?: 0.0)
                    val apr = aprText.toDoubleOrNull() ?: (loan?.apr ?: 0.0)
                    val pay = paymentText.toDoubleOrNull() ?: (loan?.monthlyPayment ?: 0.0)
                    val updated = LoanItem(
                        id = loan?.id ?: UUID.randomUUID().toString(),
                        name = name.ifBlank { "Trust Note" },
                        category = category.ifBlank { "Real Estate" },
                        collateral = collateral.ifBlank { "Trust Assets" },
                        balance = bal,
                        apr = apr,
                        monthlyPayment = pay,
                        mode = mode
                    )
                    onSave(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = tokens.accent)
            ) {
                Text("Save Note")
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
