package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TrustState
import com.example.model.BeverageSimState
import com.example.model.LlcTaxEquitySimState
import com.example.model.SnackSimState
import com.example.model.SubsidiaryLlc
import com.example.ui.components.*
import com.example.ui.theme.LocalTrustColors

enum class LlcTabSection(val title: String) {
    SIMULATOR("LLC Tax & Equity Simulator"),
    HOLDINGS("Holdings & Operations"),
    WATERFALL("Yield Waterfall")
}

@Composable
fun LlcsScreen(
    state: TrustState,
    onUpdateBeverageSim: (BeverageSimState) -> Unit,
    onUpdateSnackSim: (SnackSimState) -> Unit,
    onUpdateLlcTaxEquitySim: (LlcTaxEquitySimState) -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalTrustColors.current
    var currentSection by remember { mutableStateOf(LlcTabSection.SIMULATOR) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section Navigation Pills
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(tokens.surfaceElevated)
                    .border(1.dp, tokens.border, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                LlcTabSection.values().forEach { section ->
                    val isSelected = currentSection == section
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) tokens.accent else Color.Transparent)
                            .clickable { currentSection = section }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (section) {
                                LlcTabSection.SIMULATOR -> "Simulator"
                                LlcTabSection.HOLDINGS -> "Holdings"
                                LlcTabSection.WATERFALL -> "Waterfall"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else tokens.textSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        when (currentSection) {
            LlcTabSection.SIMULATOR -> {
                // Feature: The LLC Simulator Component
                item {
                    SectionHeader(
                        title = "LLC Asset & Tax Simulator",
                        subtitle = "Model hypothetical asset values, equity splits, tax shields & distributions",
                        icon = Icons.Default.Calculate
                    )

                    LlcEquityTaxSimulator(
                        simState = state.llcTaxEquitySim,
                        onSimStateChange = onUpdateLlcTaxEquitySim
                    )
                }
            }

            LlcTabSection.HOLDINGS -> {
                // Section 1: Master Holding Structure
                item {
                    SectionHeader(
                        title = "Trust LLC Architecture",
                        subtitle = "Wholly owned subsidiaries under Anthony Smith Master Trust",
                        icon = Icons.Default.AccountTree
                    )

                    StyledCard(
                        backgroundColor = tokens.surfaceElevated,
                        borderColor = tokens.gold.copy(alpha = 0.4f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(tokens.gold.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Business, contentDescription = null, tint = tokens.gold)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Anthony Smith Master Trust (Parent)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = tokens.textPrimary
                                )
                                Text(
                                    text = "100% Sole Member & Sovereign Equity Holder",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = tokens.gold
                                )
                            }
                        }
                    }
                }

                items(state.llcs, key = { it.id }) { llc ->
                    LlcCard(llc = llc)
                }

                // Section 2: Smith Beverages LLC Simulator ("Smith's Pop")
                item {
                    SectionHeader(
                        title = "Smith Beverages LLC Simulator",
                        subtitle = "\"Smith's Pop\" artisan craft soda & tonic CPG revenue engine",
                        icon = Icons.Default.LocalDrink
                    )

                    BeverageSimulatorCard(
                        sim = state.beverageSim,
                        onUpdate = onUpdateBeverageSim
                    )
                }

                // Section 3: Smith Snacks LLC Simulator ("Smith's Crisps & Bites")
                item {
                    SectionHeader(
                        title = "Smith Snacks LLC Simulator",
                        subtitle = "\"Smith's Crisps & Bites\" co-packing & distribution visualizer",
                        icon = Icons.Default.Fastfood
                    )

                    SnackSimulatorCard(
                        sim = state.snackSim,
                        onUpdate = onUpdateSnackSim
                    )
                }
            }

            LlcTabSection.WATERFALL -> {
                // Section 4: The Sovereign Annual Yield Waterfall
                item {
                    SectionHeader(
                        title = "Annual Yield Waterfall",
                        subtitle = "Complete flow from gross fixed income yield down to pure liquid surplus",
                        icon = Icons.Default.WaterfallChart
                    )

                    WaterfallCard(state = state)
                }
            }
        }
    }
}

@Composable
private fun LlcCard(llc: SubsidiaryLlc) {
    val tokens = LocalTrustColors.current

    StyledCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = llc.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = tokens.textPrimary
                )
                Text(
                    text = "${llc.entityType} • ${llc.ownershipPct.toInt()}% Trust Owned",
                    style = MaterialTheme.typography.bodySmall,
                    color = tokens.accent
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = llc.purpose,
            style = MaterialTheme.typography.bodySmall,
            color = tokens.textSecondary,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun BeverageSimulatorCard(
    sim: BeverageSimState,
    onUpdate: (BeverageSimState) -> Unit
) {
    val tokens = LocalTrustColors.current

    StyledCard(
        backgroundColor = tokens.surfaceElevated,
        borderColor = tokens.accent.copy(alpha = 0.35f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ANNUAL NET OPERATING INCOME (NOI)",
                    style = MaterialTheme.typography.labelSmall,
                    color = tokens.positive,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = TrustFormatters.currency(sim.annualNetOperatingIncome) + " /yr",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = tokens.positive
                )
            }
            MetricChip(
                label = "Gross Margin",
                value = TrustFormatters.currency(sim.annualGrossMargin),
                color = tokens.gold
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Cans Slider
        LabeledSlider(
            label = "Monthly Cans Produced & Sold",
            valueText = "${String.format("%,d", sim.monthlyCans.toLong())} cans/mo",
            value = (sim.monthlyCans / 5000.0).toFloat(),
            onValueChange = { newUnits ->
                onUpdate(sim.copy(monthlyCans = newUnits.toDouble() * 5000.0))
            },
            valueRange = 2f..100f,
            steps = 49,
            accentColor = tokens.accent,
            modifier = Modifier.testTag("bev_cans_slider")
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Price per Can Slider
        LabeledSlider(
            label = "Wholesale Price per Can",
            valueText = TrustFormatters.currencyCents(sim.pricePerCan),
            value = sim.pricePerCan.toFloat(),
            onValueChange = { newPrice ->
                val rounded = Math.round(newPrice * 100.0) / 100.0
                onUpdate(sim.copy(pricePerCan = rounded))
            },
            valueRange = 1.50f..6.00f,
            steps = 45,
            accentColor = tokens.gold,
            modifier = Modifier.testTag("bev_price_slider")
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Margin Slider
        LabeledSlider(
            label = "Gross Production Margin",
            valueText = "${sim.grossMarginPct.toInt()}%",
            value = sim.grossMarginPct.toFloat(),
            onValueChange = { newMargin ->
                onUpdate(sim.copy(grossMarginPct = newMargin.toDouble()))
            },
            valueRange = 20f..75f,
            steps = 55,
            accentColor = tokens.positive
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Media Retainer Slider
        LabeledSlider(
            label = "Monthly Marketing & Retainer",
            valueText = "${TrustFormatters.currency(sim.monthlyRetainer)} /mo",
            value = (sim.monthlyRetainer / 1000.0).toFloat(),
            onValueChange = { newRetainer ->
                onUpdate(sim.copy(monthlyRetainer = newRetainer.toDouble() * 1000.0))
            },
            valueRange = 2f..40f,
            steps = 38,
            accentColor = tokens.negative
        )
    }
}

@Composable
private fun SnackSimulatorCard(
    sim: SnackSimState,
    onUpdate: (SnackSimState) -> Unit
) {
    val tokens = LocalTrustColors.current

    val year1Noi = sim.annualNetOperatingIncome
    val year5Noi = year1Noi * 1.6
    val cumulative5Yr = (year1Noi * 5.0) * 1.3

    StyledCard(
        backgroundColor = tokens.surfaceElevated,
        borderColor = tokens.gold.copy(alpha = 0.35f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ANNUAL NET OPERATING INCOME (NOI)",
                    style = MaterialTheme.typography.labelSmall,
                    color = tokens.gold,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = TrustFormatters.currency(sim.annualNetOperatingIncome) + " /yr",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = tokens.positive
                )
            }
            MetricChip(
                label = "5-Yr Projected",
                value = TrustFormatters.compactCurrency(cumulative5Yr),
                color = tokens.accent
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Bags Slider
        LabeledSlider(
            label = "Monthly Bags Distributed",
            valueText = "${String.format("%,d", sim.monthlyBags.toLong())} bags/mo",
            value = (sim.monthlyBags / 5000.0).toFloat(),
            onValueChange = { newBags ->
                onUpdate(sim.copy(monthlyBags = newBags.toDouble() * 5000.0))
            },
            valueRange = 2f..60f,
            steps = 29,
            accentColor = tokens.gold,
            modifier = Modifier.testTag("snack_bags_slider")
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Price per Bag Slider
        LabeledSlider(
            label = "Wholesale Price per Bag",
            valueText = TrustFormatters.currencyCents(sim.pricePerBag),
            value = sim.pricePerBag.toFloat(),
            onValueChange = { newPrice ->
                val rounded = Math.round(newPrice * 100.0) / 100.0
                onUpdate(sim.copy(pricePerBag = rounded))
            },
            valueRange = 2.00f..8.00f,
            steps = 60,
            accentColor = tokens.accent
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Gross Margin Slider
        LabeledSlider(
            label = "Co-Packer Gross Margin",
            valueText = "${sim.grossMarginPct.toInt()}%",
            value = sim.grossMarginPct.toFloat(),
            onValueChange = { newMargin ->
                onUpdate(sim.copy(grossMarginPct = newMargin.toDouble()))
            },
            valueRange = 20f..75f,
            steps = 55,
            accentColor = tokens.positive
        )
    }
}

@Composable
private fun WaterfallCard(state: TrustState) {
    val tokens = LocalTrustColors.current

    StyledCard(
        backgroundColor = tokens.surfaceElevated,
        borderColor = tokens.positive.copy(alpha = 0.5f)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            WaterfallRow(
                step = "1",
                label = "Gross Annual Trust Yield (Vault + Banks)",
                amount = "+ ${TrustFormatters.currency(state.grossAnnualYield)}",
                color = tokens.positive,
                isBold = true
            )
            Divider(color = tokens.border)

            WaterfallRow(
                step = "2",
                label = "Debt Service Obligations",
                amount = "- ${TrustFormatters.currency(state.totalDebtServiceAnnual)}",
                color = tokens.negative
            )

            WaterfallRow(
                step = "3",
                label = "J.P. Morgan Reserve Executive Spend",
                amount = "- ${TrustFormatters.currency(state.jpReserve.annualSpend)}",
                color = tokens.negative
            )

            WaterfallRow(
                step = "4",
                label = "17 Family Gifting Cards Cap (\$19k/yr)",
                amount = "- ${TrustFormatters.currency(state.activeFamilyCardsAnnualCap)}",
                color = tokens.negative
            )

            WaterfallRow(
                step = "5",
                label = "Insurance Premiums (ILIT, Term, Health, Evac)",
                amount = "- ${TrustFormatters.currency(state.totalInsurancePremiumsAnnual)}",
                color = tokens.negative
            )

            WaterfallRow(
                step = "6",
                label = "Real Estate Escrows (7 Estates & Upkeep)",
                amount = "- ${TrustFormatters.currency(state.totalRealEstateEscrowAnnual)}",
                color = tokens.negative
            )

            WaterfallRow(
                step = "7",
                label = "Family Fleet Upkeep & Escrows",
                amount = "- ${TrustFormatters.currency(state.totalFleetUpkeepAnnual)}",
                color = tokens.negative
            )

            Divider(color = tokens.border, thickness = 1.5.dp)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ANTHONY'S PURE LIQUID SURPLUS",
                        style = MaterialTheme.typography.labelSmall,
                        color = tokens.positive,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "100% Discretionary Unencumbered Cash",
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.textSecondary,
                        fontSize = 11.sp
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = TrustFormatters.currency(state.pureSurplusAnnual) + "/yr",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = tokens.positive
                    )
                    Text(
                        text = "${TrustFormatters.currency(state.monthlyPureSurplus)} /month",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = tokens.gold
                    )
                }
            }
        }
    }
}

@Composable
private fun WaterfallRow(
    step: String,
    label: String,
    amount: String,
    color: Color,
    isBold: Boolean = false
) {
    val tokens = LocalTrustColors.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(tokens.border),
                contentAlignment = Alignment.Center
            ) {
                Text(text = step, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = tokens.textSecondary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = if (isBold) tokens.textPrimary else tokens.textSecondary,
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
            )
        }

        Text(
            text = amount,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Black else FontWeight.SemiBold,
            color = color
        )
    }
}
