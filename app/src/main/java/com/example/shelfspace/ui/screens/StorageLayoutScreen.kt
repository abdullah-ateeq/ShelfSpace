package com.example.shelfspace.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.shelfspace.ui.components.DiagramViewMode
import com.example.shelfspace.ui.components.StorageDiagramCanvas
import com.example.shelfspace.viewmodel.ShelfSpaceViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageLayoutScreen(
    viewModel: ShelfSpaceViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedView by remember { mutableStateOf(DiagramViewMode.FRONT) }
    val unitSystem = uiState.selectedUnitSystem
    val decimals = uiState.settings.decimalPlaces
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Storage Layout", fontWeight = FontWeight.Bold) },
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
            // View Mode Selector Chips
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FilterChip(
                        selected = selectedView == DiagramViewMode.FRONT,
                        onClick = { selectedView = DiagramViewMode.FRONT },
                        label = { Text("Front Elevation") }
                    )
                    FilterChip(
                        selected = selectedView == DiagramViewMode.SIDE,
                        onClick = { selectedView = DiagramViewMode.SIDE },
                        label = { Text("Side Profile") }
                    )
                    FilterChip(
                        selected = selectedView == DiagramViewMode.TOP,
                        onClick = { selectedView = DiagramViewMode.TOP },
                        label = { Text("Top Area") }
                    )
                }
            }

            // Description of current view
            Text(
                text = when (selectedView) {
                    DiagramViewMode.FRONT -> "Front Elevation: shows overall cabinet dimensions, side panel offsets, shelf planks, and compartment spacing. Tap any shelf to inspect."
                    DiagramViewMode.SIDE -> "Side View: shows overall depth, back panel deduction, and usable depth."
                    DiagramViewMode.TOP -> "Top View: shows width × depth footprint and surface area of each shelf board."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            // Canvas Blueprint Diagram
            StorageDiagramCanvas(
                unit = uiState.currentUnit,
                result = uiState.currentResult,
                viewMode = selectedView,
                unitSystem = unitSystem,
                decimals = decimals
            )

            // Layout Metrics Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "ARCHITECTURAL SPECIFICATIONS",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Overall Dimensions:", style = MaterialTheme.typography.bodySmall)
                        Text(
                            "${unitSystem.format(uiState.currentUnit.widthMm, decimals)} × ${unitSystem.format(uiState.currentUnit.depthMm, decimals)} × ${unitSystem.format(uiState.currentUnit.heightMm, decimals)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Usable Internal Space:", style = MaterialTheme.typography.bodySmall)
                        Text(
                            "${unitSystem.format(uiState.currentResult.usableWidthMm, decimals)} × ${unitSystem.format(uiState.currentResult.usableDepthMm, decimals)} × ${unitSystem.format(uiState.currentResult.usableHeightMm, decimals)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Shelves / Compartments:", style = MaterialTheme.typography.bodySmall)
                        Text(
                            "${uiState.currentResult.shelfCount} shelves  /  ${uiState.currentResult.compartmentCount} compartments",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Clearance per Compartment:", style = MaterialTheme.typography.bodySmall)
                        Text(
                            unitSystem.format(uiState.currentResult.compartmentHeightMm, decimals),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Linear Usable Frontage:", style = MaterialTheme.typography.bodySmall)
                        Text(
                            String.format(Locale.US, "%.${decimals}f linear m", uiState.currentResult.linearShelfLengthM),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Usable Surface Area:", style = MaterialTheme.typography.bodySmall)
                        Text(
                            String.format(Locale.US, "%.${decimals}f m²", uiState.currentResult.totalShelfAreaM2),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Storage Volume:", style = MaterialTheme.typography.bodySmall)
                        Text(
                            String.format(Locale.US, "%.1f Liters", uiState.currentResult.storageVolumeLiters),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
