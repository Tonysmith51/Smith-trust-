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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EntityTaxClassification
import com.example.model.LlcTaxEquitySimState
import com.example.model.StateTaxNexus
import com.example.ui.theme.LocalTrustColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LlcEquityTaxSimulator(
    simState: LlcTaxEquitySimState,
    onSimStateChange: (LlcTaxEquitySimState) -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalTrustColors.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Preset Scenarios Quick Selector
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = tokens.surfaceElevated)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "SELECT HYPOTHETICAL LLC SCENARIO",
                    style = MaterialTheme.typography.labelSmall,
                    color = tokens.gold,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PresetScenarioChip(
                        title = "Smith Real Estate ($25M)",
                        isSelected = simState.hypotheticalAssetValue == 25000000.0,
                        onClick = {
                            onSimStateChange(
                                simState.copy(
                                    entityName = "Smith Real Estate Holdings LLC",
                                    hypotheticalAssetValue = 25000000.0,
                                    annualNoiYieldPct = 7.8,
                                    depreciationShieldPct = 35.0,
                                    stateNexus = StateTaxNexus.DELAWARE
                                )
                            )
                        }
                    )
                    PresetScenarioChip(
                        title = "Smith Asset Holdings ($15M)",
                        isSelected = simState.hypotheticalAssetValue == 15000000.0,
                        onClick = {
                            onSimStateChange(
                                simState.copy(
                                    entityName = "Smith Asset Holdings LLC",
                                    hypotheticalAssetValue = 15000000.0,
                                    annualNoiYieldPct = 8.5,
                                    depreciationShieldPct = 20.0,
                                    stateNexus = StateTaxNexus.DELAWARE
                                )
                            )
                        }
                    )
                    PresetScenarioChip(
                        title = "Smith Hospitality ($8M)",
                        isSelected = simState.hypotheticalAssetValue == 8000000.0,
                        onClick = {
                            onSimStateChange(
                                simState.copy(
                                    entityName = "Smith Hospitality & Lodging LLC",
                                    hypotheticalAssetValue = 8000000.0,
                                    annualNoiYieldPct = 11.2,
                                    depreciationShieldPct = 28.0,
                                    stateNexus = StateTaxNexus.MICHIGAN
                                )
                            )
                        }
                    )
                    PresetScenarioChip(
                        title = "New Venture M&A ($50M)",
                        isSelected = simState.hypotheticalAssetValue == 50000000.0,
                        onClick = {
                            onSimStateChange(
                                simState.copy(
                                    entityName = "Sovereign Venture Holdings LLC",
                                    hypotheticalAssetValue = 50000000.0,
                                    annualNoiYieldPct = 9.5,
                                    depreciationShieldPct = 30.0,
                                    stateNexus = StateTaxNexus.DELAWARE
                                )
                            )
                        }
                    )
                }
            }
        }

        // 2. Hypothetical Asset Valuation & Distribution Yield Inputs
        StyledCard(borderColor = tokens.accent.copy(alpha = 0.4f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "HYPOTHETICAL ASSET VALUATION",
                        style = MaterialTheme.typography.labelSmall,
                        color = tokens.gold,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = TrustFormatters.currency(simState.hypotheticalAssetValue),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = tokens.textPrimary
                    )
                }
                MetricChip(
                    label = "Annual Pre-Tax NOI",
                    value = TrustFormatters.compactCurrency(simState.grossAnnualDistribution) + "/yr",
                    color = tokens.positive
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Asset Value Slider ($1M - $100M)
            LabeledSlider(
                label = "Adjust Hypothetical Asset Value",
                valueText = TrustFormatters.compactCurrency(simState.hypotheticalAssetValue),
                value = (simState.hypotheticalAssetValue / 1_000_000.0).toFloat(),
                onValueChange = { newMillions ->
                    onSimStateChange(simState.copy(hypotheticalAssetValue = newMillions.toDouble() * 1_000_000.0))
                },
                valueRange = 1f..100f,
                steps = 99,
                accentColor = tokens.gold,
                modifier = Modifier.testTag("llc_asset_value_slider")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Annual NOI / Distribution Yield % Slider (2% - 18%)
            LabeledSlider(
                label = "Annual Distribution / NOI Yield Rate",
                valueText = String.format("%.1f%%", simState.annualNoiYieldPct),
                value = simState.annualNoiYieldPct.toFloat(),
                onValueChange = { newYield ->
                    val rounded = Math.round(newYield * 10.0) / 10.0
                    onSimStateChange(simState.copy(annualNoiYieldPct = rounded))
                },
                valueRange = 2f..18f,
                steps = 32,
                accentColor = tokens.positive,
                modifier = Modifier.testTag("llc_yield_slider")
            )
        }

        // 3. Equity Ownership Percentage Split
        StyledCard(borderColor = tokens.gold.copy(alpha = 0.35f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "EQUITY OWNERSHIP SPLIT",
                        style = MaterialTheme.typography.labelSmall,
                        color = tokens.gold,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Member Classes & Voting Units",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = tokens.textPrimary
                    )
                }

                val totalEq = simState.totalEquityPct
                val isValid = Math.abs(totalEq - 100.0) < 0.1
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isValid) tokens.positive.copy(alpha = 0.2f) else tokens.negative.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isValid) "100% Balanced" else "Total: ${totalEq.toInt()}%",
                        color = if (isValid) tokens.positive else tokens.negative,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Multi-colored equity visualizer bar
            val trustWeight = (simState.trustEquityPct / 100.0).toFloat().coerceIn(0.01f, 1f)
            val managingWeight = (simState.managingMemberPct / 100.0).toFloat().coerceIn(0.01f, 1f)
            val familyWeight = (simState.familyPoolPct / 100.0).toFloat().coerceIn(0.01f, 1f)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(tokens.border)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(trustWeight)
                        .background(tokens.gold)
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(managingWeight)
                        .background(tokens.accent)
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(familyWeight)
                        .background(tokens.positive)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Slider 1: Class A (Master Trust)
            LabeledSlider(
                label = "Class A: Master Trust (IRC §671)",
                valueText = "${simState.trustEquityPct.toInt()}% (${TrustFormatters.compactCurrency(simState.trustEquityValue)})",
                value = simState.trustEquityPct.toFloat(),
                onValueChange = { newPct ->
                    onSimStateChange(simState.copy(trustEquityPct = Math.round(newPct).toDouble()))
                },
                valueRange = 0f..100f,
                steps = 100,
                accentColor = tokens.gold,
                modifier = Modifier.testTag("equity_trust_slider")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Slider 2: Class B (Managing Member - Anthony Smith)
            LabeledSlider(
                label = "Class B: Anthony Smith (Executive)",
                valueText = "${simState.managingMemberPct.toInt()}% (${TrustFormatters.compactCurrency(simState.managingMemberEquityValue)})",
                value = simState.managingMemberPct.toFloat(),
                onValueChange = { newPct ->
                    onSimStateChange(simState.copy(managingMemberPct = Math.round(newPct).toDouble()))
                },
                valueRange = 0f..100f,
                steps = 100,
                accentColor = tokens.accent,
                modifier = Modifier.testTag("equity_managing_slider")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Slider 3: Class C (Family / Key-Person Pool)
            LabeledSlider(
                label = "Class C: Family / Key Executive Pool",
                valueText = "${simState.familyPoolPct.toInt()}% (${TrustFormatters.compactCurrency(simState.familyPoolEquityValue)})",
                value = simState.familyPoolPct.toFloat(),
                onValueChange = { newPct ->
                    onSimStateChange(simState.copy(familyPoolPct = Math.round(newPct).toDouble()))
                },
                valueRange = 0f..100f,
                steps = 100,
                accentColor = tokens.positive,
                modifier = Modifier.testTag("equity_family_slider")
            )

            if (Math.abs(simState.totalEquityPct - 100.0) >= 0.1) {
                Spacer(modifier = Modifier.height(4.dp))
                TextButton(
                    onClick = {
                        // Auto-balance remaining to Class A
                        val remaining = (100.0 - (simState.managingMemberPct + simState.familyPoolPct)).coerceIn(0.0, 100.0)
                        onSimStateChange(simState.copy(trustEquityPct = remaining))
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Auto-balance to 100%", color = tokens.gold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 4. Tax Structuring & Depreciation Shield
        StyledCard(borderColor = tokens.positive.copy(alpha = 0.35f)) {
            Text(
                text = "TAX STRUCTURING & WRITE-OFF SHIELD",
                style = MaterialTheme.typography.labelSmall,
                color = tokens.positive,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Classification Toggle Buttons
            Text(
                text = "Entity Tax Classification",
                style = MaterialTheme.typography.bodySmall,
                color = tokens.textSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                EntityTaxClassification.values().forEach { classification ->
                    val isSelected = simState.taxClassification == classification
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) tokens.surfaceElevated else tokens.surface)
                            .border(
                                width = if (isSelected) 1.5.dp else 0.5.dp,
                                color = if (isSelected) tokens.accent else tokens.border,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onSimStateChange(simState.copy(taxClassification = classification)) }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { onSimStateChange(simState.copy(taxClassification = classification)) },
                            colors = RadioButtonDefaults.colors(selectedColor = tokens.accent)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = classification.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = tokens.textPrimary
                            )
                            Text(
                                text = classification.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = tokens.textSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // State Tax Nexus
            Text(
                text = "State Nexus & Domicile",
                style = MaterialTheme.typography.bodySmall,
                color = tokens.textSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StateTaxNexus.values().forEach { nexus ->
                    val isSelected = simState.stateNexus == nexus
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSimStateChange(simState.copy(stateNexus = nexus)) },
                        label = {
                            Text(
                                text = "${nexus.displayName} (${nexus.ratePct}%)",
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = tokens.gold,
                            selectedLabelColor = Color.Black,
                            containerColor = tokens.surfaceElevated,
                            labelColor = tokens.textSecondary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Depreciation Shield Slider (0% - 50%)
            LabeledSlider(
                label = "Cost Segregation / Depreciation Shield",
                valueText = "${simState.depreciationShieldPct.toInt()}% (-${TrustFormatters.compactCurrency(simState.annualDepreciationDeduction)}/yr deduction)",
                value = simState.depreciationShieldPct.toFloat(),
                onValueChange = { newShield ->
                    onSimStateChange(simState.copy(depreciationShieldPct = Math.round(newShield).toDouble()))
                },
                valueRange = 0f..50f,
                steps = 50,
                accentColor = tokens.positive,
                modifier = Modifier.testTag("depreciation_slider")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Tax Summary Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricChip(
                    label = "Total Effective Rate",
                    value = String.format("%.2f%%", simState.totalEffectiveTaxRatePct),
                    color = tokens.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                MetricChip(
                    label = "Tax Shield Savings",
                    value = "+${TrustFormatters.compactCurrency(simState.annualTaxShieldSavings)}/yr",
                    color = tokens.positive,
                    modifier = Modifier.weight(1.1f)
                )
                MetricChip(
                    label = "Est. Tax Liability",
                    value = "-${TrustFormatters.compactCurrency(simState.estimatedAnnualTaxLiability)}/yr",
                    color = tokens.negative,
                    modifier = Modifier.weight(1.1f)
                )
            }
        }

        // 5. Projected After-Tax Equity Distributions
        StyledCard(
            backgroundColor = tokens.surfaceElevated,
            borderColor = tokens.positive.copy(alpha = 0.5f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TOTAL NET AFTER-TAX DISTRIBUTION",
                        style = MaterialTheme.typography.labelSmall,
                        color = tokens.positive,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = TrustFormatters.currency(simState.netAfterTaxDistribution) + " /yr",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = tokens.positive
                    )
                }
                MetricChip(
                    label = "Monthly Net",
                    value = "${TrustFormatters.currency(simState.monthlyAfterTaxDistribution)}/mo",
                    color = tokens.gold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            Divider(color = tokens.border)
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "PROJECTED MEMBER DISTRIBUTIONS",
                style = MaterialTheme.typography.labelSmall,
                color = tokens.textSecondary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Payout 1: Master Trust
            MemberDistributionRow(
                className = "Class A: Master Trust",
                percentage = simState.trustEquityPct,
                equityValue = simState.trustEquityValue,
                annualCash = simState.trustAnnualDistribution,
                color = tokens.gold
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Payout 2: Anthony Smith Personal
            MemberDistributionRow(
                className = "Class B: Anthony Smith",
                percentage = simState.managingMemberPct,
                equityValue = simState.managingMemberEquityValue,
                annualCash = simState.managingMemberAnnualDistribution,
                color = tokens.accent
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Payout 3: Family Pool
            MemberDistributionRow(
                className = "Class C: Family Pool",
                percentage = simState.familyPoolPct,
                equityValue = simState.familyPoolEquityValue,
                annualCash = simState.familyPoolAnnualDistribution,
                color = tokens.positive
            )
        }

        // 6. Hypothetical Exit / Liquidation Projection
        StyledCard(borderColor = tokens.accent.copy(alpha = 0.35f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${simState.holdingYearsHorizon}-YEAR EXIT / LIQUIDATION VALUATION",
                        style = MaterialTheme.typography.labelSmall,
                        color = tokens.gold,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = TrustFormatters.compactCurrency(simState.projectedExitValuation),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = tokens.textPrimary
                    )
                }
                MetricChip(
                    label = "Net Exit Proceeds",
                    value = TrustFormatters.compactCurrency(simState.netExitProceeds),
                    color = tokens.positive
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Horizon Slider (1 - 20 years)
            LabeledSlider(
                label = "Holding Period Horizon",
                valueText = "${simState.holdingYearsHorizon} Years",
                value = simState.holdingYearsHorizon.toFloat(),
                onValueChange = { newYears ->
                    onSimStateChange(simState.copy(holdingYearsHorizon = newYears.toInt()))
                },
                valueRange = 1f..20f,
                steps = 19,
                accentColor = tokens.gold
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Exit Appreciation Rate (1% - 15%)
            LabeledSlider(
                label = "Annual Asset Appreciation (CAGR)",
                valueText = String.format("%.1f%%", simState.exitAppreciationRatePct),
                value = simState.exitAppreciationRatePct.toFloat(),
                onValueChange = { newCagr ->
                    val rounded = Math.round(newCagr * 10.0) / 10.0
                    onSimStateChange(simState.copy(exitAppreciationRatePct = rounded))
                },
                valueRange = 1f..15f,
                steps = 28,
                accentColor = tokens.accent
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Exit Proceeds Breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricChip(
                    label = "Trust Exit Proceeds",
                    value = TrustFormatters.compactCurrency(simState.trustExitProceeds),
                    color = tokens.gold,
                    modifier = Modifier.weight(1f)
                )
                MetricChip(
                    label = "Anthony Smith Exit",
                    value = TrustFormatters.compactCurrency(simState.managingMemberExitProceeds),
                    color = tokens.accent,
                    modifier = Modifier.weight(1f)
                )
                MetricChip(
                    label = "Family Pool Exit",
                    value = TrustFormatters.compactCurrency(simState.familyPoolExitProceeds),
                    color = tokens.positive,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PresetScenarioChip(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val tokens = LocalTrustColors.current

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) tokens.accent else tokens.surface)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) tokens.accent else tokens.border,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else tokens.textSecondary,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun MemberDistributionRow(
    className: String,
    percentage: Double,
    equityValue: Double,
    annualCash: Double,
    color: Color
) {
    val tokens = LocalTrustColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(tokens.surface)
            .border(1.dp, tokens.border, RoundedCornerShape(10.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "$className (${percentage.toInt()}%)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = tokens.textPrimary
                )
                Text(
                    text = "Asset Equity: ${TrustFormatters.compactCurrency(equityValue)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = tokens.textSecondary,
                    fontSize = 11.sp
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${TrustFormatters.currency(annualCash)} /yr",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = color
            )
            Text(
                text = "${TrustFormatters.currency(annualCash / 12.0)} /mo",
                style = MaterialTheme.typography.bodySmall,
                color = tokens.textSecondary,
                fontSize = 11.sp
            )
        }
    }
}
