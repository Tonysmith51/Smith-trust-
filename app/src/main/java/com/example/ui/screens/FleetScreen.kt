package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.TrustState
import com.example.model.FleetVehicle
import com.example.ui.components.*
import com.example.ui.theme.LocalTrustColors
import java.util.UUID

@Composable
fun FleetScreen(
    state: TrustState,
    onAddVehicle: (FleetVehicle) -> Unit,
    onUpdateVehicle: (FleetVehicle) -> Unit,
    onDeleteVehicle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalTrustColors.current
    var editingVehicle by remember { mutableStateOf<FleetVehicle?>(null) }
    var isAddingVehicle by remember { mutableStateOf(false) }
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var serviceLoggedAssetId by remember { mutableStateOf<String?>(null) }

    // Filter fleet
    val filteredFleet = remember(state.fleet, selectedCategoryFilter) {
        when (selectedCategoryFilter) {
            "Aircraft" -> state.fleet.filter { it.assetCategory.equals("Aircraft", ignoreCase = true) }
            "Vehicles" -> state.fleet.filter { it.assetCategory.equals("Vehicle", ignoreCase = true) }
            "Watercraft" -> state.fleet.filter { it.assetCategory.equals("Watercraft", ignoreCase = true) }
            else -> state.fleet
        }
    }

    // Counts & Stats
    val aircraftCount = state.fleet.count { it.assetCategory.equals("Aircraft", ignoreCase = true) }
    val vehicleCount = state.fleet.count { it.assetCategory.equals("Vehicle", ignoreCase = true) }
    val watercraftCount = state.fleet.count { it.assetCategory.equals("Watercraft", ignoreCase = true) }

    val readyCount = state.fleet.count { 
        it.maintenanceStatus.contains("Ready", ignoreCase = true) || it.maintenanceStatus.contains("Active", ignoreCase = true) 
    }
    val serviceDueCount = state.fleet.count { it.maintenanceStatus.contains("Due", ignoreCase = true) }
    val maintenanceHoldCount = state.fleet.count { it.maintenanceStatus.contains("Hold", ignoreCase = true) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Master Fleet Valuation & Health Card
        item {
            StyledCard(
                backgroundColor = tokens.surfaceElevated,
                borderColor = tokens.accent.copy(alpha = 0.45f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "SOVEREIGN FLEET & AVIATION DIVISION",
                            style = MaterialTheme.typography.labelSmall,
                            color = tokens.gold,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = TrustFormatters.currency(state.totalFleetMarketValue),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = tokens.textPrimary
                        )
                        Text(
                            text = "Total Acquisition Capital: ${TrustFormatters.currency(state.totalFleetInvested)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = tokens.textSecondary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        val retentionPct = if (state.totalFleetInvested > 0) {
                            (state.totalFleetMarketValue / state.totalFleetInvested) * 100.0
                        } else 100.0
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = tokens.accent.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, tokens.accent.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "${String.format(java.util.Locale.US, "%.1f", retentionPct)}% Value Retention",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = tokens.accent,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${state.fleet.size} Active Assets",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = tokens.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Operational Status Indicators
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusBadge(
                        label = "$readyCount Operational",
                        color = tokens.positive,
                        icon = Icons.Default.CheckCircle,
                        modifier = Modifier.weight(1f)
                    )
                    StatusBadge(
                        label = "$serviceDueCount Service Due",
                        color = tokens.gold,
                        icon = Icons.Default.Warning,
                        modifier = Modifier.weight(1f)
                    )
                    StatusBadge(
                        label = "$maintenanceHoldCount In Hold",
                        color = tokens.accent,
                        icon = Icons.Default.Build,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Upkeep Escrow & Operations
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricChip(
                        label = "Annual Fleet Upkeep",
                        value = "${TrustFormatters.currency(state.totalFleetUpkeepAnnual)} /yr",
                        color = tokens.negative,
                        modifier = Modifier.weight(1f)
                    )
                    MetricChip(
                        label = "Monthly Fuel & Maint",
                        value = "${TrustFormatters.currency(state.totalFleetUpkeepAnnual / 12.0)} /mo",
                        color = tokens.textSecondary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Section Title & Category Filters
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Fleet Assets & Maintenance Telemetry",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = tokens.textPrimary
                        )
                        Text(
                            text = "Smith Aviation & Fleet LLC • Upload Asset Photos",
                            style = MaterialTheme.typography.bodySmall,
                            color = tokens.textSecondary
                        )
                    }

                    FilledTonalButton(
                        onClick = { isAddingVehicle = true },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = tokens.accent.copy(alpha = 0.2f),
                            contentColor = tokens.accent
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("add_vehicle_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Asset", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val filterOptions = listOf(
                        "All" to "All Assets (${state.fleet.size})",
                        "Aircraft" to "Aircraft ($aircraftCount)",
                        "Vehicles" to "Ground Fleet ($vehicleCount)",
                        "Watercraft" to "Marine ($watercraftCount)"
                    )
                    items(filterOptions) { (key, label) ->
                        val isSelected = selectedCategoryFilter == key
                        val chipBg by animateColorAsState(
                            if (isSelected) tokens.accent.copy(alpha = 0.25f) else tokens.surfaceElevated,
                            label = "filterChipBg"
                        )
                        val chipBorder by animateColorAsState(
                            if (isSelected) tokens.accent else tokens.border.copy(alpha = 0.5f),
                            label = "filterChipBorder"
                        )
                        val textColor by animateColorAsState(
                            if (isSelected) tokens.accent else tokens.textSecondary,
                            label = "filterChipText"
                        )

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = chipBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, chipBorder),
                            modifier = Modifier
                                .clickable { selectedCategoryFilter = key }
                                .testTag("filter_chip_$key")
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = textColor,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }
        }

        // Fleet Asset Cards
        items(filteredFleet, key = { it.id }) { asset ->
            FleetAssetCard(
                asset = asset,
                onEdit = { editingVehicle = asset },
                onDelete = { onDeleteVehicle(asset.id) },
                onUpdatePhoto = { newPhotoUri ->
                    onUpdateVehicle(asset.copy(photoUri = newPhotoUri))
                },
                onLogServiceCompleted = {
                    val updated = asset.copy(
                        maintenanceStatus = if (asset.assetCategory.equals("Aircraft", ignoreCase = true)) {
                            "Certified Flight-Ready"
                        } else {
                            "Certified Active"
                        },
                        nextMaintenanceDate = "Nov 15, 2027",
                        notes = "${asset.notes.trim()} [Logged Complete by Certified Crew]"
                    )
                    onUpdateVehicle(updated)
                    serviceLoggedAssetId = asset.id
                },
                isServiceJustLogged = serviceLoggedAssetId == asset.id
            )
        }
    }

    if (isAddingVehicle || editingVehicle != null) {
        FleetAssetEditDialog(
            asset = editingVehicle,
            onDismiss = {
                isAddingVehicle = false
                editingVehicle = null
            },
            onSave = { saved ->
                if (editingVehicle != null) {
                    onUpdateVehicle(saved)
                } else {
                    onAddVehicle(saved)
                }
                isAddingVehicle = false
                editingVehicle = null
            }
        )
    }
}

@Composable
private fun StatusBadge(
    label: String,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(13.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun FleetAssetCard(
    asset: FleetVehicle,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onUpdatePhoto: (String?) -> Unit,
    onLogServiceCompleted: () -> Unit,
    isServiceJustLogged: Boolean
) {
    val tokens = LocalTrustColors.current

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onUpdatePhoto(uri.toString())
        }
    }

    val categoryIcon = when (asset.assetCategory.lowercase()) {
        "aircraft" -> Icons.Default.Flight
        "watercraft" -> Icons.Default.DirectionsBoat
        else -> Icons.Default.DirectionsCar
    }

    val isWarning = asset.maintenanceStatus.contains("Due", ignoreCase = true)
    val isHold = asset.maintenanceStatus.contains("Hold", ignoreCase = true)
    val statusColor = when {
        isWarning -> tokens.gold
        isHold -> tokens.accent
        else -> tokens.positive
    }

    StyledCard(
        borderColor = if (isWarning) tokens.gold.copy(alpha = 0.5f) else tokens.border
    ) {
        // Uploaded Photo Banner
        if (asset.photoUri != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(tokens.surfaceElevated)
            ) {
                AsyncImage(
                    model = asset.photoUri,
                    contentDescription = asset.yearModel,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.6f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .clickable {
                            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Change Photo", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Top Row: Category icon, Year & Model, Registration & Actions
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
                    color = tokens.accent.copy(alpha = 0.15f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = categoryIcon,
                            contentDescription = asset.assetCategory,
                            tint = tokens.accent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = asset.yearModel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = tokens.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = tokens.surfaceElevated,
                            border = androidx.compose.foundation.BorderStroke(1.dp, tokens.border)
                        ) {
                            Text(
                                text = asset.registrationNumber,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = tokens.gold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Text(
                            text = "• ${asset.assetCategory.uppercase()}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = tokens.textSecondary
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Photo upload icon button if no photo yet
                if (asset.photoUri == null) {
                    IconButton(
                        onClick = {
                            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        modifier = Modifier.size(32.dp).testTag("upload_asset_photo_${asset.id}")
                    ) {
                        Icon(
                            Icons.Default.AddPhotoAlternate,
                            contentDescription = "Upload Photo",
                            tint = tokens.accent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = tokens.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = tokens.negative.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Station & Usage Telemetry
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(tokens.surfaceElevated)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = tokens.textSecondary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = asset.location,
                    fontSize = 11.sp,
                    color = tokens.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Speed,
                    contentDescription = null,
                    tint = tokens.accent,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = asset.telemetryUsage,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = tokens.accent
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Valuation & Cost Metrics
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricChip(
                label = "Current Valuation",
                value = TrustFormatters.compactCurrency(asset.estimatedCurrentValuation),
                color = tokens.positive,
                modifier = Modifier.weight(1f)
            )
            MetricChip(
                label = "Acquisition Delivered",
                value = TrustFormatters.compactCurrency(asset.totalDeliveredCost),
                color = tokens.textPrimary,
                modifier = Modifier.weight(1f)
            )
            MetricChip(
                label = "Value Retention",
                value = "${String.format(java.util.Locale.US, "%.1f", asset.valueRetentionPct)}%",
                color = if (asset.valueRetentionPct >= 95.0) tokens.accent else tokens.gold,
                modifier = Modifier.weight(0.9f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Maintenance Schedule & Status Banner
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = statusColor.copy(alpha = 0.08f),
            border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isWarning) Icons.Default.Warning else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = asset.maintenanceStatus,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = statusColor
                        )
                    }

                    Text(
                        text = "Next: ${asset.nextMaintenanceDate}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isWarning) tokens.gold else tokens.textSecondary
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Schedule: ${asset.maintenanceSchedule}",
                    fontSize = 11.sp,
                    color = tokens.textSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Annual Upkeep: ${TrustFormatters.currency(asset.annualUpkeep)}/yr",
                        fontSize = 11.sp,
                        color = tokens.negative
                    )

                    OutlinedButton(
                        onClick = onLogServiceCompleted,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = statusColor
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("log_service_button_${asset.id}")
                    ) {
                        Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isServiceJustLogged) "Service Logged ✓" else "Log Service",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        if (asset.notes.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = asset.notes,
                style = MaterialTheme.typography.bodySmall,
                color = tokens.textSecondary,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun FleetAssetEditDialog(
    asset: FleetVehicle?,
    onDismiss: () -> Unit,
    onSave: (FleetVehicle) -> Unit
) {
    val tokens = LocalTrustColors.current
    var assetCategory by remember { mutableStateOf(asset?.assetCategory ?: "Vehicle") }
    var yearModel by remember { mutableStateOf(asset?.yearModel ?: "") }
    var registrationNumber by remember { mutableStateOf(asset?.registrationNumber ?: "") }
    var dealer by remember { mutableStateOf(asset?.dealer ?: "") }
    var priceText by remember { mutableStateOf(asset?.preTaxPrice?.toLong()?.toString() ?: "95000") }
    var valuationText by remember { mutableStateOf(asset?.estimatedCurrentValuation?.toLong()?.toString() ?: "90000") }
    var taxRateText by remember { mutableStateOf(asset?.salesTaxRate?.toString() ?: "6.0") }
    var upkeepText by remember { mutableStateOf(asset?.annualUpkeep?.toLong()?.toString() ?: "14000") }
    var location by remember { mutableStateOf(asset?.location ?: "Swaney TC Flagship Garage") }
    var telemetryUsage by remember { mutableStateOf(asset?.telemetryUsage ?: "5,000 Mi") }
    var maintenanceSchedule by remember { mutableStateOf(asset?.maintenanceSchedule ?: "Annual Certified Inspection") }
    var nextMaintenanceDate by remember { mutableStateOf(asset?.nextMaintenanceDate ?: "Dec 15, 2026") }
    var maintenanceStatus by remember { mutableStateOf(asset?.maintenanceStatus ?: "Certified Active") }
    var notes by remember { mutableStateOf(asset?.notes ?: "") }
    var photoUri by remember { mutableStateOf(asset?.photoUri) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            photoUri = uri.toString()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (asset != null) "Edit Fleet Asset" else "Add Sovereign Fleet Asset",
                color = tokens.textPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Photo upload preview or button
                if (photoUri != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        AsyncImage(
                            model = photoUri,
                            contentDescription = "Asset Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        IconButton(
                            onClick = { photoUri = null },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(28.dp)
                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = {
                            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Upload Aircraft/Vehicle Photo", fontSize = 12.sp)
                    }
                }

                // Category Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val categories = listOf("Aircraft", "Vehicle", "Watercraft")
                    categories.forEach { cat ->
                        val selected = assetCategory.equals(cat, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selected) tokens.accent.copy(alpha = 0.25f) else tokens.surfaceElevated,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (selected) tokens.accent else tokens.border
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { assetCategory = cat }
                        ) {
                            Text(
                                text = cat,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) tokens.accent else tokens.textSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = yearModel,
                    onValueChange = { yearModel = it },
                    label = { Text("Model & Configuration") },
                    placeholder = { Text("e.g. 2025 Pilatus PC-12 NGX") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = registrationNumber,
                        onValueChange = { registrationNumber = it },
                        label = { Text("Tail # / VIN / HIN") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = valuationText,
                        onValueChange = { valuationText = it },
                        label = { Text("Valuation ($)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("Acquisition ($)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = upkeepText,
                        onValueChange = { upkeepText = it },
                        label = { Text("Upkeep ($/yr)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Hangar / Station / Berth") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = telemetryUsage,
                        onValueChange = { telemetryUsage = it },
                        label = { Text("Usage Telemetry") },
                        placeholder = { Text("e.g. 250 Flight Hrs") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = nextMaintenanceDate,
                        onValueChange = { nextMaintenanceDate = it },
                        label = { Text("Next Service Due") },
                        placeholder = { Text("Jan 18, 2027") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = maintenanceSchedule,
                    onValueChange = { maintenanceSchedule = it },
                    label = { Text("Maintenance Interval / Task") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Maintenance Status Quick Select
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val statuses = listOf(
                        "Certified Active",
                        "Certified Flight-Ready",
                        "Service Due Soon",
                        "In Maintenance Hold"
                    )
                    statuses.forEach { st ->
                        val selected = maintenanceStatus == st
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (selected) tokens.accent.copy(alpha = 0.2f) else tokens.surfaceElevated,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (selected) tokens.accent else tokens.border.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { maintenanceStatus = st }
                        ) {
                            Text(
                                text = when (st) {
                                    "Certified Flight-Ready" -> "Flight-Ready"
                                    "Certified Active" -> "Active"
                                    "Service Due Soon" -> "Due Soon"
                                    else -> "Hold"
                                },
                                fontSize = 9.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) tokens.accent else tokens.textSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(vertical = 5.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Specification & Avionics Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = priceText.toDoubleOrNull() ?: (asset?.preTaxPrice ?: 0.0)
                    val valuation = valuationText.toDoubleOrNull() ?: (asset?.estimatedCurrentValuation ?: price)
                    val tax = taxRateText.toDoubleOrNull() ?: (asset?.salesTaxRate ?: 6.0)
                    val upkeep = upkeepText.toDoubleOrNull() ?: (asset?.annualUpkeep ?: 0.0)
                    val updated = FleetVehicle(
                        id = asset?.id ?: UUID.randomUUID().toString(),
                        yearModel = yearModel.ifBlank { "Sovereign Fleet Asset" },
                        dealer = dealer.ifBlank { "Smith Aviation & Fleet LLC" },
                        preTaxPrice = price,
                        salesTaxRate = tax,
                        annualUpkeep = upkeep,
                        notes = notes,
                        assetCategory = assetCategory,
                        registrationNumber = registrationNumber.ifBlank { "REG ••••" },
                        estimatedCurrentValuation = valuation,
                        nextMaintenanceDate = nextMaintenanceDate.ifBlank { "Dec 15, 2026" },
                        maintenanceSchedule = maintenanceSchedule.ifBlank { "Certified Annual Inspection" },
                        maintenanceStatus = maintenanceStatus,
                        location = location.ifBlank { "Swaney Flagship Garage" },
                        telemetryUsage = telemetryUsage.ifBlank { "Active" },
                        photoUri = photoUri
                    )
                    onSave(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = tokens.accent)
            ) {
                Text("Save Fleet Asset")
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
