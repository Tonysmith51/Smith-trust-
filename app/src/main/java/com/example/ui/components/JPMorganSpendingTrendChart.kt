package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalTrustColors
import java.util.Locale
import kotlin.math.roundToInt

data class MonthlyTreasurySpend(
    val monthName: String,
    val shortMonth: String,
    val year: Int,
    val totalSpend: Double,
    val realEstateSpend: Double,
    val fleetSpend: Double,
    val operatingSpend: Double,
    val keyEvent: String
)

object DefaultTreasurySpendingData {
    val trailing12Months = listOf(
        MonthlyTreasurySpend("November", "Nov", 2025, 185_000.0, 65_000.0, 45_000.0, 75_000.0, "Routine operating & hangar staging"),
        MonthlyTreasurySpend("December", "Dec", 2025, 320_000.0, 110_000.0, 85_000.0, 125_000.0, "Year-end estate improvements & family prep"),
        MonthlyTreasurySpend("January", "Jan", 2026, 240_000.0, 80_000.0, 50_000.0, 110_000.0, "Annual insurance retainers & winter upkeep"),
        MonthlyTreasurySpend("February", "Feb", 2026, 195_000.0, 95_000.0, 35_000.0, 65_000.0, "Swaney flagship interior refinements"),
        MonthlyTreasurySpend("March", "Mar", 2026, 410_000.0, 120_000.0, 210_000.0, 80_000.0, "Expedition Max & Grand Wagoneer cash outlays"),
        MonthlyTreasurySpend("April", "Apr", 2026, 275_000.0, 115_000.0, 60_000.0, 100_000.0, "Q1 operating sweep & landscaping contracts"),
        MonthlyTreasurySpend("May", "May", 2026, 350_000.0, 140_000.0, 130_000.0, 80_000.0, "Pilatus PC-12 hangar deposit & navigation subs"),
        MonthlyTreasurySpend("June", "Jun", 2026, 490_000.0, 230_000.0, 160_000.0, 100_000.0, "Applegate Huron dock & Chris-Craft commissioning"),
        MonthlyTreasurySpend("July", "Jul", 2026, 310_000.0, 125_000.0, 85_000.0, 100_000.0, "Summer family estate grounds maintenance"),
        MonthlyTreasurySpend("August", "Aug", 2026, 280_000.0, 110_000.0, 70_000.0, 100_000.0, "Traverse City property exterior improvements"),
        MonthlyTreasurySpend("September", "Sep", 2026, 620_000.0, 380_000.0, 140_000.0, 100_000.0, "Cedar 400-Acre compound runway expansion"),
        MonthlyTreasurySpend("October", "Oct", 2026, 265_000.0, 90_000.0, 75_000.0, 100_000.0, "Fall fleet winterization & reserve replenishment")
    )
}

enum class TimeframeFilter(val label: String) {
    TRAILING_12("Trailing 12 Mo"),
    LAST_6("Last 6 Mo"),
    YTD_2026("2026 YTD")
}

@Composable
fun JPMorganSpendingTrendChart(
    treasuryBalance: Double = 31_761_543.0,
    modifier: Modifier = Modifier
) {
    val tokens = LocalTrustColors.current
    var selectedTimeframe by remember { mutableStateOf(TimeframeFilter.TRAILING_12) }

    val rawData = DefaultTreasurySpendingData.trailing12Months
    val displayedData = remember(selectedTimeframe) {
        when (selectedTimeframe) {
            TimeframeFilter.TRAILING_12 -> rawData
            TimeframeFilter.LAST_6 -> rawData.takeLast(6)
            TimeframeFilter.YTD_2026 -> rawData.filter { it.year == 2026 }
        }
    }

    var selectedIndex by remember { mutableStateOf<Int?>(displayedData.size - 1) }

    // Ensure selectedIndex remains in range when timeframe switches
    LaunchedEffect(displayedData.size) {
        if (selectedIndex == null || selectedIndex!! >= displayedData.size) {
            selectedIndex = displayedData.size - 1
        }
    }

    val totalSpend = remember(displayedData) { displayedData.sumOf { it.totalSpend } }
    val avgSpend = remember(displayedData, totalSpend) { if (displayedData.isNotEmpty()) totalSpend / displayedData.size else 0.0 }
    val peakSpend = remember(displayedData) { displayedData.maxOfOrNull { it.totalSpend } ?: 0.0 }
    val lowestSpend = remember(displayedData) { displayedData.minOfOrNull { it.totalSpend } ?: 0.0 }
    val coverageMonths = remember(treasuryBalance, avgSpend) {
        if (avgSpend > 0) treasuryBalance / avgSpend else 0.0
    }

    val selectedPoint = selectedIndex?.let { displayedData.getOrNull(it) } ?: displayedData.lastOrNull()

    StyledCard(
        modifier = modifier.testTag("jpm_spending_trend_card"),
        backgroundColor = tokens.surfaceElevated,
        borderColor = tokens.accent.copy(alpha = 0.5f)
    ) {
        // 1. Header with Badge & J.P. Morgan Treasury Identification
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(tokens.gold.copy(alpha = 0.2f))
                            .border(0.5.dp, tokens.gold.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timeline,
                                contentDescription = null,
                                tint = tokens.gold,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "D3 SPLINE ANALYTICS",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = tokens.gold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "J.P. MORGAN TREASURY",
                        style = MaterialTheme.typography.labelSmall,
                        color = tokens.textSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 0.8.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Treasury Monthly Spending Trends",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = tokens.textPrimary
                )

                Text(
                    text = "Houses, vehicles, aircraft CapEx & liquidity sweeps from $31.76M reserve",
                    style = MaterialTheme.typography.bodySmall,
                    color = tokens.textSecondary,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Timeframe Selection Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TimeframeFilter.values().forEach { timeframe ->
                val isSelected = selectedTimeframe == timeframe
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        selectedTimeframe = timeframe
                        selectedIndex = null
                    },
                    label = {
                        Text(
                            text = timeframe.label,
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
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) tokens.accent else tokens.border,
                        selectedBorderColor = tokens.accent
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(32.dp).testTag("timeframe_${timeframe.name.lowercase()}")
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Interactive Inspector / Crosshair Stats Card
        if (selectedPoint != null) {
            val vsAvgPct = if (avgSpend > 0) ((selectedPoint.totalSpend - avgSpend) / avgSpend) * 100.0 else 0.0
            val isAboveAvg = vsAvgPct >= 0

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(tokens.surface)
                    .border(1.dp, tokens.gold.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${selectedPoint.monthName} ${selectedPoint.year}".uppercase(Locale.US),
                                style = MaterialTheme.typography.labelSmall,
                                color = tokens.gold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = TrustFormatters.currency(selectedPoint.totalSpend),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = tokens.textPrimary
                            )
                        }

                        // Variance chip
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isAboveAvg) tokens.accent.copy(alpha = 0.2f) else tokens.positive.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isAboveAvg) "+${String.format(Locale.US, "%.1f", vsAvgPct)}% vs Avg"
                                else "${String.format(Locale.US, "%.1f", vsAvgPct)}% vs Avg",
                                color = if (isAboveAvg) tokens.accent else tokens.positive,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Key Event Memo
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = tokens.textSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = selectedPoint.keyEvent,
                            style = MaterialTheme.typography.bodySmall,
                            color = tokens.textSecondary,
                            fontSize = 11.sp
                        )
                    }

                    // Spending Category Distribution for Selected Month
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SpendingMiniTag(
                            label = "Real Estate",
                            amount = selectedPoint.realEstateSpend,
                            color = tokens.gold,
                            modifier = Modifier.weight(1f)
                        )
                        SpendingMiniTag(
                            label = "Fleet & Air",
                            amount = selectedPoint.fleetSpend,
                            color = tokens.accent,
                            modifier = Modifier.weight(1f)
                        )
                        SpendingMiniTag(
                            label = "Operations",
                            amount = selectedPoint.operatingSpend,
                            color = tokens.positive,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. D3-Style Canvas Spline Chart with Crosshair & Touch Gestures
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(tokens.surface)
                .border(0.5.dp, tokens.border.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                .padding(top = 16.dp, bottom = 8.dp, start = 8.dp, end = 8.dp)
        ) {
            D3SplineChartCanvas(
                data = displayedData,
                selectedIndex = selectedIndex,
                avgSpend = avgSpend,
                peakSpend = peakSpend,
                lineColor = tokens.gold,
                accentColor = tokens.accent,
                textColor = tokens.textSecondary,
                gridColor = tokens.border.copy(alpha = 0.4f),
                onSelectIndex = { selectedIndex = it }
            )
        }

        // Horizontal Month Axis Labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            displayedData.forEachIndexed { idx, point ->
                val isSelected = idx == selectedIndex
                Text(
                    text = point.shortMonth,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) tokens.gold else tokens.textSecondary,
                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                    fontSize = 10.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 5. Key Treasury Analytics Ribbon
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricChip(
                label = "Average / Month",
                value = TrustFormatters.currency(avgSpend),
                color = tokens.textPrimary,
                modifier = Modifier.weight(1f)
            )
            MetricChip(
                label = "Peak Outflow",
                value = TrustFormatters.currency(peakSpend),
                color = tokens.accent,
                modifier = Modifier.weight(1f)
            )
            MetricChip(
                label = "Reserve Runway",
                value = "${String.format(Locale.US, "%.1f", coverageMonths)} Mo",
                color = tokens.positive,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Liquidity ratio footer note
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = tokens.positive,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Treasury Reserve ($31,761,543.00) provides ${(coverageMonths / 12.0).toInt()} years of liquidity coverage at current spending velocity.",
                style = MaterialTheme.typography.bodySmall,
                color = tokens.textSecondary,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun SpendingMiniTag(
    label: String,
    amount: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Column {
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = TrustFormatters.compactCurrency(amount),
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }
    }
}

@Composable
private fun D3SplineChartCanvas(
    data: List<MonthlyTreasurySpend>,
    selectedIndex: Int?,
    avgSpend: Double,
    peakSpend: Double,
    lineColor: Color,
    accentColor: Color,
    textColor: Color,
    gridColor: Color,
    onSelectIndex: (Int) -> Unit
) {
    val maxVal = remember(data, peakSpend) {
        (peakSpend * 1.15).coerceAtLeast(100_000.0)
    }
    val minVal = 0.0

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(data) {
                detectTapGestures { offset ->
                    val stepX = size.width / (data.size - 1).coerceAtLeast(1)
                    val rawIndex = ((offset.x + stepX / 2f) / stepX).toInt()
                    val clamped = rawIndex.coerceIn(0, data.size - 1)
                    onSelectIndex(clamped)
                }
            }
            .pointerInput(data) {
                detectDragGestures { change, _ ->
                    val stepX = size.width / (data.size - 1).coerceAtLeast(1)
                    val rawIndex = ((change.position.x + stepX / 2f) / stepX).toInt()
                    val clamped = rawIndex.coerceIn(0, data.size - 1)
                    onSelectIndex(clamped)
                }
            }
    ) {
        if (data.isEmpty()) return@Canvas

        val w = size.width
        val h = size.height
        val n = data.size
        val stepX = if (n > 1) w / (n - 1) else w

        // Helper to convert data value to Y coordinate
        fun toY(v: Double): Float {
            val norm = ((v - minVal) / (maxVal - minVal)).toFloat()
            return h - (norm * h)
        }

        // 1. Draw horizontal grid lines (0%, 25%, 50%, 75%, 100%)
        val gridFractions = listOf(0.25f, 0.50f, 0.75f)
        gridFractions.forEach { frac ->
            val y = h * frac
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
            )
        }

        // 2. Draw Average Spending Reference Line (D3 style dashed guideline)
        val avgY = toY(avgSpend)
        drawLine(
            color = accentColor.copy(alpha = 0.65f),
            start = Offset(0f, avgY),
            end = Offset(w, avgY),
            strokeWidth = 1.5f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
        )

        // 3. Calculate points coordinates
        val points = data.mapIndexed { idx, item ->
            val x = idx * stepX
            val y = toY(item.totalSpend)
            Offset(x, y)
        }

        // 4. Construct Monotone Cubic Bézier Spline (D3 curveMonotoneX / cubicTo interpolation)
        val path = Path()
        val fillPath = Path()

        if (points.isNotEmpty()) {
            path.moveTo(points.first().x, points.first().y)
            fillPath.moveTo(points.first().x, h)
            fillPath.lineTo(points.first().x, points.first().y)

            for (i in 0 until points.size - 1) {
                val p0 = if (i > 0) points[i - 1] else points[i]
                val p1 = points[i]
                val p2 = points[i + 1]
                val p3 = if (i + 2 < points.size) points[i + 2] else p2

                // D3-style smooth tangent control points
                val cp1x = p1.x + (p2.x - p0.x) * 0.18f
                val cp1y = p1.y + (p2.y - p0.y) * 0.18f

                val cp2x = p2.x - (p3.x - p1.x) * 0.18f
                val cp2y = p2.y - (p3.y - p1.y) * 0.18f

                path.cubicTo(cp1x, cp1y, cp2x, cp2y, p2.x, p2.y)
                fillPath.cubicTo(cp1x, cp1y, cp2x, cp2y, p2.x, p2.y)
            }

            fillPath.lineTo(points.last().x, h)
            fillPath.close()

            // Draw Area Gradient under the Curve
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        lineColor.copy(alpha = 0.35f),
                        lineColor.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = h
                )
            )

            // Draw Outer Glow Aura Stroke
            drawPath(
                path = path,
                color = lineColor.copy(alpha = 0.30f),
                style = Stroke(
                    width = 8f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // Draw Primary Spline Line Stroke
            drawPath(
                path = path,
                brush = Brush.horizontalGradient(
                    colors = listOf(lineColor, accentColor, lineColor)
                ),
                style = Stroke(
                    width = 3.5f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }

        // 5. Draw Selected Point Crosshair & Pulsing Halo
        selectedIndex?.let { idx ->
            if (idx in points.indices) {
                val pt = points[idx]

                // Vertical Crosshair Line
                drawLine(
                    color = Color.White.copy(alpha = 0.6f),
                    start = Offset(pt.x, 0f),
                    end = Offset(pt.x, h),
                    strokeWidth = 1.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                )

                // Outer Halo
                drawCircle(
                    color = lineColor.copy(alpha = 0.25f),
                    radius = 16f,
                    center = pt
                )

                // Middle Ring
                drawCircle(
                    color = lineColor,
                    radius = 7f,
                    center = pt
                )

                // Inner Bright Node
                drawCircle(
                    color = Color.White,
                    radius = 3.5f,
                    center = pt
                )
            }
        }

        // Draw Subtle Point Nodes for unselected points
        points.forEachIndexed { i, pt ->
            if (i != selectedIndex) {
                drawCircle(
                    color = lineColor.copy(alpha = 0.8f),
                    radius = 3f,
                    center = pt
                )
            }
        }
    }
}
