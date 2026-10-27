package com.example.shelfspace.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.text.format.DateUtils
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ControlPointDuplicate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
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
import com.example.shelfspace.data.ExportHelper
import com.example.shelfspace.model.SavedCalculation
import com.example.shelfspace.viewmodel.ShelfSpaceViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: ShelfSpaceViewModel,
    onNavigateBack: () -> Unit,
    onOpenCalculation: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val unitSystem = uiState.selectedUnitSystem
    val decimals = uiState.settings.decimalPlaces

    var itemToDelete by remember { mutableStateOf<SavedCalculation?>(null) }
    var itemToExport by remember { mutableStateOf<SavedCalculation?>(null) }
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
        if (uri != null && itemToExport != null) {
            val calc = itemToExport!!
            val content = when (exportFormat) {
                "csv" -> ExportHelper.generateCsv(calc.unit, calc.result, unitSystem, decimals)
                "json" -> ExportHelper.generateJson(calc.unit, calc.result, calc.items)
                else -> ExportHelper.generateTxt(calc.unit, calc.result, unitSystem, decimals)
            }
            val ok = ExportHelper.writeToUri(context, uri, content)
            if (ok) {
                Toast.makeText(context, "Exported successfully as ${exportFormat.uppercase()}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("History (${uiState.historyList.size}/10)", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        if (uiState.historyList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.BookmarkBorder,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "No saved calculations yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "Save up to 10 calculations from the Home screen",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(uiState.historyList, key = { it.id }) { item ->
                    val relativeTime = DateUtils.getRelativeTimeSpanString(
                        item.timestamp,
                        System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS
                    ).toString()

                    var showCardMenu by remember { mutableStateOf(false) }

                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column {
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "${unitSystem.format(item.unit.widthMm, decimals)} × ${unitSystem.format(item.unit.depthMm, decimals)} × ${unitSystem.format(item.unit.heightMm, decimals)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = relativeTime,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }

                                Row {
                                    IconButton(onClick = {
                                        viewModel.loadHistoryItem(item)
                                        onOpenCalculation()
                                    }) {
                                        Icon(Icons.Default.FolderOpen, contentDescription = "Open", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(onClick = {
                                        if (uiState.settings.confirmDelete) {
                                            itemToDelete = item
                                        } else {
                                            viewModel.deleteHistoryItem(item.id)
                                        }
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Shelves", style = MaterialTheme.typography.labelSmall)
                                    Text("${item.result.shelfCount}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                }
                                Column {
                                    Text("Shelf length", style = MaterialTheme.typography.labelSmall)
                                    Text(String.format(Locale.US, "%.${decimals}f m", item.result.linearShelfLengthM), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                }
                                Column {
                                    Text("Shelf Area", style = MaterialTheme.typography.labelSmall)
                                    Text(String.format(Locale.US, "%.${decimals}f m²", item.result.totalShelfAreaM2), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                }
                                Column {
                                    Text("Storage", style = MaterialTheme.typography.labelSmall)
                                    Text(String.format(Locale.US, "%.1f L", item.result.storageVolumeLiters), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(Modifier.height(10.dp))

                            // Action buttons row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        viewModel.loadHistoryItem(item)
                                        onOpenCalculation()
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Open")
                                }
                                OutlinedButton(
                                    onClick = { viewModel.duplicateHistoryItem(item) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.ControlPointDuplicate, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Copy")
                                }
                                OutlinedButton(
                                    onClick = {
                                        val shareText = ExportHelper.generateShareText(item.unit, item.result, unitSystem, decimals)
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("ShelfSpace Calculation", shareText))
                                        Toast.makeText(context, "Copied calculation to clipboard", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Clip")
                                }
                                Box {
                                    OutlinedButton(
                                        onClick = { showCardMenu = true },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.FileDownload, contentDescription = "Export", modifier = Modifier.size(16.dp))
                                    }
                                    DropdownMenu(
                                        expanded = showCardMenu,
                                        onDismissRequest = { showCardMenu = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Export as TXT") },
                                            onClick = {
                                                showCardMenu = false
                                                itemToExport = item
                                                exportFormat = "txt"
                                                exportLauncher.launch("${item.name.replace(" ", "_")}_shelfspace.txt")
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Export as CSV") },
                                            onClick = {
                                                showCardMenu = false
                                                itemToExport = item
                                                exportFormat = "csv"
                                                exportLauncher.launch("${item.name.replace(" ", "_")}_shelfspace.csv")
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Export as JSON") },
                                            onClick = {
                                                showCardMenu = false
                                                itemToExport = item
                                                exportFormat = "json"
                                                exportLauncher.launch("${item.name.replace(" ", "_")}_shelfspace.json")
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Confirm Delete Dialog
    itemToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Delete Calculation") },
            text = { Text("Are you sure you want to remove '${item.name}' from your history?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteHistoryItem(item.id)
                        itemToDelete = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) { Text("Cancel") }
            }
        )
    }
}
