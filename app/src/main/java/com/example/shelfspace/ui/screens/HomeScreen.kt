package com.example.shelfspace.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shelfspace.model.CalculationMode
import com.example.shelfspace.model.DimensionUnit
import com.example.shelfspace.model.ItemOrientation
import com.example.shelfspace.model.ShelfMode
import com.example.shelfspace.model.SpacingMode
import com.example.shelfspace.ui.components.CalculationBreakdownCard
import com.example.shelfspace.ui.components.DimensionInputField
import com.example.shelfspace.ui.components.ItemCapacityResultCard
import com.example.shelfspace.ui.components.MainHeroResultCard
import com.example.shelfspace.ui.components.SecondaryResultCards
import com.example.shelfspace.ui.components.StorageDiagramCanvas
import com.example.shelfspace.ui.components.StorageUtilizationCard
import com.example.shelfspace.viewmodel.ShelfSpaceViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    viewModel: ShelfSpaceViewModel,
    onNavigateToLayout: () -> Unit,
    onNavigateToMultipleUnits: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    // Export Launchers
    var exportFormat by remember { mutableStateOf("txt") }
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(
            when (exportFormat) {
                "csv" -> "text/csv"
                "json" -> "application/json"
                else -> "text/plain"
            }
        )
    ) { uri ->
        if (uri != null) {
            viewModel.exportToUri(context, uri, exportFormat)
        }
    }

    var showMenu by remember { mutableStateOf(false) }

    // Local dimension edit strings (in user's active unit)
    val unitSystem = uiState.selectedUnitSystem
    val decimals = uiState.settings.decimalPlaces

    // Raw input states
    var widthStr by remember(uiState.currentUnit.widthMm, unitSystem) {
        mutableStateOf(unitSystem.formatValueOnly(uiState.currentUnit.widthMm, decimals))
    }
    var depthStr by remember(uiState.currentUnit.depthMm, unitSystem) {
        mutableStateOf(unitSystem.formatValueOnly(uiState.currentUnit.depthMm, decimals))
    }
    var heightStr by remember(uiState.currentUnit.heightMm, unitSystem) {
        mutableStateOf(unitSystem.formatValueOnly(uiState.currentUnit.heightMm, decimals))
    }

    // Panel thicknesses (in mm for high precision carpentry)
    var sidePanelStr by remember(uiState.currentUnit.sidePanelThicknessMm) {
        mutableStateOf(String.format(Locale.US, "%.1f", uiState.currentUnit.sidePanelThicknessMm))
    }
    var topPanelStr by remember(uiState.currentUnit.topThicknessMm) {
        mutableStateOf(String.format(Locale.US, "%.1f", uiState.currentUnit.topThicknessMm))
    }
    var bottomPanelStr by remember(uiState.currentUnit.bottomThicknessMm) {
        mutableStateOf(String.format(Locale.US, "%.1f", uiState.currentUnit.bottomThicknessMm))
    }
    var backPanelStr by remember(uiState.currentUnit.backPanelThicknessMm) {
        mutableStateOf(String.format(Locale.US, "%.1f", uiState.currentUnit.backPanelThicknessMm))
    }
    var shelfThickStr by remember(uiState.currentUnit.shelfThicknessMm) {
        mutableStateOf(String.format(Locale.US, "%.1f", uiState.currentUnit.shelfThicknessMm))
    }

    // Shelves count
    var shelfCountStr by remember(uiState.currentUnit.shelfCount) {
        mutableStateOf("${uiState.currentUnit.shelfCount}")
    }

    // Item input states
    var itemWidthStr by remember(uiState.currentItem.widthMm, unitSystem) {
        mutableStateOf(unitSystem.formatValueOnly(uiState.currentItem.widthMm, decimals))
    }
    var itemDepthStr by remember(uiState.currentItem.depthMm, unitSystem) {
        mutableStateOf(unitSystem.formatValueOnly(uiState.currentItem.depthMm, decimals))
    }
    var itemHeightStr by remember(uiState.currentItem.heightMm, unitSystem) {
        mutableStateOf(unitSystem.formatValueOnly(uiState.currentItem.heightMm, decimals))
    }
    var itemQtyStr by remember(uiState.currentItem.quantity) {
        mutableStateOf("${uiState.currentItem.quantity}")
    }

    // Max shelves clearance input
    var maxClearanceStr by remember(uiState.maxShelfTargetClearanceMm, unitSystem) {
        mutableStateOf(unitSystem.formatValueOnly(uiState.maxShelfTargetClearanceMm, decimals))
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "ShelfSpace",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Calculate usable shelf and storage capacity.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                    IconButton(onClick = onNavigateToLayout) {
                        Icon(Icons.Default.Dashboard, contentDescription = "Storage Layout")
                    }
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(Icons.Default.History, contentDescription = "History")
                    }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Multiple Units") },
                            onClick = { showMenu = false; onNavigateToMultipleUnits() },
                            leadingIcon = { Icon(Icons.Default.Layers, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Save to History") },
                            onClick = { showMenu = false; viewModel.saveCurrentToHistory() },
                            leadingIcon = { Icon(Icons.Default.BookmarkAdd, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Share Result") },
                            onClick = { showMenu = false; viewModel.shareCurrentCalculation(context) },
                            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Export as TXT") },
                            onClick = {
                                showMenu = false
                                exportFormat = "txt"
                                exportLauncher.launch("${uiState.currentUnit.name.replace(" ", "_")}_shelfspace.txt")
                            },
                            leadingIcon = { Icon(Icons.Default.FileDownload, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Export as CSV") },
                            onClick = {
                                showMenu = false
                                exportFormat = "csv"
                                exportLauncher.launch("${uiState.currentUnit.name.replace(" ", "_")}_shelfspace.csv")
                            },
                            leadingIcon = { Icon(Icons.Default.FileDownload, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Export as JSON") },
                            onClick = {
                                showMenu = false
                                exportFormat = "json"
                                exportLauncher.launch("${uiState.currentUnit.name.replace(" ", "_")}_shelfspace.json")
                            },
                            leadingIcon = { Icon(Icons.Default.FileDownload, contentDescription = null) }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Settings") },
                            onClick = { showMenu = false; onNavigateToSettings() },
                            leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("About") },
                            onClick = { showMenu = false; onNavigateToAbout() },
                            leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) }
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // UNIT SYSTEM SELECTOR
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "UNIT",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        DimensionUnit.entries.forEach { unit ->
                            FilterChip(
                                selected = unitSystem == unit,
                                onClick = { viewModel.setUnitSystem(unit) },
                                label = { Text(unit.symbol) }
                            )
                        }
                    }
                }
            }

            // CALCULATION MODE
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "CALCULATION MODE",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = uiState.calculationMode == CalculationMode.SHELF_CABINET,
                            onClick = { viewModel.setCalculationMode(CalculationMode.SHELF_CABINET) },
                            label = { Text("Shelf / Cabinet") }
                        )
                        FilterChip(
                            selected = uiState.calculationMode == CalculationMode.SINGLE_SHELF,
                            onClick = {
                                viewModel.setCalculationMode(CalculationMode.SINGLE_SHELF)
                                viewModel.updateShelfCount(1)
                            },
                            label = { Text("Single Shelf") }
                        )
                        FilterChip(
                            selected = false,
                            onClick = onNavigateToMultipleUnits,
                            label = { Text("Multiple Units") }
                        )
                        FilterChip(
                            selected = false,
                            onClick = onNavigateToMultipleUnits,
                            label = { Text("Custom Sections") }
                        )
                    }
                }
            }

            // STORAGE UNIT (DIMENSIONS)
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "STORAGE UNIT",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DimensionInputField(
                            label = "Overall Width",
                            valueText = widthStr,
                            onValueChange = {
                                widthStr = it
                                it.toDoubleOrNull()?.let { v ->
                                    if (v > 0) viewModel.updateDimensions(unitSystem.toMm(v), uiState.currentUnit.depthMm, uiState.currentUnit.heightMm)
                                }
                            },
                            unitSymbol = unitSystem.symbol,
                            modifier = Modifier.weight(1f)
                        )
                        DimensionInputField(
                            label = "Overall Depth",
                            valueText = depthStr,
                            onValueChange = {
                                depthStr = it
                                it.toDoubleOrNull()?.let { v ->
                                    if (v > 0) viewModel.updateDimensions(uiState.currentUnit.widthMm, unitSystem.toMm(v), uiState.currentUnit.heightMm)
                                }
                            },
                            unitSymbol = unitSystem.symbol,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    DimensionInputField(
                        label = "Overall Height",
                        valueText = heightStr,
                        onValueChange = {
                            heightStr = it
                            it.toDoubleOrNull()?.let { v ->
                                if (v > 0) viewModel.updateDimensions(uiState.currentUnit.widthMm, uiState.currentUnit.depthMm, unitSystem.toMm(v))
                            }
                        },
                        unitSymbol = unitSystem.symbol,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // CONSTRUCTION (SIDE, TOP, BOTTOM, BACK PANEL THICKNESS)
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "CONSTRUCTION & PANEL THICKNESS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DimensionInputField(
                            label = "Side Panels",
                            valueText = sidePanelStr,
                            onValueChange = {
                                sidePanelStr = it
                                it.toDoubleOrNull()?.let { v ->
                                    if (v >= 0) viewModel.updateConstruction(v, uiState.currentUnit.topThicknessMm, uiState.currentUnit.bottomThicknessMm, uiState.currentUnit.backPanelThicknessMm, uiState.currentUnit.hasBackPanel)
                                }
                            },
                            unitSymbol = "mm",
                            modifier = Modifier.weight(1f)
                        )
                        DimensionInputField(
                            label = "Shelf Board",
                            valueText = shelfThickStr,
                            onValueChange = {
                                shelfThickStr = it
                                it.toDoubleOrNull()?.let { v ->
                                    if (v > 0) viewModel.updateShelfThickness(v)
                                }
                            },
                            unitSymbol = "mm",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DimensionInputField(
                            label = "Top Panel",
                            valueText = topPanelStr,
                            onValueChange = {
                                topPanelStr = it
                                it.toDoubleOrNull()?.let { v ->
                                    if (v >= 0) viewModel.updateConstruction(uiState.currentUnit.sidePanelThicknessMm, v, uiState.currentUnit.bottomThicknessMm, uiState.currentUnit.backPanelThicknessMm, uiState.currentUnit.hasBackPanel)
                                }
                            },
                            unitSymbol = "mm",
                            modifier = Modifier.weight(1f)
                        )
                        DimensionInputField(
                            label = "Bottom Panel",
                            valueText = bottomPanelStr,
                            onValueChange = {
                                bottomPanelStr = it
                                it.toDoubleOrNull()?.let { v ->
                                    if (v >= 0) viewModel.updateConstruction(uiState.currentUnit.sidePanelThicknessMm, uiState.currentUnit.topThicknessMm, v, uiState.currentUnit.backPanelThicknessMm, uiState.currentUnit.hasBackPanel)
                                }
                            },
                            unitSymbol = "mm",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    // Back panel
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = uiState.currentUnit.hasBackPanel,
                                onCheckedChange = { checked ->
                                    viewModel.updateConstruction(
                                        uiState.currentUnit.sidePanelThicknessMm,
                                        uiState.currentUnit.topThicknessMm,
                                        uiState.currentUnit.bottomThicknessMm,
                                        uiState.currentUnit.backPanelThicknessMm,
                                        checked
                                    )
                                }
                            )
                            Text("Has Back Panel", style = MaterialTheme.typography.bodyMedium)
                        }

                        if (uiState.currentUnit.hasBackPanel) {
                            DimensionInputField(
                                label = "Back Panel",
                                valueText = backPanelStr,
                                onValueChange = {
                                    backPanelStr = it
                                    it.toDoubleOrNull()?.let { v ->
                                        if (v >= 0) viewModel.updateConstruction(uiState.currentUnit.sidePanelThicknessMm, uiState.currentUnit.topThicknessMm, uiState.currentUnit.bottomThicknessMm, v, true)
                                    }
                                },
                                unitSymbol = "mm",
                                modifier = Modifier.width(130.dp)
                            )
                        }
                    }

                    // Usable Internal Dimensions Callout
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "USABLE INTERNAL DIMENSIONS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Width: ${unitSystem.format(uiState.currentResult.usableWidthMm, decimals)}  •  Depth: ${unitSystem.format(uiState.currentResult.usableDepthMm, decimals)}  •  Height: ${unitSystem.format(uiState.currentResult.usableHeightMm, decimals)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // SHELVES & SPACING
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "SHELVES & COMPARTMENTS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DimensionInputField(
                            label = "Number of Shelves",
                            valueText = shelfCountStr,
                            onValueChange = {
                                shelfCountStr = it
                                it.toIntOrNull()?.let { c ->
                                    if (c >= 0) viewModel.updateShelfCount(c)
                                }
                            },
                            unitSymbol = "boards",
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f)
                        )

                        // Quick shelf count stepper
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedButton(
                                onClick = {
                                    val newCount = (uiState.currentUnit.shelfCount - 1).coerceAtLeast(0)
                                    viewModel.updateShelfCount(newCount)
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.size(44.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                            ) { Text("−", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
                            Spacer(Modifier.width(4.dp))
                            OutlinedButton(
                                onClick = {
                                    val newCount = (uiState.currentUnit.shelfCount + 1).coerceAtMost(100)
                                    viewModel.updateShelfCount(newCount)
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.size(44.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                            ) { Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    // Shelf Type & Spacing Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Shelf Type", style = MaterialTheme.typography.labelSmall)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                FilterChip(
                                    selected = uiState.currentUnit.shelfMode == ShelfMode.FIXED,
                                    onClick = { viewModel.updateShelfMode(ShelfMode.FIXED) },
                                    label = { Text("Fixed") }
                                )
                                FilterChip(
                                    selected = uiState.currentUnit.shelfMode == ShelfMode.ADJUSTABLE,
                                    onClick = { viewModel.updateShelfMode(ShelfMode.ADJUSTABLE) },
                                    label = { Text("Adjustable") }
                                )
                            }
                        }

                        Column {
                            Text("Spacing", style = MaterialTheme.typography.labelSmall)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                FilterChip(
                                    selected = uiState.currentUnit.spacingMode == SpacingMode.EQUAL,
                                    onClick = { viewModel.updateSpacingMode(SpacingMode.EQUAL) },
                                    label = { Text("Equal") }
                                )
                                FilterChip(
                                    selected = uiState.currentUnit.spacingMode == SpacingMode.CUSTOM,
                                    onClick = { viewModel.updateSpacingMode(SpacingMode.CUSTOM) },
                                    label = { Text("Custom") }
                                )
                            }
                        }
                    }

                    // Resulting compartment height display
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "EQUAL COMPARTMENT HEIGHT",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${uiState.currentResult.compartmentCount} compartments × ${unitSystem.format(uiState.currentResult.compartmentHeightMm, decimals)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // HOW MANY SHELVES CAN FIT? (MAX SHELF CALCULATOR)
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "HOW MANY SHELVES CAN FIT?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Calculate maximum practical shelves preserving minimum desired compartment clearance.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DimensionInputField(
                            label = "Minimum Clearance",
                            valueText = maxClearanceStr,
                            onValueChange = {
                                maxClearanceStr = it
                                it.toDoubleOrNull()?.let { v ->
                                    if (v > 0) viewModel.updateMaxShelfTargetClearance(unitSystem.toMm(v))
                                }
                            },
                            unitSymbol = unitSystem.symbol,
                            modifier = Modifier.weight(1f)
                        )
                        uiState.maxShelfResult?.let { maxRes ->
                            Button(
                                onClick = { viewModel.applyMaxShelvesToCabinet() },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(52.dp)
                            ) {
                                Text("Apply ${maxRes.maxShelves} Shelves")
                            }
                        }
                    }

                    uiState.maxShelfResult?.let { maxRes ->
                        Spacer(Modifier.height(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "MAXIMUM PRACTICAL SHELVES: ${maxRes.maxShelves}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = "Resulting clearance: approx. ${unitSystem.format(maxRes.resultingClearanceMm, decimals)} across ${maxRes.resultingCompartments} compartments",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }

            // PRIMARY RESULTS HERO CARD
            MainHeroResultCard(
                result = uiState.currentResult,
                decimals = decimals
            )

            // SECONDARY RESULT CARDS
            SecondaryResultCards(
                unit = uiState.currentUnit,
                result = uiState.currentResult,
                unitSystem = unitSystem,
                decimals = decimals
            )

            // DIAGRAM PREVIEW (WITH SHORTCUT TO STORAGE LAYOUT SCREEN)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "STORAGE ELEVATION",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        TextButton(onClick = onNavigateToLayout) {
                            Text("Full Diagram View →")
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    StorageDiagramCanvas(
                        unit = uiState.currentUnit,
                        result = uiState.currentResult,
                        viewMode = uiState.activeDiagramViewMode,
                        unitSystem = unitSystem,
                        decimals = decimals
                    )
                }
            }

            // ITEM STORAGE & CAPACITY SECTION
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "ITEM STORAGE & CAPACITY",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Test item fit, stacking, clearances & capacity",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                        Switch(
                            checked = uiState.itemStorageEnabled,
                            onCheckedChange = { viewModel.toggleItemStorage(it) }
                        )
                    }

                    if (uiState.itemStorageEnabled) {
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DimensionInputField(
                                label = "Item Width",
                                valueText = itemWidthStr,
                                onValueChange = {
                                    itemWidthStr = it
                                    it.toDoubleOrNull()?.let { v ->
                                        if (v > 0) viewModel.updateItem(unitSystem.toMm(v), uiState.currentItem.depthMm, uiState.currentItem.heightMm, uiState.currentItem.quantity, uiState.currentItem.allowStacking)
                                    }
                                },
                                unitSymbol = unitSystem.symbol,
                                modifier = Modifier.weight(1f)
                            )
                            DimensionInputField(
                                label = "Item Depth",
                                valueText = itemDepthStr,
                                onValueChange = {
                                    itemDepthStr = it
                                    it.toDoubleOrNull()?.let { v ->
                                        if (v > 0) viewModel.updateItem(uiState.currentItem.widthMm, unitSystem.toMm(v), uiState.currentItem.heightMm, uiState.currentItem.quantity, uiState.currentItem.allowStacking)
                                    }
                                },
                                unitSymbol = unitSystem.symbol,
                                modifier = Modifier.weight(1f)
                            )
                            DimensionInputField(
                                label = "Item Height",
                                valueText = itemHeightStr,
                                onValueChange = {
                                    itemHeightStr = it
                                    it.toDoubleOrNull()?.let { v ->
                                        if (v > 0) viewModel.updateItem(uiState.currentItem.widthMm, uiState.currentItem.depthMm, unitSystem.toMm(v), uiState.currentItem.quantity, uiState.currentItem.allowStacking)
                                    }
                                },
                                unitSymbol = unitSystem.symbol,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            DimensionInputField(
                                label = "Quantity",
                                valueText = itemQtyStr,
                                onValueChange = {
                                    itemQtyStr = it
                                    it.toIntOrNull()?.let { q ->
                                        if (q > 0) viewModel.updateItem(uiState.currentItem.widthMm, uiState.currentItem.depthMm, uiState.currentItem.heightMm, q, uiState.currentItem.allowStacking)
                                    }
                                },
                                unitSymbol = "pcs",
                                keyboardType = KeyboardType.Number,
                                modifier = Modifier.width(130.dp)
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = uiState.currentItem.allowStacking,
                                    onCheckedChange = { checked ->
                                        viewModel.updateItem(
                                            uiState.currentItem.widthMm,
                                            uiState.currentItem.depthMm,
                                            uiState.currentItem.heightMm,
                                            uiState.currentItem.quantity,
                                            checked
                                        )
                                    }
                                )
                                Text("Allow Stacking", style = MaterialTheme.typography.bodyMedium)
                            }
                        }

                        // Orientation Chips
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Orientation:", style = MaterialTheme.typography.labelSmall)
                            FilterChip(
                                selected = uiState.currentItem.orientation == ItemOrientation.AUTOMATIC,
                                onClick = {
                                    viewModel.updateItem(
                                        uiState.currentItem.widthMm,
                                        uiState.currentItem.depthMm,
                                        uiState.currentItem.heightMm,
                                        uiState.currentItem.quantity,
                                        uiState.currentItem.allowStacking,
                                        ItemOrientation.AUTOMATIC
                                    )
                                },
                                label = { Text("Auto") }
                            )
                            FilterChip(
                                selected = uiState.currentItem.orientation == ItemOrientation.FIXED,
                                onClick = {
                                    viewModel.updateItem(
                                        uiState.currentItem.widthMm,
                                        uiState.currentItem.depthMm,
                                        uiState.currentItem.heightMm,
                                        uiState.currentItem.quantity,
                                        uiState.currentItem.allowStacking,
                                        ItemOrientation.FIXED
                                    )
                                },
                                label = { Text("Fixed") }
                            )
                        }

                        // Item Capacity Result Card
                        uiState.currentItemFit?.let { fitRes ->
                            Spacer(Modifier.height(10.dp))
                            ItemCapacityResultCard(
                                fitResult = fitRes,
                                unitSystem = unitSystem,
                                decimals = decimals
                            )
                        }
                    }
                }
            }

            // STORAGE UTILIZATION (PROGRESS BAR)
            uiState.storageUtilization?.let { util ->
                StorageUtilizationCard(utilization = util)
            }

            // HOW WAS THIS CALCULATED? BREAKDOWN CARD
            CalculationBreakdownCard(
                unit = uiState.currentUnit,
                result = uiState.currentResult,
                unitSystem = unitSystem,
                decimals = decimals
            )

            // ACTION BUTTONS ROW
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.saveCurrentToHistory() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Save")
                }
                OutlinedButton(
                    onClick = { viewModel.shareCurrentCalculation(context) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Share")
                }
                OutlinedButton(
                    onClick = onNavigateToLayout,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Dashboard, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Layout")
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
