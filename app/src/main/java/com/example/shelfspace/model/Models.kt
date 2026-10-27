package com.example.shelfspace.model

import java.util.Locale

/**
 * Supported measurement units with conversion to/from internal millimeters (mm).
 */
enum class DimensionUnit(val symbol: String, val displayName: String, val mmFactor: Double) {
    MILLIMETERS("mm", "Millimeters", 1.0),
    CENTIMETERS("cm", "Centimeters", 10.0),
    METERS("m", "Meters", 1000.0),
    INCHES("in", "Inches", 25.4),
    FEET("ft", "Feet", 304.8);

    fun toMm(value: Double): Double = value * mmFactor
    fun fromMm(valueMm: Double): Double = valueMm / mmFactor

    fun format(valueMm: Double, decimals: Int = 2): String {
        val converted = fromMm(valueMm)
        return String.format(Locale.US, "%.${decimals}f %s", converted, symbol)
    }

    fun formatValueOnly(valueMm: Double, decimals: Int = 2): String {
        val converted = fromMm(valueMm)
        return String.format(Locale.US, "%.${decimals}f", converted)
    }

    companion object {
        fun fromSymbol(symbol: String): DimensionUnit {
            return entries.firstOrNull { it.symbol.equals(symbol, ignoreCase = true) } ?: CENTIMETERS
        }
    }
}

enum class ShelfMode {
    FIXED,
    ADJUSTABLE,
    MAXIMUM
}

enum class SpacingMode {
    EQUAL,
    CUSTOM
}

enum class ItemOrientation {
    AUTOMATIC,
    FIXED,
    ROTATION_ALLOWED
}

enum class CalculationMode {
    SHELF_CABINET,
    SINGLE_SHELF,
    MULTIPLE_UNITS,
    CUSTOM_SECTIONS
}

/**
 * Storage Unit model as specified in requirements.
 */
data class StorageUnit(
    val id: String,
    val name: String,
    val widthMm: Double,
    val depthMm: Double,
    val heightMm: Double,
    val sidePanelThicknessMm: Double = 18.0,
    val topThicknessMm: Double = 18.0,
    val bottomThicknessMm: Double = 18.0,
    val backPanelThicknessMm: Double = 6.0,
    val hasBackPanel: Boolean = true,
    val shelfThicknessMm: Double = 18.0,
    val shelfCount: Int = 4,
    val shelfMode: ShelfMode = ShelfMode.FIXED,
    val spacingMode: SpacingMode = SpacingMode.EQUAL,
    val customSpacingMm: List<Double> = emptyList(),
    val customShelfThicknessesMm: List<Double> = emptyList()
)

/**
 * Storage Item model as specified in requirements.
 */
data class StorageItem(
    val id: String,
    val name: String,
    val widthMm: Double,
    val depthMm: Double,
    val heightMm: Double,
    val quantity: Int = 1,
    val allowStacking: Boolean = false,
    val orientation: ItemOrientation = ItemOrientation.AUTOMATIC
)

/**
 * Custom Section model for pantries, wardrobes, bookcases, etc.
 */
data class CustomSection(
    val id: String,
    val name: String,
    val widthMm: Double,
    val depthMm: Double,
    val heightMm: Double,
    val shelfCount: Int = 3,
    val shelfThicknessMm: Double = 18.0
)

/**
 * Complete calculated shelf result.
 */
data class ShelfResult(
    val usableWidthMm: Double,
    val usableDepthMm: Double,
    val usableHeightMm: Double,
    val shelfAreaMm2: Double,
    val totalShelfAreaMm2: Double,
    val linearShelfLengthMm: Double,
    val storageVolumeMm3: Double,
    val compartmentHeightMm: Double,
    val shelfCount: Int,
    val compartmentCount: Int = shelfCount + 1,
    val isValid: Boolean = true,
    val validationError: String? = null
) {
    val shelfAreaM2: Double get() = shelfAreaMm2 / 1_000_000.0
    val totalShelfAreaM2: Double get() = totalShelfAreaMm2 / 1_000_000.0
    val linearShelfLengthM: Double get() = linearShelfLengthMm / 1000.0
    val storageVolumeLiters: Double get() = storageVolumeMm3 / 1_000_000.0
    val storageVolumeM3: Double get() = storageVolumeMm3 / 1_000_000_000.0
    val storageVolumeCuFt: Double get() = storageVolumeMm3 / 28_316_846.592
    val compartmentVolumeMm3: Double get() = usableWidthMm * usableDepthMm * compartmentHeightMm
    val compartmentVolumeLiters: Double get() = compartmentVolumeMm3 / 1_000_000.0
}

/**
 * Detailed item fit calculation result.
 */
data class ItemFitResult(
    val fits: Boolean,
    val itemsAcross: Int,
    val itemsDeep: Int,
    val verticalLayers: Int,
    val itemsPerCompartment: Int,
    val totalCapacity: Int,
    val isRotated: Boolean,
    val effectiveItemWidthMm: Double,
    val effectiveItemDepthMm: Double,
    val clearanceWidthMm: Double,
    val clearanceDepthMm: Double,
    val clearanceHeightMm: Double,
    val statusMessage: String,
    val specificFailureReason: String? = null,
    val isLowClearance: Boolean = false,
    val enteredQuantity: Int = 1,
    val overflow: Int = 0
)

/**
 * Result for maximum shelves calculation.
 */
data class MaxShelfResult(
    val maxShelves: Int,
    val resultingCompartments: Int,
    val resultingClearanceMm: Double,
    val totalShelfAreaM2: Double,
    val totalLinearLengthM: Double,
    val isValid: Boolean,
    val message: String
)

/**
 * Custom spacing validation result.
 */
data class CustomSpacingValidation(
    val isValid: Boolean,
    val requiredHeightMm: Double,
    val availableHeightMm: Double,
    val excessHeightMm: Double,
    val errorMessage: String? = null
)

/**
 * Storage utilization calculation result.
 */
data class StorageUtilizationResult(
    val occupiedVolumeMm3: Double,
    val totalVolumeMm3: Double,
    val utilizationPercentage: Double,
    val freeVolumePercentage: Double,
    val occupiedShelfAreaMm2: Double,
    val totalShelfAreaMm2: Double,
    val freeShelfAreaMm2: Double,
    val exceedsVolume: Boolean = false
) {
    val occupiedVolumeLiters: Double get() = occupiedVolumeMm3 / 1_000_000.0
    val freeVolumeLiters: Double get() = Math.max(0.0, totalVolumeMm3 - occupiedVolumeMm3) / 1_000_000.0
    val occupiedAreaM2: Double get() = occupiedShelfAreaMm2 / 1_000_000.0
    val freeAreaM2: Double get() = Math.max(0.0, freeShelfAreaMm2) / 1_000_000.0
}

/**
 * History item for saved calculations (maximum 10).
 */
data class SavedCalculation(
    val id: String,
    val name: String,
    val timestamp: Long,
    val unit: StorageUnit,
    val result: ShelfResult,
    val items: List<StorageItem> = emptyList()
)

/**
 * User application settings.
 */
data class AppSettings(
    val themeMode: String = "System", // System, Light, Dark
    val dynamicColor: Boolean = true,
    val defaultUnit: DimensionUnit = DimensionUnit.CENTIMETERS,
    val defaultShelfThicknessMm: Double = 18.0,
    val defaultSidePanelThicknessMm: Double = 18.0,
    val defaultBackPanelThicknessMm: Double = 6.0,
    val defaultTopThicknessMm: Double = 18.0,
    val defaultBottomThicknessMm: Double = 18.0,
    val defaultMinimumClearanceMm: Double = 300.0,
    val showFrontDiagram: Boolean = true,
    val showSideDiagram: Boolean = true,
    val showTopDiagram: Boolean = true,
    val showCalculationBreakdown: Boolean = true,
    val showItemCapacity: Boolean = true,
    val showStorageUtilization: Boolean = true,
    val decimalPlaces: Int = 2,
    val liveCalculation: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val animationsEnabled: Boolean = true,
    val confirmDelete: Boolean = true,
    val restoreLastCalculation: Boolean = true
)
