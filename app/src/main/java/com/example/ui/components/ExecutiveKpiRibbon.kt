package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TrustState
import com.example.ui.theme.LocalTrustColors

@Composable
fun ExecutiveKpiRibbon(
    state: TrustState,
    onTabSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalTrustColors.current
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // KPI 1: Pure Surplus (The Crown Jewel Metric)
        KpiCard(
            title = "Anthony's Pure Surplus",
            mainValue = TrustFormatters.currency(state.pureSurplusAnnual) + "/yr",
            subValue = TrustFormatters.currency(state.monthlyPureSurplus) + " /mo net",
            icon = Icons.Default.MonetizationOn,
            accentColor = tokens.positive,
            isHighlight = true,
            onClick = { onTabSelect(0) }
        )

        // KPI 2: Gross Annual Trust Yield
        KpiCard(
            title = "Gross Trust Yield",
            mainValue = TrustFormatters.currency(state.grossAnnualYield) + "/yr",
            subValue = "${TrustFormatters.currency(state.monthlyCashSweep)}/mo sweep",
            icon = Icons.Default.TrendingUp,
            accentColor = tokens.accent,
            isHighlight = false,
            onClick = { onTabSelect(0) }
        )

        // KPI 3: Core Vault Corpus
        KpiCard(
            title = "3-Legged Vault Corpus",
            mainValue = TrustFormatters.currency(state.totalVaultCorpus),
            subValue = "${TrustFormatters.percent(state.blendedVaultApy)} Blended APY",
            icon = Icons.Default.Security,
            accentColor = tokens.gold,
            isHighlight = false,
            onClick = { onTabSelect(0) }
        )

        // KPI 4: Cash & T-Bills Reserves
        KpiCard(
            title = "Cash & T-Bills Reserves",
            mainValue = TrustFormatters.currency(state.totalBankBalance),
            subValue = "+${TrustFormatters.currency(state.totalBankInterest)}/yr (JPM & T-Bills)",
            icon = Icons.Default.AccountBalance,
            accentColor = tokens.accent,
            isHighlight = false,
            onClick = { onTabSelect(1) }
        )

        // KPI 5: Total Outflows
        KpiCard(
            title = "Total Annual Outflows",
            mainValue = TrustFormatters.currency(state.totalAnnualOutflows) + "/yr",
            subValue = "Debt, Cards, Ins, RE, Fleet",
            icon = Icons.Default.CreditCard,
            accentColor = tokens.negative.copy(alpha = 0.9f),
            isHighlight = false,
            onClick = { onTabSelect(3) }
        )

        // KPI 6: Protected Trust AUM
        KpiCard(
            title = "Total Protected AUM",
            mainValue = TrustFormatters.compactCurrency(state.trustAum),
            subValue = "Vault + Cash + Estates + Fleet",
            icon = Icons.Default.Security,
            accentColor = tokens.gold,
            isHighlight = false,
            onClick = { onTabSelect(7) }
        )
    }
}

@Composable
private fun KpiCard(
    title: String,
    mainValue: String,
    subValue: String,
    icon: ImageVector,
    accentColor: Color,
    isHighlight: Boolean,
    onClick: () -> Unit
) {
    val tokens = LocalTrustColors.current

    val bgGradient = if (isHighlight) {
        Brush.linearGradient(
            colors = listOf(
                tokens.surfaceElevated,
                tokens.positive.copy(alpha = 0.12f)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                tokens.surface,
                tokens.surfaceElevated
            )
        )
    }

    Box(
        modifier = Modifier
            .width(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bgGradient)
            .border(
                width = if (isHighlight) 1.5.dp else 1.dp,
                color = if (isHighlight) accentColor.copy(alpha = 0.5f) else tokens.border,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = tokens.textSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(accentColor.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Text(
                text = mainValue,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = if (isHighlight) accentColor else tokens.textPrimary,
                fontSize = 17.sp
            )

            Text(
                text = subValue,
                style = MaterialTheme.typography.bodySmall,
                color = tokens.textSecondary,
                fontSize = 11.sp
            )
        }
    }
}
