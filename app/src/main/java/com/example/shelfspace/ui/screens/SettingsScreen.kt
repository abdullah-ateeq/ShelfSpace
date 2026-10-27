package com.example.shelfspace.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.shelfspace.model.AppSettings
import com.example.shelfspace.model.DimensionUnit
import com.example.shelfspace.ui.components.DimensionInputField
import com.example.shelfspace.viewmodel.ShelfSpaceViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: ShelfSpaceViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val settings = uiState.settings
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var showResetDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
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
            // APPEARANCE
            SettingsSectionCard(title = "APPEARANCE") {
                Text("Theme", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("System", "Light", "Dark").forEach { mode ->
                        FilterChip(
                            selected = settings.themeMode == mode,
                            onClick = { viewModel.updateSettings(settings.copy(themeMode = mode)) },
                            label = { Text(mode) }
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
                SettingToggleRow(
                    title = "Dynamic Material Color",
                    subtitle = "Adapt color theme based on device wallpaper (Android 12+)",
                    checked = settings.dynamicColor,
                    onCheckedChange = { viewModel.updateSettings(settings.copy(dynamicColor = it)) }
                )
            }

            // CALCULATOR DEFAULTS
            SettingsSectionCard(title = "CALCULATOR DEFAULTS") {
                Text("Default Dimension Unit", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    DimensionUnit.entries.forEach { u ->
                        FilterChip(
                            selected = settings.defaultUnit == u,
                            onClick = { viewModel.updateSettings(settings.copy(defaultUnit = u)) },
                            label = { Text(u.symbol) }
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DimensionInputField(
                        label = "Shelf Thickness",
                        valueText = String.format(Locale.US, "%.1f", settings.defaultShelfThicknessMm),
                        onValueChange = {
                            it.toDoubleOrNull()?.let { v ->
                                if (v > 0) viewModel.updateSettings(settings.copy(defaultShelfThicknessMm = v))
                            }
                        },
                        unitSymbol = "mm",
                        modifier = Modifier.weight(1f)
                    )
                    DimensionInputField(
                        label = "Side Panels",
                        valueText = String.format(Locale.US, "%.1f", settings.defaultSidePanelThicknessMm),
                        onValueChange = {
                            it.toDoubleOrNull()?.let { v ->
                                if (v >= 0) viewModel.updateSettings(settings.copy(defaultSidePanelThicknessMm = v))
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
                        valueText = String.format(Locale.US, "%.1f", settings.defaultTopThicknessMm),
                        onValueChange = {
                            it.toDoubleOrNull()?.let { v ->
                                if (v >= 0) viewModel.updateSettings(settings.copy(defaultTopThicknessMm = v))
                            }
                        },
                        unitSymbol = "mm",
                        modifier = Modifier.weight(1f)
                    )
                    DimensionInputField(
                        label = "Bottom Panel",
                        valueText = String.format(Locale.US, "%.1f", settings.defaultBottomThicknessMm),
                        onValueChange = {
                            it.toDoubleOrNull()?.let { v ->
                                if (v >= 0) viewModel.updateSettings(settings.copy(defaultBottomThicknessMm = v))
                            }
                        },
                        unitSymbol = "mm",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DimensionInputField(
                        label = "Back Panel",
                        valueText = String.format(Locale.US, "%.1f", settings.defaultBackPanelThicknessMm),
                        onValueChange = {
                            it.toDoubleOrNull()?.let { v ->
                                if (v >= 0) viewModel.updateSettings(settings.copy(defaultBackPanelThicknessMm = v))
                            }
                        },
                        unitSymbol = "mm",
                        modifier = Modifier.weight(1f)
                    )
                    DimensionInputField(
                        label = "Min Clearance",
                        valueText = String.format(Locale.US, "%.1f", settings.defaultMinimumClearanceMm / 10.0),
                        onValueChange = {
                            it.toDoubleOrNull()?.let { v ->
                                if (v > 0) viewModel.updateSettings(settings.copy(defaultMinimumClearanceMm = v * 10.0))
                            }
                        },
                        unitSymbol = "cm",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // PRECISION
            SettingsSectionCard(title = "DISPLAY PRECISION") {
                Text("Decimal Places (Displayed Values)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (0..4).forEach { p ->
                        FilterChip(
                            selected = settings.decimalPlaces == p,
                            onClick = { viewModel.updateSettings(settings.copy(decimalPlaces = p)) },
                            label = { Text("$p decimals") }
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Full precision is always maintained internally in exact millimeters.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            // DISPLAY SETTINGS
            SettingsSectionCard(title = "DISPLAY SETTINGS") {
                SettingToggleRow(
                    title = "Show Front Diagram",
                    subtitle = "Show architectural front elevation on Home",
                    checked = settings.showFrontDiagram,
                    onCheckedChange = { viewModel.updateSettings(settings.copy(showFrontDiagram = it)) }
                )
                SettingToggleRow(
                    title = "Show Side Diagram",
                    subtitle = "Show side depth view in diagram viewer",
                    checked = settings.showSideDiagram,
                    onCheckedChange = { viewModel.updateSettings(settings.copy(showSideDiagram = it)) }
                )
                SettingToggleRow(
                    title = "Show Top Diagram",
                    subtitle = "Show shelf area footprint diagram",
                    checked = settings.showTopDiagram,
                    onCheckedChange = { viewModel.updateSettings(settings.copy(showTopDiagram = it)) }
                )
                SettingToggleRow(
                    title = "Show Calculation Breakdown",
                    subtitle = "Show step-by-step mathematical explanation",
                    checked = settings.showCalculationBreakdown,
                    onCheckedChange = { viewModel.updateSettings(settings.copy(showCalculationBreakdown = it)) }
                )
                SettingToggleRow(
                    title = "Show Item Capacity",
                    subtitle = "Show item fit and packing calculator",
                    checked = settings.showItemCapacity,
                    onCheckedChange = { viewModel.updateSettings(settings.copy(showItemCapacity = it)) }
                )
                SettingToggleRow(
                    title = "Show Storage Utilization",
                    subtitle = "Show volume occupancy progress indicator",
                    checked = settings.showStorageUtilization,
                    onCheckedChange = { viewModel.updateSettings(settings.copy(showStorageUtilization = it)) }
                )
            }

            // BEHAVIOR
            SettingsSectionCard(title = "BEHAVIOR") {
                SettingToggleRow(
                    title = "Live Calculation",
                    subtitle = "Recalculate immediately as inputs change",
                    checked = settings.liveCalculation,
                    onCheckedChange = { viewModel.updateSettings(settings.copy(liveCalculation = it)) }
                )
                SettingToggleRow(
                    title = "Haptic Feedback",
                    subtitle = "Vibrate on button taps and shelf selection",
                    checked = settings.hapticsEnabled,
                    onCheckedChange = { viewModel.updateSettings(settings.copy(hapticsEnabled = it)) }
                )
                SettingToggleRow(
                    title = "Animations",
                    subtitle = "Smooth UI transitions and diagrams",
                    checked = settings.animationsEnabled,
                    onCheckedChange = { viewModel.updateSettings(settings.copy(animationsEnabled = it)) }
                )
                SettingToggleRow(
                    title = "Confirm Delete",
                    subtitle = "Prompt before deleting items from history",
                    checked = settings.confirmDelete,
                    onCheckedChange = { viewModel.updateSettings(settings.copy(confirmDelete = it)) }
                )
                SettingToggleRow(
                    title = "Restore Last Calculation",
                    subtitle = "Automatically load previous calculation when app opens",
                    checked = settings.restoreLastCalculation,
                    onCheckedChange = { viewModel.updateSettings(settings.copy(restoreLastCalculation = it)) }
                )
            }

            // RESET SETTINGS BUTTON
            OutlinedButton(
                onClick = { showResetDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Reset Settings")
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Settings") },
            text = { Text("This will reset all preferences and default dimensions back to original factory defaults. Your saved calculation history will NOT be deleted.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetSettings()
                        showResetDialog = false
                        Toast.makeText(context, "Settings reset to defaults", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
