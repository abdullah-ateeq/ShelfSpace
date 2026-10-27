package com.example.shelfspace.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.shelfspace.calculator.ShelfCalculator
import com.example.shelfspace.calculator.StorageCapacityCalculator
import com.example.shelfspace.data.ExportHelper
import com.example.shelfspace.data.PreferencesManager
import com.example.shelfspace.model.AppSettings
import com.example.shelfspace.model.CalculationMode
import com.example.shelfspace.model.CustomSection
import com.example.shelfspace.model.DimensionUnit
import com.example.shelfspace.model.ItemFitResult
import com.example.shelfspace.model.ItemOrientation
import com.example.shelfspace.model.MaxShelfResult
import com.example.shelfspace.model.SavedCalculation
import com.example.shelfspace.model.ShelfMode
import com.example.shelfspace.model.ShelfResult
import com.example.shelfspace.model.SpacingMode
import com.example.shelfspace.model.StorageItem
import com.example.shelfspace.model.StorageUnit
import com.example.shelfspace.model.StorageUtilizationResult
import com.example.shelfspace.ui.components.DiagramViewMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class ShelfSpaceUiState(
    val currentUnit: StorageUnit = StorageUnit(
        id = UUID.randomUUID().toString(),
        name = "Living Room Cabinet",
        widthMm = 1200.0, // 120 cm
        depthMm = 400.0,  // 40 cm
        heightMm = 1800.0, // 180 cm
        sidePanelThicknessMm = 18.0,
        topThicknessMm = 18.0,
        bottomThicknessMm = 18.0,
        backPanelThicknessMm = 6.0,
        hasBackPanel = true,
        shelfThicknessMm = 18.0,
        shelfCount = 4,
        shelfMode = ShelfMode.FIXED,
        spacingMode = SpacingMode.EQUAL
    ),
    val currentResult: ShelfResult = ShelfResult(
        usableWidthMm = 1164.0,
        usableDepthMm = 394.0,
        usableHeightMm = 1764.0,
        shelfAreaMm2 = 458616.0,
        totalShelfAreaMm2 = 1834464.0,
        linearShelfLengthMm = 4656.0,
        storageVolumeMm3 = 808998624.0,
        compartmentHeightMm = 338.4,
        shelfCount = 4
    ),
    val selectedUnitSystem: DimensionUnit = DimensionUnit.CENTIMETERS,
    val calculationMode: CalculationMode = CalculationMode.SHELF_CABINET,
    val itemStorageEnabled: Boolean = true,
    val currentItem: StorageItem = StorageItem(
        id = "default-item",
        name = "Standard Box",
        widthMm = 300.0, // 30 cm
        depthMm = 200.0, // 20 cm
        heightMm = 200.0, // 20 cm
        quantity = 1,
        allowStacking = false,
        orientation = ItemOrientation.AUTOMATIC
    ),
    val currentItemFit: ItemFitResult? = null,
    val additionalItems: List<StorageItem> = emptyList(),
    val storageUtilization: StorageUtilizationResult? = null,
    val maxShelfTargetClearanceMm: Double = 300.0, // 30 cm
    val maxShelfResult: MaxShelfResult? = null,
    val multipleUnits: List<StorageUnit> = emptyList(),
    val customSections: List<CustomSection> = emptyList(),
    val historyList: List<SavedCalculation> = emptyList(),
    val settings: AppSettings = AppSettings(),
    val activeDiagramViewMode: DiagramViewMode = DiagramViewMode.FRONT,
    val snackbarMessage: String? = null
)

class ShelfSpaceViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = PreferencesManager(application)
    private val _uiState = MutableStateFlow(ShelfSpaceUiState())
    val uiState: StateFlow<ShelfSpaceUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        val loadedSettings = prefs.loadSettings()
        val loadedHistory = prefs.loadHistory()
        val loadedUnits = prefs.loadMultipleUnits()
        val loadedSections = prefs.loadCustomSections()

        val unit = if (loadedSettings.restoreLastCalculation) {
            prefs.loadLastUnit() ?: _uiState.value.currentUnit
        } else {
            _uiState.value.currentUnit
        }

        _uiState.update { current ->
            current.copy(
                settings = loadedSettings,
                selectedUnitSystem = loadedSettings.defaultUnit,
                historyList = loadedHistory,
                multipleUnits = loadedUnits,
                customSections = loadedSections,
                currentUnit = unit.copy(
                    shelfThicknessMm = if (unit.shelfThicknessMm > 0) unit.shelfThicknessMm else loadedSettings.defaultShelfThicknessMm,
                    sidePanelThicknessMm = if (unit.sidePanelThicknessMm > 0) unit.sidePanelThicknessMm else loadedSettings.defaultSidePanelThicknessMm,
                    topThicknessMm = if (unit.topThicknessMm > 0) unit.topThicknessMm else loadedSettings.defaultTopThicknessMm,
                    bottomThicknessMm = if (unit.bottomThicknessMm > 0) unit.bottomThicknessMm else loadedSettings.defaultBottomThicknessMm,
                    backPanelThicknessMm = if (unit.backPanelThicknessMm > 0) unit.backPanelThicknessMm else loadedSettings.defaultBackPanelThicknessMm
                ),
                maxShelfTargetClearanceMm = loadedSettings.defaultMinimumClearanceMm
            )
        }
        recalculateAll()
    }

    // --- Calculation Trigger ---

    fun recalculateAll() {
        val state = _uiState.value
        val result = ShelfCalculator.calculate(state.currentUnit)

        // Calculate item capacity if enabled
        val itemFit = if (state.itemStorageEnabled && result.isValid) {
            StorageCapacityCalculator.calculateItemFit(
                shelfWidthMm = result.usableWidthMm,
                shelfDepthMm = result.usableDepthMm,
                compartmentHeightMm = result.compartmentHeightMm,
                item = state.currentItem,
                totalCompartments = result.compartmentCount
            )
        } else null

        // Calculate utilization across all items
        val allItems = buildList {
            if (state.itemStorageEnabled) add(state.currentItem)
            addAll(state.additionalItems)
        }
        val utilization = if (result.isValid) {
            StorageCapacityCalculator.calculateStorageUtilization(
                usableStorageVolumeMm3 = result.storageVolumeMm3,
                totalShelfAreaMm2 = result.totalShelfAreaMm2,
                items = allItems
            )
        } else null

        // Calculate max practical shelves
        val maxResult = if (result.usableHeightMm > 0) {
            ShelfCalculator.calculateMaxShelves(
                usableHeightMm = result.usableHeightMm,
                shelfThicknessMm = state.currentUnit.shelfThicknessMm,
                minimumClearanceMm = state.maxShelfTargetClearanceMm,
                usableWidthMm = result.usableWidthMm,
                usableDepthMm = result.usableDepthMm
            )
        } else null

        _uiState.update {
            it.copy(
                currentResult = result,
                currentItemFit = itemFit,
                storageUtilization = utilization,
                maxShelfResult = maxResult
            )
        }

        // Save last state
        if (state.settings.restoreLastCalculation) {
            prefs.saveLastUnit(state.currentUnit)
        }
    }

    // --- Unit & Dimension Updates ---

    fun updateUnitName(name: String) {
        _uiState.update { it.copy(currentUnit = it.currentUnit.copy(name = name)) }
    }

    fun updateDimensions(widthMm: Double, depthMm: Double, heightMm: Double) {
        _uiState.update {
            it.copy(
                currentUnit = it.currentUnit.copy(
                    widthMm = widthMm,
                    depthMm = depthMm,
                    heightMm = heightMm
                )
            )
        }
        recalculateAll()
    }

    fun updateConstruction(
        sidePanelMm: Double,
        topMm: Double,
        bottomMm: Double,
        backMm: Double,
        hasBack: Boolean
    ) {
        _uiState.update {
            it.copy(
                currentUnit = it.currentUnit.copy(
                    sidePanelThicknessMm = sidePanelMm,
                    topThicknessMm = topMm,
                    bottomThicknessMm = bottomMm,
                    backPanelThicknessMm = backMm,
                    hasBackPanel = hasBack
                )
            )
        }
        recalculateAll()
    }

    fun updateShelfCount(count: Int) {
        val safeCount = count.coerceIn(0, 100)
        _uiState.update {
            it.copy(
                currentUnit = it.currentUnit.copy(shelfCount = safeCount)
            )
        }
        recalculateAll()
    }

    fun updateShelfThickness(thicknessMm: Double) {
        _uiState.update {
            it.copy(
                currentUnit = it.currentUnit.copy(shelfThicknessMm = thicknessMm)
            )
        }
        recalculateAll()
    }

    fun updateShelfMode(mode: ShelfMode) {
        _uiState.update { it.copy(currentUnit = it.currentUnit.copy(shelfMode = mode)) }
    }

    fun updateSpacingMode(mode: SpacingMode) {
        _uiState.update { it.copy(currentUnit = it.currentUnit.copy(spacingMode = mode)) }
        recalculateAll()
    }

    fun updateMaxShelfTargetClearance(clearanceMm: Double) {
        _uiState.update { it.copy(maxShelfTargetClearanceMm = clearanceMm) }
        recalculateAll()
    }

    fun applyMaxShelvesToCabinet() {
        val maxShelves = _uiState.value.maxShelfResult?.maxShelves ?: return
        updateShelfCount(maxShelves)
    }

    // --- Item Updates ---

    fun toggleItemStorage(enabled: Boolean) {
        _uiState.update { it.copy(itemStorageEnabled = enabled) }
        recalculateAll()
    }

    fun updateItem(
        widthMm: Double,
        depthMm: Double,
        heightMm: Double,
        quantity: Int,
        allowStacking: Boolean,
        orientation: ItemOrientation = _uiState.value.currentItem.orientation
    ) {
        _uiState.update {
            it.copy(
                currentItem = it.currentItem.copy(
                    widthMm = widthMm,
                    depthMm = depthMm,
                    heightMm = heightMm,
                    quantity = quantity.coerceAtLeast(1),
                    allowStacking = allowStacking,
                    orientation = orientation
                )
            )
        }
        recalculateAll()
    }

    fun addAdditionalItem(item: StorageItem) {
        _uiState.update {
            it.copy(additionalItems = it.additionalItems + item)
        }
        recalculateAll()
    }

    fun removeAdditionalItem(id: String) {
        _uiState.update {
            it.copy(additionalItems = it.additionalItems.filterNot { it.id == id })
        }
        recalculateAll()
    }

    // --- Unit System & Mode Selection ---

    fun setUnitSystem(unit: DimensionUnit) {
        _uiState.update { it.copy(selectedUnitSystem = unit) }
    }

    fun setCalculationMode(mode: CalculationMode) {
        _uiState.update { it.copy(calculationMode = mode) }
    }

    fun setDiagramViewMode(viewMode: DiagramViewMode) {
        _uiState.update { it.copy(activeDiagramViewMode = viewMode) }
    }

    // --- History Management (Max 10) ---

    fun saveCurrentToHistory() {
        val state = _uiState.value
        val historyItem = SavedCalculation(
            id = UUID.randomUUID().toString(),
            name = state.currentUnit.name,
            timestamp = System.currentTimeMillis(),
            unit = state.currentUnit,
            result = state.currentResult,
            items = buildList {
                if (state.itemStorageEnabled) add(state.currentItem)
                addAll(state.additionalItems)
            }
        )
        val updated = prefs.saveHistoryItem(historyItem)
        _uiState.update {
            it.copy(
                historyList = updated,
                snackbarMessage = "Calculation saved to history"
            )
        }
    }

    fun loadHistoryItem(item: SavedCalculation) {
        _uiState.update {
            it.copy(
                currentUnit = item.unit.copy(id = UUID.randomUUID().toString()),
                currentItem = item.items.firstOrNull() ?: it.currentItem,
                additionalItems = if (item.items.size > 1) item.items.subList(1, item.items.size) else emptyList(),
                calculationMode = CalculationMode.SHELF_CABINET,
                snackbarMessage = "Loaded '${item.name}'"
            )
        }
        recalculateAll()
    }

    fun duplicateHistoryItem(item: SavedCalculation) {
        val duplicated = item.copy(
            id = UUID.randomUUID().toString(),
            name = "${item.name} (Copy)",
            timestamp = System.currentTimeMillis()
        )
        val updated = prefs.saveHistoryItem(duplicated)
        _uiState.update {
            it.copy(
                historyList = updated,
                snackbarMessage = "Calculation duplicated"
            )
        }
    }

    fun deleteHistoryItem(id: String) {
        val updated = prefs.deleteHistoryItem(id)
        _uiState.update {
            it.copy(
                historyList = updated,
                snackbarMessage = "Calculation removed from history"
            )
        }
    }

    // --- Multiple Units Management ---

    fun addMultipleUnit(unit: StorageUnit) {
        val updated = _uiState.value.multipleUnits + unit
        prefs.saveMultipleUnits(updated)
        _uiState.update { it.copy(multipleUnits = updated) }
    }

    fun updateMultipleUnit(unit: StorageUnit) {
        val updated = _uiState.value.multipleUnits.map { if (it.id == unit.id) unit else it }
        prefs.saveMultipleUnits(updated)
        _uiState.update { it.copy(multipleUnits = updated) }
    }

    fun deleteMultipleUnit(id: String) {
        val updated = _uiState.value.multipleUnits.filterNot { it.id == id }
        prefs.saveMultipleUnits(updated)
        _uiState.update { it.copy(multipleUnits = updated) }
    }

    // --- Custom Sections Management ---

    fun addCustomSection(section: CustomSection) {
        val updated = _uiState.value.customSections + section
        prefs.saveCustomSections(updated)
        _uiState.update { it.copy(customSections = updated) }
    }

    fun deleteCustomSection(id: String) {
        val updated = _uiState.value.customSections.filterNot { it.id == id }
        prefs.saveCustomSections(updated)
        _uiState.update { it.copy(customSections = updated) }
    }

    // --- Settings Updates ---

    fun updateSettings(settings: AppSettings) {
        prefs.saveSettings(settings)
        _uiState.update { it.copy(settings = settings, selectedUnitSystem = settings.defaultUnit) }
        recalculateAll()
    }

    fun resetSettings() {
        val defaults = prefs.resetSettings()
        _uiState.update { it.copy(settings = defaults, selectedUnitSystem = defaults.defaultUnit) }
        recalculateAll()
    }

    // --- Export & Share ---

    fun shareCurrentCalculation(context: Context) {
        val state = _uiState.value
        val shareText = ExportHelper.generateShareText(
            unit = state.currentUnit,
            result = state.currentResult,
            unitSystem = state.selectedUnitSystem,
            decimals = state.settings.decimalPlaces
        )
        ExportHelper.shareContent(context, shareText)
    }

    fun exportToUri(context: Context, uri: Uri, format: String): Boolean {
        val state = _uiState.value
        val content = when (format.lowercase()) {
            "csv" -> ExportHelper.generateCsv(state.currentUnit, state.currentResult, state.selectedUnitSystem, state.settings.decimalPlaces)
            "json" -> ExportHelper.generateJson(state.currentUnit, state.currentResult, buildList {
                if (state.itemStorageEnabled) add(state.currentItem)
                addAll(state.additionalItems)
            })
            else -> ExportHelper.generateTxt(state.currentUnit, state.currentResult, state.selectedUnitSystem, state.settings.decimalPlaces)
        }
        val success = ExportHelper.writeToUri(context, uri, content)
        if (success) {
            _uiState.update { it.copy(snackbarMessage = "Exported successfully as ${format.uppercase()}") }
        } else {
            _uiState.update { it.copy(snackbarMessage = "Export failed") }
        }
        return success
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}
