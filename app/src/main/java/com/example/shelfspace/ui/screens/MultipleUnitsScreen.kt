package com.example.shelfspace.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shelfspace.calculator.ShelfCalculator
import com.example.shelfspace.calculator.StorageCapacityCalculator
import com.example.shelfspace.model.CustomSection
import com.example.shelfspace.model.ShelfResult
import com.example.shelfspace.model.StorageUnit
import com.example.shelfspace.ui.components.DimensionInputField
import com.example.shelfspace.viewmodel.ShelfSpaceViewModel
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultipleUnitsScreen(
    viewModel: ShelfSpaceViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val unitSystem = uiState.selectedUnitSystem
    val decimals = uiState.settings.decimalPlaces

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showAddUnitDialog by remember { mutableStateOf(false) }
    var showAddSectionDialog by remember { mutableStateOf(false) }

    // Multi-unit independent calculation summation
    val unitResults: List<Pair<StorageUnit, ShelfResult>> = uiState.multipleUnits.map { unit ->
        unit to ShelfCalculator.calculate(unit)
    }

    val totalShelfAreaM2 = unitResults.sumOf { it.second.totalShelfAreaM2 }
    val totalVolumeLiters = unitResults.sumOf { it.second.storageVolumeLiters }
    val totalShelves = unitResults.sumOf { it.first.shelfCount }
    val totalLinearLengthM = unitResults.sumOf { it.second.linearShelfLengthM }

    // Combined item capacity across all units (calculated independently)
    val totalCombinedItemCapacity = if (uiState.itemStorageEnabled) {
        unitResults.sumOf { (u, r) ->
            if (r.isValid) {
                val fit = StorageCapacityCalculator.calculateItemFit(
                    shelfWidthMm = r.usableWidthMm,
                    shelfDepthMm = r.usableDepthMm,
                    compartmentHeightMm = r.compartmentHeightMm,
                    item = uiState.currentItem,
                    totalCompartments = r.compartmentCount
                )
                fit.totalCapacity
            } else 0
        }
    } else 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Multiple Units & Sections", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedTabIndex == 0) showAddUnitDialog = true else showAddSectionDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Cabinets (${uiState.multipleUnits.size})") },
                    icon = { Icon(Icons.Default.Layers, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Custom Sections (${uiState.customSections.size})") },
                    icon = { Icon(Icons.Default.Kitchen, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            if (selectedTabIndex == 0) {
                // CABINETS TAB
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Multi-unit project summary card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "STORAGE PROJECT SUMMARY",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.height(12.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    SummaryItem("Units", "${uiState.multipleUnits.size}")
                                    SummaryItem("Total Shelves", "$totalShelves")
                                    SummaryItem("Total Area", String.format(Locale.US, "%.${decimals}f m²", totalShelfAreaM2))
                                }
                                Spacer(Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    SummaryItem("Total Volume", String.format(Locale.US, "%.1f L", totalVolumeLiters))
                                    SummaryItem("Linear Shelf", String.format(Locale.US, "%.${decimals}f m", totalLinearLengthM))
                                    if (uiState.itemStorageEnabled) {
                                        SummaryItem("Item Capacity", "$totalCombinedItemCapacity pcs")
                                    } else {
                                        SummaryItem("Items", "Off")
                                    }
                                }
                            }
                        }
                    }

                    items(unitResults, key = { it.first.id }) { (unit, result) ->
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = unit.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "${unitSystem.format(unit.widthMm, decimals)} × ${unitSystem.format(unit.depthMm, decimals)} × ${unitSystem.format(unit.heightMm, decimals)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                    }
                                    IconButton(onClick = { viewModel.deleteMultipleUnit(unit.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Shelves: ${unit.shelfCount}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                    Text("Area: ${String.format(Locale.US, "%.${decimals}f m²", result.totalShelfAreaM2)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                    Text("Volume: ${String.format(Locale.US, "%.1f L", result.storageVolumeLiters)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                }

                                if (uiState.itemStorageEnabled && result.isValid) {
                                    val fit = StorageCapacityCalculator.calculateItemFit(
                                        shelfWidthMm = result.usableWidthMm,
                                        shelfDepthMm = result.usableDepthMm,
                                        compartmentHeightMm = result.compartmentHeightMm,
                                        item = uiState.currentItem,
                                        totalCompartments = result.compartmentCount
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = "Capacity for ${uiState.currentItem.name}: ${fit.totalCapacity} items",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // CUSTOM SECTIONS TAB (Pantry, Kitchen, Wardrobes, Racks, Built-ins)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Text(
                            text = "Calculate custom built-ins, pantries, kitchen cabinets, and storage racks independently.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }

                    items(uiState.customSections, key = { it.id }) { section ->
                        val secAreaM2 = (section.widthMm * section.depthMm * section.shelfCount) / 1_000_000.0
                        val secVolL = (section.widthMm * section.depthMm * section.heightMm) / 1_000_000.0
                        val secLinearM = (section.widthMm * section.shelfCount) / 1000.0

                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = section.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "${unitSystem.format(section.widthMm, decimals)} × ${unitSystem.format(section.depthMm, decimals)} × ${unitSystem.format(section.heightMm, decimals)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                    }
                                    IconButton(onClick = { viewModel.deleteCustomSection(section.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Shelves: ${section.shelfCount}", style = MaterialTheme.typography.bodySmall)
                                    Text("Shelf Area: ${String.format(Locale.US, "%.${decimals}f m²", secAreaM2)}", style = MaterialTheme.typography.bodySmall)
                                    Text("Linear: ${String.format(Locale.US, "%.${decimals}f m", secLinearM)}", style = MaterialTheme.typography.bodySmall)
                                    Text("Volume: ${String.format(Locale.US, "%.1f L", secVolL)}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Unit Dialog
    if (showAddUnitDialog) {
        AddUnitDialog(
            unitSystem = unitSystem,
            decimals = decimals,
            onDismiss = { showAddUnitDialog = false },
            onAddUnit = { newUnit ->
                viewModel.addMultipleUnit(newUnit)
                showAddUnitDialog = false
            }
        )
    }

    // Add Section Dialog
    if (showAddSectionDialog) {
        AddSectionDialog(
            unitSystem = unitSystem,
            decimals = decimals,
            onDismiss = { showAddSectionDialog = false },
            onAddSection = { newSection ->
                viewModel.addCustomSection(newSection)
                showAddSectionDialog = false
            }
        )
    }
}

@Composable
private fun SummaryItem(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

@Composable
private fun AddUnitDialog(
    unitSystem: com.example.shelfspace.model.DimensionUnit,
    decimals: Int,
    onDismiss: () -> Unit,
    onAddUnit: (StorageUnit) -> Unit
) {
    var name by remember { mutableStateOf("New Cabinet") }
    var widthStr by remember { mutableStateOf(unitSystem.formatValueOnly(1000.0, decimals)) }
    var depthStr by remember { mutableStateOf(unitSystem.formatValueOnly(400.0, decimals)) }
    var heightStr by remember { mutableStateOf(unitSystem.formatValueOnly(1800.0, decimals)) }
    var shelfCountStr by remember { mutableStateOf("4") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Storage Unit") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Unit Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                DimensionInputField(
                    label = "Width",
                    valueText = widthStr,
                    onValueChange = { widthStr = it },
                    unitSymbol = unitSystem.symbol
                )
                DimensionInputField(
                    label = "Depth",
                    valueText = depthStr,
                    onValueChange = { depthStr = it },
                    unitSymbol = unitSystem.symbol
                )
                DimensionInputField(
                    label = "Height",
                    valueText = heightStr,
                    onValueChange = { heightStr = it },
                    unitSymbol = unitSystem.symbol
                )
                DimensionInputField(
                    label = "Shelves",
                    valueText = shelfCountStr,
                    onValueChange = { shelfCountStr = it },
                    unitSymbol = "boards"
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val w = widthStr.toDoubleOrNull() ?: 100.0
                    val d = depthStr.toDoubleOrNull() ?: 40.0
                    val h = heightStr.toDoubleOrNull() ?: 180.0
                    val count = shelfCountStr.toIntOrNull() ?: 4
                    val newUnit = StorageUnit(
                        id = UUID.randomUUID().toString(),
                        name = name.ifBlank { "Storage Unit" },
                        widthMm = unitSystem.toMm(w),
                        depthMm = unitSystem.toMm(d),
                        heightMm = unitSystem.toMm(h),
                        shelfCount = count
                    )
                    onAddUnit(newUnit)
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun AddSectionDialog(
    unitSystem: com.example.shelfspace.model.DimensionUnit,
    decimals: Int,
    onDismiss: () -> Unit,
    onAddSection: (CustomSection) -> Unit
) {
    var name by remember { mutableStateOf("Pantry Shelf") }
    var widthStr by remember { mutableStateOf(unitSystem.formatValueOnly(900.0, decimals)) }
    var depthStr by remember { mutableStateOf(unitSystem.formatValueOnly(350.0, decimals)) }
    var heightStr by remember { mutableStateOf(unitSystem.formatValueOnly(2000.0, decimals)) }
    var shelfCountStr by remember { mutableStateOf("5") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Custom Section") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Section Name (e.g. Pantry, Wardrobe)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                DimensionInputField(
                    label = "Width",
                    valueText = widthStr,
                    onValueChange = { widthStr = it },
                    unitSymbol = unitSystem.symbol
                )
                DimensionInputField(
                    label = "Depth",
                    valueText = depthStr,
                    onValueChange = { depthStr = it },
                    unitSymbol = unitSystem.symbol
                )
                DimensionInputField(
                    label = "Height",
                    valueText = heightStr,
                    onValueChange = { heightStr = it },
                    unitSymbol = unitSystem.symbol
                )
                DimensionInputField(
                    label = "Shelves",
                    valueText = shelfCountStr,
                    onValueChange = { shelfCountStr = it },
                    unitSymbol = "shelves"
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val w = widthStr.toDoubleOrNull() ?: 90.0
                    val d = depthStr.toDoubleOrNull() ?: 35.0
                    val h = heightStr.toDoubleOrNull() ?: 200.0
                    val count = shelfCountStr.toIntOrNull() ?: 5
                    val section = CustomSection(
                        id = UUID.randomUUID().toString(),
                        name = name.ifBlank { "Custom Section" },
                        widthMm = unitSystem.toMm(w),
                        depthMm = unitSystem.toMm(d),
                        heightMm = unitSystem.toMm(h),
                        shelfCount = count
                    )
                    onAddSection(section)
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
