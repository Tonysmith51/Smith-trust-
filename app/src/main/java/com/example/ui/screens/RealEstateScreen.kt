package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.TrustState
import com.example.model.TrustProperty
import com.example.ui.components.*
import com.example.ui.theme.LocalTrustColors
import java.util.UUID

@Composable
fun RealEstateScreen(
    state: TrustState,
    onAddProperty: (TrustProperty) -> Unit,
    onUpdateProperty: (TrustProperty) -> Unit,
    onDeleteProperty: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalTrustColors.current
    var editingProperty by remember { mutableStateOf<TrustProperty?>(null) }
    var isAddingProperty by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Portfolio Summary Card
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
                            text = "7 TRUST ESTATES & ACREAGE",
                            style = MaterialTheme.typography.labelSmall,
                            color = tokens.gold,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = TrustFormatters.currency(state.totalRealEstateInvested),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = tokens.textPrimary
                        )
                    }
                    MetricChip(
                        label = "Properties",
                        value = "${state.properties.size} Estates",
                        color = tokens.accent
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricChip(
                        label = "Annual Escrows & Tax",
                        value = "${TrustFormatters.currency(state.totalRealEstateEscrowAnnual)} /yr",
                        color = tokens.negative,
                        modifier = Modifier.weight(1f)
                    )
                    MetricChip(
                        label = "Monthly Carrying",
                        value = "${TrustFormatters.currency(state.totalRealEstateEscrowAnnual / 12.0)} /mo",
                        color = tokens.textSecondary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Section Title & Add Button
        item {
            SectionHeader(
                title = "Family Properties & Compound Ledger",
                subtitle = "Held under Smith Real Estate Holdings LLC • Upload property photos",
                icon = Icons.Default.Home,
                action = {
                    FilledTonalButton(
                        onClick = { isAddingProperty = true },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = tokens.accent.copy(alpha = 0.2f),
                            contentColor = tokens.accent
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("add_property_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Property", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        items(state.properties, key = { it.id }) { property ->
            PropertyCard(
                property = property,
                onEdit = { editingProperty = property },
                onDelete = { onDeleteProperty(property.id) },
                onUpdatePhoto = { newUri ->
                    onUpdateProperty(property.copy(photoUri = newUri))
                }
            )
        }
    }

    if (isAddingProperty || editingProperty != null) {
        PropertyEditDialog(
            property = editingProperty,
            onDismiss = {
                isAddingProperty = false
                editingProperty = null
            },
            onSave = { saved ->
                if (editingProperty != null) {
                    onUpdateProperty(saved)
                } else {
                    onAddProperty(saved)
                }
                isAddingProperty = false
                editingProperty = null
            }
        )
    }
}

@Composable
private fun PropertyCard(
    property: TrustProperty,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onUpdatePhoto: (String?) -> Unit
) {
    val tokens = LocalTrustColors.current

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onUpdatePhoto(uri.toString())
        }
    }

    StyledCard {
        // Photo Banner if uploaded or placeholder action
        if (property.photoUri != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(tokens.surfaceElevated)
            ) {
                AsyncImage(
                    model = property.photoUri,
                    contentDescription = property.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Overlay change photo button
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = property.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = tokens.textPrimary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = tokens.gold, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = property.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.textSecondary
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Occupancy: ${property.occupants}",
                    style = MaterialTheme.typography.bodySmall,
                    color = tokens.accent,
                    fontSize = 11.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Photo upload icon button if no photo yet
                if (property.photoUri == null) {
                    IconButton(
                        onClick = {
                            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        modifier = Modifier.size(32.dp).testTag("upload_property_photo_${property.id}")
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
            MetricChip(
                label = "List Price",
                value = TrustFormatters.compactCurrency(property.listPrice),
                color = tokens.textPrimary,
                modifier = Modifier.weight(1f)
            )
            MetricChip(
                label = "Total Invested",
                value = TrustFormatters.compactCurrency(property.closingInvested),
                color = tokens.gold,
                modifier = Modifier.weight(1f)
            )
            MetricChip(
                label = "Escrow / Tax",
                value = "${TrustFormatters.currency(property.annualEscrow)}/yr",
                color = tokens.negative,
                modifier = Modifier.weight(1.1f)
            )
        }

        if (property.notes.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = property.notes,
                style = MaterialTheme.typography.bodySmall,
                color = tokens.textSecondary,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun PropertyEditDialog(
    property: TrustProperty?,
    onDismiss: () -> Unit,
    onSave: (TrustProperty) -> Unit
) {
    val tokens = LocalTrustColors.current
    var name by remember { mutableStateOf(property?.name ?: "") }
    var location by remember { mutableStateOf(property?.location ?: "") }
    var listPriceText by remember { mutableStateOf(property?.listPrice?.toLong()?.toString() ?: "1000000") }
    var closingText by remember { mutableStateOf(property?.closingInvested?.toLong()?.toString() ?: "1050000") }
    var occupants by remember { mutableStateOf(property?.occupants ?: "") }
    var escrowText by remember { mutableStateOf(property?.annualEscrow?.toLong()?.toString() ?: "15000") }
    var notes by remember { mutableStateOf(property?.notes ?: "") }
    var photoUri by remember { mutableStateOf(property?.photoUri) }

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
                text = if (property != null) "Edit Trust Property" else "Add Trust Property",
                color = tokens.textPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Photo preview or picker button
                if (photoUri != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        AsyncImage(
                            model = photoUri,
                            contentDescription = "Estate Photo",
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
                        Text("Upload Estate Photo from Device", fontSize = 12.sp)
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Estate / Property Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Geographic Location") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = occupants,
                    onValueChange = { occupants = it },
                    label = { Text("Occupancy & Purpose") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = listPriceText,
                        onValueChange = { listPriceText = it },
                        label = { Text("List Price ($)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = closingText,
                        onValueChange = { closingText = it },
                        label = { Text("Closing Cost ($)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = escrowText,
                    onValueChange = { escrowText = it },
                    label = { Text("Annual Escrow / Taxes / Upkeep ($)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Legal & Tax Notes (e.g. IRC §121)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val list = listPriceText.toDoubleOrNull() ?: (property?.listPrice ?: 0.0)
                    val closing = closingText.toDoubleOrNull() ?: (property?.closingInvested ?: 0.0)
                    val escrow = escrowText.toDoubleOrNull() ?: (property?.annualEscrow ?: 0.0)
                    val updated = TrustProperty(
                        id = property?.id ?: UUID.randomUUID().toString(),
                        name = name.ifBlank { "Trust Estate" },
                        location = location.ifBlank { "Michigan" },
                        listPrice = list,
                        closingInvested = closing,
                        occupants = occupants.ifBlank { "Family Residence" },
                        annualEscrow = escrow,
                        notes = notes,
                        photoUri = photoUri
                    )
                    onSave(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = tokens.accent)
            ) {
                Text("Save Estate")
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
