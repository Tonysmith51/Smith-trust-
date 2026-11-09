package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HealthAndSafety
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
import com.example.model.InsurancePolicy
import com.example.ui.components.*
import com.example.ui.theme.LocalTrustColors
import java.util.UUID

@Composable
fun InsuranceScreen(
    state: TrustState,
    onAddPolicy: (InsurancePolicy) -> Unit,
    onUpdatePolicy: (InsurancePolicy) -> Unit,
    onDeletePolicy: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalTrustColors.current
    var editingPolicy by remember { mutableStateOf<InsurancePolicy?>(null) }
    var isAddingPolicy by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Insurance Summary Card
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
                            text = "SOVEREIGN RISK & PROTECTION POOL",
                            style = MaterialTheme.typography.labelSmall,
                            color = tokens.gold,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = TrustFormatters.compactCurrency(state.totalInsuranceDeathBenefit) + " Coverage",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = tokens.textPrimary
                        )
                    }
                    MetricChip(
                        label = "Cash Value",
                        value = TrustFormatters.compactCurrency(state.totalInsuranceCashValue),
                        color = tokens.positive
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricChip(
                        label = "Annual Premiums",
                        value = "${TrustFormatters.currency(state.totalInsurancePremiumsAnnual)} /yr",
                        color = tokens.negative,
                        modifier = Modifier.weight(1f)
                    )
                    MetricChip(
                        label = "Monthly Escrow",
                        value = "${TrustFormatters.currency(state.totalInsurancePremiumsAnnual / 12.0)} /mo",
                        color = tokens.textSecondary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Section Title & Add Policy Button
        item {
            SectionHeader(
                title = "Trust Insurance Policies",
                subtitle = "ILIT Whole Life, Key-Person Term, Executive Health & Global Evac",
                icon = Icons.Default.HealthAndSafety,
                action = {
                    FilledTonalButton(
                        onClick = { isAddingPolicy = true },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = tokens.accent.copy(alpha = 0.2f),
                            contentColor = tokens.accent
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("add_policy_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Policy", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        items(state.insurancePolicies, key = { it.id }) { policy ->
            PolicyCard(
                policy = policy,
                onEdit = { editingPolicy = policy },
                onDelete = { onDeletePolicy(policy.id) }
            )
        }
    }

    if (isAddingPolicy || editingPolicy != null) {
        PolicyEditDialog(
            policy = editingPolicy,
            onDismiss = {
                isAddingPolicy = false
                editingPolicy = null
            },
            onSave = { saved ->
                if (editingPolicy != null) {
                    onUpdatePolicy(saved)
                } else {
                    onAddPolicy(saved)
                }
                isAddingPolicy = false
                editingPolicy = null
            }
        )
    }
}

@Composable
private fun PolicyCard(
    policy: InsurancePolicy,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val tokens = LocalTrustColors.current

    StyledCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = policy.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = tokens.textPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(tokens.accent.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = policy.category.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = tokens.accent
                        )
                    }
                }
                Text(
                    text = policy.carrier,
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
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (policy.deathBenefit > 0) {
                MetricChip(
                    label = "Death Benefit",
                    value = TrustFormatters.compactCurrency(policy.deathBenefit),
                    color = tokens.gold,
                    modifier = Modifier.weight(1f)
                )
            }
            if (policy.cashValue > 0) {
                MetricChip(
                    label = "Cash Value",
                    value = TrustFormatters.currency(policy.cashValue),
                    color = tokens.positive,
                    modifier = Modifier.weight(1f)
                )
            }
            MetricChip(
                label = "Annual Premium",
                value = TrustFormatters.currency(policy.annualPremium) + "/yr",
                color = tokens.negative,
                modifier = Modifier.weight(1f)
            )
        }

        if (policy.coverageHighlights.isNotBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = policy.coverageHighlights,
                style = MaterialTheme.typography.bodySmall,
                color = tokens.textSecondary,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun PolicyEditDialog(
    policy: InsurancePolicy?,
    onDismiss: () -> Unit,
    onSave: (InsurancePolicy) -> Unit
) {
    val tokens = LocalTrustColors.current
    var name by remember { mutableStateOf(policy?.name ?: "") }
    var carrier by remember { mutableStateOf(policy?.carrier ?: "") }
    var category by remember { mutableStateOf(policy?.category ?: "Whole Life ILIT") }
    var deathBenefitText by remember { mutableStateOf(policy?.deathBenefit?.toLong()?.toString() ?: "10000000") }
    var cashValueText by remember { mutableStateOf(policy?.cashValue?.toLong()?.toString() ?: "0") }
    var premiumText by remember { mutableStateOf(policy?.annualPremium?.toLong()?.toString() ?: "25000") }
    var highlights by remember { mutableStateOf(policy?.coverageHighlights ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (policy != null) "Edit Insurance Policy" else "Add Insurance Policy",
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
                    label = { Text("Policy Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = carrier,
                    onValueChange = { carrier = it },
                    label = { Text("Insurance Carrier") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category (e.g. Whole Life, Term, PPO)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = deathBenefitText,
                    onValueChange = { deathBenefitText = it },
                    label = { Text("Death Benefit ($)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = cashValueText,
                    onValueChange = { cashValueText = it },
                    label = { Text("Cash Value Pool ($)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = premiumText,
                    onValueChange = { premiumText = it },
                    label = { Text("Annual Premium ($)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = highlights,
                    onValueChange = { highlights = it },
                    label = { Text("Coverage Highlights / Covenants") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val db = deathBenefitText.toDoubleOrNull() ?: (policy?.deathBenefit ?: 0.0)
                    val cv = cashValueText.toDoubleOrNull() ?: (policy?.cashValue ?: 0.0)
                    val prem = premiumText.toDoubleOrNull() ?: (policy?.annualPremium ?: 0.0)
                    val updated = InsurancePolicy(
                        id = policy?.id ?: UUID.randomUUID().toString(),
                        name = name.ifBlank { "Trust Insurance Policy" },
                        carrier = carrier.ifBlank { "Private Wealth Carrier" },
                        category = category.ifBlank { "Executive Policy" },
                        deathBenefit = db,
                        cashValue = cv,
                        annualPremium = prem,
                        coverageHighlights = highlights
                    )
                    onSave(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = tokens.accent)
            ) {
                Text("Save Policy")
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
