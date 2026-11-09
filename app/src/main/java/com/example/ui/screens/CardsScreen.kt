package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TrustState
import com.example.model.FamilyCard
import com.example.model.JpMorganReserveState
import com.example.model.MinorCustodialFund
import com.example.ui.components.*
import com.example.ui.theme.LocalTrustColors
import java.util.UUID

@Composable
fun CardsScreen(
    state: TrustState,
    onUpdateJpSpend: (Double) -> Unit,
    onAddFamilyCard: (FamilyCard) -> Unit,
    onUpdateFamilyCard: (FamilyCard) -> Unit,
    onDeleteFamilyCard: (String) -> Unit,
    onUpdateMinorCustodialFund: ((MinorCustodialFund) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val tokens = LocalTrustColors.current
    val jp = state.jpReserve
    val minorFund = state.minorCustodialFund
    var editingCard by remember { mutableStateOf<FamilyCard?>(null) }
    var isAddingCard by remember { mutableStateOf(false) }
    var editingMinorFund by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section A: J.P. Morgan Reserve Palladium Card
        item {
            StyledCard(
                backgroundColor = tokens.surfaceElevated,
                borderColor = tokens.gold.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "J.P. MORGAN RESERVE",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = tokens.textPrimary,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(tokens.gold.copy(alpha = 0.2f))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text("PALLADIUM", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = tokens.gold)
                            }
                        }
                        Text(
                            text = "Master Executive Sovereign Card • \$10M+ Private Bank Line",
                            style = MaterialTheme.typography.bodySmall,
                            color = tokens.textSecondary
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.CreditCard,
                        contentDescription = null,
                        tint = tokens.gold,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Annual Spend Slider
                LabeledSlider(
                    label = "Annual Executive Card Spend",
                    valueText = TrustFormatters.currency(jp.annualSpend) + "/yr",
                    value = (jp.annualSpend / 10_000.0).toFloat(),
                    onValueChange = { newTenK ->
                        onUpdateJpSpend(newTenK.toDouble() * 10_000.0)
                    },
                    valueRange = 1f..200f,
                    steps = 199,
                    accentColor = tokens.gold,
                    modifier = Modifier.testTag("jp_spend_slider")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Points & Value Summary
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricChip(
                        label = "Points Multiplier",
                        value = "${String.format(java.util.Locale.US, "%,d", jp.totalUrPoints.toLong())} Pts",
                        color = tokens.accent,
                        modifier = Modifier.weight(1f)
                    )
                    MetricChip(
                        label = "Est. Travel Value",
                        value = TrustFormatters.currency(jp.estimatedPointValueCash),
                        color = tokens.positive,
                        modifier = Modifier.weight(1f)
                    )
                    MetricChip(
                        label = "Travel Credit",
                        value = "\$300/yr",
                        color = tokens.gold,
                        modifier = Modifier.weight(0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "• Multipliers: 8x J.P. Morgan Travel, 4x Dining/Travel, 1x Base • Master billing account for family Sapphire reserves",
                    style = MaterialTheme.typography.bodySmall,
                    color = tokens.textSecondary,
                    fontSize = 11.sp
                )
            }
        }

        // Section B: Dedicated Account for Aunt Amy's 6 Young Kids
        item {
            StyledCard(
                backgroundColor = tokens.surfaceElevated,
                borderColor = tokens.accent.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = tokens.accent.copy(alpha = 0.2f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Savings,
                                    contentDescription = null,
                                    tint = tokens.accent,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Aunt Amy's Minors Custodial Escrow",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = tokens.textPrimary
                                )
                            }
                            Text(
                                text = "${minorFund.institution} • ${minorFund.accountNumberMask}",
                                style = MaterialTheme.typography.bodySmall,
                                color = tokens.textSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = { editingMinorFund = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit Escrow",
                            tint = tokens.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Highlighting the $19k for the 6 kids
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricChip(
                        label = "Annual Custodial Escrow",
                        value = "${TrustFormatters.currency(minorFund.totalAnnualAllocation)} /yr",
                        color = tokens.positive,
                        modifier = Modifier.weight(1.1f)
                    )
                    MetricChip(
                        label = "Per Minor Child",
                        value = "${TrustFormatters.currency(minorFund.annualPerChild)} /yr",
                        color = tokens.gold,
                        modifier = Modifier.weight(1f)
                    )
                    MetricChip(
                        label = "Interest Yield (${minorFund.apy}%)",
                        value = "${TrustFormatters.currency(minorFund.annualInterestYield)} /yr",
                        color = tokens.accent,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Detailed breakdown of the 6 children
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = tokens.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, tokens.border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "6 Young Beneficiaries (Too Young for Credit Cards)",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = tokens.textPrimary
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = tokens.positive.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Accumulating to Age 18",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = tokens.positive,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val minorAges = listOf("Child 1 (Age 11)", "Child 2 (Age 9)", "Child 3 (Age 7)", "Child 4 (Age 5)", "Child 5 (Age 3)", "Child 6 (Age 1)")
                        minorAges.chunked(2).forEach { rowChildren ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                rowChildren.forEach { child ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = tokens.positive,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "$child: \$19,000/yr",
                                            fontSize = 11.sp,
                                            color = tokens.textSecondary
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "• Fiduciary Guardian: ${minorFund.guardian}\n• Held in protected trust escrow account until each child attains legal age of majority.",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = tokens.textSecondary
                        )
                    }
                }
            }
        }

        // Section C: 11 Family Chase Sapphire Reserve Cards
        item {
            SectionHeader(
                title = "11 Family Chase Sapphire Reserves",
                subtitle = "Authorized Users: \$19,000/yr annual exclusion per beneficiary",
                icon = Icons.Default.CreditCard,
                action = {
                    FilledTonalButton(
                        onClick = { isAddingCard = true },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = tokens.accent.copy(alpha = 0.2f),
                            contentColor = tokens.accent
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("add_card_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Card", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            )

            // Family Cards Aggregate Box
            StyledCard(backgroundColor = tokens.surface) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TOTAL FAMILY CARDS BUDGET",
                            style = MaterialTheme.typography.labelSmall,
                            color = tokens.textSecondary
                        )
                        Text(
                            text = "${TrustFormatters.currency(state.activeFamilyCardsAnnualCap)} /yr",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = tokens.positive
                        )
                        Text(
                            text = "11 Active Sapphire Cards @ \$19,000/yr each",
                            style = MaterialTheme.typography.bodySmall,
                            color = tokens.textSecondary,
                            fontSize = 10.sp
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "YTD FAMILY SPENT",
                            style = MaterialTheme.typography.labelSmall,
                            color = tokens.textSecondary
                        )
                        Text(
                            text = TrustFormatters.currency(state.totalFamilyCardsYtdSpend),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = tokens.gold
                        )
                        val remainingAll = (state.activeFamilyCardsAnnualCap - state.totalFamilyCardsYtdSpend).coerceAtLeast(0.0)
                        Text(
                            text = "Remaining: ${TrustFormatters.currency(remainingAll)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = tokens.accent,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        items(state.familyCards, key = { it.id }) { card ->
            FamilyCardItem(
                card = card,
                onUpdate = onUpdateFamilyCard,
                onEdit = { editingCard = card },
                onDelete = { onDeleteFamilyCard(card.id) }
            )
        }
    }

    if (isAddingCard || editingCard != null) {
        FamilyCardDialog(
            card = editingCard,
            onDismiss = {
                isAddingCard = false
                editingCard = null
            },
            onSave = { saved ->
                if (editingCard != null) {
                    onUpdateFamilyCard(saved)
                } else {
                    onAddFamilyCard(saved)
                }
                isAddingCard = false
                editingCard = null
            }
        )
    }

    if (editingMinorFund) {
        MinorFundEditDialog(
            fund = minorFund,
            onDismiss = { editingMinorFund = false },
            onSave = { updated ->
                onUpdateMinorCustodialFund?.invoke(updated)
                editingMinorFund = false
            }
        )
    }
}

@Composable
private fun FamilyCardItem(
    card: FamilyCard,
    onUpdate: (FamilyCard) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val tokens = LocalTrustColors.current
    val isFrozen = card.isFrozen
    val utilPct = (card.ytdSpend / card.annualCap).toFloat().coerceIn(0f, 1f)

    StyledCard(
        borderColor = if (isFrozen) tokens.negative.copy(alpha = 0.5f) else tokens.border
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = card.cardholderName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = tokens.textPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    if (isFrozen) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(tokens.negative.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("FROZEN", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = tokens.negative)
                        }
                    } else {
                        // Sapphire badge
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF1E3A8A).copy(alpha = 0.35f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "SAPPHIRE RESERVE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF93C5FD),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${card.relation} • ${card.lastFour}",
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.textSecondary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• 3x Travel & Dining",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = tokens.accent
                    )
                }
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

        // Spend vs Cap Progress
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "YTD Spend: ${TrustFormatters.currency(card.ytdSpend)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isFrozen) tokens.textSecondary else tokens.textPrimary
                )
                Text(
                    text = "Remaining: ${TrustFormatters.currency(card.remainingCap)} / \$19,000",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (card.remainingCap > 2000) tokens.positive else tokens.gold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { utilPct },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (utilPct > 0.9f) tokens.negative else tokens.accent,
                trackColor = tokens.border
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Interactive YTD Spend Slider
        if (!isFrozen) {
            LabeledSlider(
                label = "Adjust YTD Spend",
                valueText = TrustFormatters.currency(card.ytdSpend),
                value = (card.ytdSpend / 100.0).toFloat(),
                onValueChange = { newHundreds ->
                    val newSpend = (newHundreds * 100.0).coerceIn(0.0, card.annualCap)
                    onUpdate(card.copy(ytdSpend = newSpend))
                },
                valueRange = 0f..190f,
                steps = 190,
                accentColor = tokens.accent
            )
        }

        // Freeze / Unfreeze Toggle Button
        FilledTonalButton(
            onClick = {
                onUpdate(card.copy(isFrozen = !card.isFrozen))
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = if (isFrozen) tokens.positive.copy(alpha = 0.15f) else tokens.surfaceElevated,
                contentColor = if (isFrozen) tokens.positive else tokens.textSecondary
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(
                imageVector = if (isFrozen) Icons.Default.LockOpen else Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (isFrozen) "Unfreeze Card (Restore \$19k Cap)" else "Freeze Card Temporarily", fontSize = 11.sp)
        }
    }
}

@Composable
private fun FamilyCardDialog(
    card: FamilyCard?,
    onDismiss: () -> Unit,
    onSave: (FamilyCard) -> Unit
) {
    val tokens = LocalTrustColors.current
    var name by remember { mutableStateOf(card?.cardholderName ?: "") }
    var relation by remember { mutableStateOf(card?.relation ?: "") }
    var mask by remember { mutableStateOf(card?.lastFour ?: "•••• ") }
    var ytdText by remember { mutableStateOf(card?.ytdSpend?.toLong()?.toString() ?: "5000") }
    var capText by remember { mutableStateOf(card?.annualCap?.toLong()?.toString() ?: "19000") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (card != null) "Edit Chase Sapphire Reserve" else "Add Family Chase Sapphire Reserve",
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
                    label = { Text("Family Member Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = relation,
                    onValueChange = { relation = it },
                    label = { Text("Trust Relationship (e.g. Mother, Sister)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = mask,
                    onValueChange = { mask = it },
                    label = { Text("Card Number Mask (e.g. •••• 7101)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = ytdText,
                    onValueChange = { ytdText = it },
                    label = { Text("YTD Spend ($)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = capText,
                    onValueChange = { capText = it },
                    label = { Text("Annual Gift Exclusion Cap ($19,000)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val ytd = ytdText.toDoubleOrNull() ?: (card?.ytdSpend ?: 0.0)
                    val cap = capText.toDoubleOrNull() ?: (card?.annualCap ?: 19000.0)
                    val updated = FamilyCard(
                        id = card?.id ?: UUID.randomUUID().toString(),
                        cardholderName = name.ifBlank { "Family Member" },
                        relation = relation.ifBlank { "Family Beneficiary" },
                        lastFour = mask.ifBlank { "•••• 7100" },
                        ytdSpend = ytd,
                        annualCap = cap,
                        isFrozen = card?.isFrozen ?: false,
                        cardProduct = "Chase Sapphire Reserve® (Visa Infinite)"
                    )
                    onSave(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = tokens.accent)
            ) {
                Text("Save Sapphire Card")
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
private fun MinorFundEditDialog(
    fund: MinorCustodialFund,
    onDismiss: () -> Unit,
    onSave: (MinorCustodialFund) -> Unit
) {
    val tokens = LocalTrustColors.current
    var perChildText by remember { mutableStateOf(fund.annualPerChild.toLong().toString()) }
    var numChildrenText by remember { mutableStateOf(fund.numberOfChildren.toString()) }
    var balanceText by remember { mutableStateOf(fund.accumulatedBalance.toLong().toString()) }
    var apyText by remember { mutableStateOf(fund.apy.toString()) }
    var guardian by remember { mutableStateOf(fund.guardian) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit Aunt Amy's Minors Escrow",
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
                    value = perChildText,
                    onValueChange = { perChildText = it },
                    label = { Text("Annual Allocation Per Child ($)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = numChildrenText,
                    onValueChange = { numChildrenText = it },
                    label = { Text("Number of Minor Children") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = balanceText,
                    onValueChange = { balanceText = it },
                    label = { Text("Current Escrow Balance ($)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = apyText,
                    onValueChange = { apyText = it },
                    label = { Text("Sweep Interest Rate APY (%)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = guardian,
                    onValueChange = { guardian = it },
                    label = { Text("Guardian & Trustee Oversight") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val perChild = perChildText.toDoubleOrNull() ?: fund.annualPerChild
                    val count = numChildrenText.toIntOrNull() ?: fund.numberOfChildren
                    val balance = balanceText.toDoubleOrNull() ?: fund.accumulatedBalance
                    val apy = apyText.toDoubleOrNull() ?: fund.apy
                    val updated = fund.copy(
                        annualPerChild = perChild,
                        numberOfChildren = count,
                        accumulatedBalance = balance,
                        apy = apy,
                        guardian = guardian
                    )
                    onSave(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = tokens.accent)
            ) {
                Text("Save Escrow")
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
