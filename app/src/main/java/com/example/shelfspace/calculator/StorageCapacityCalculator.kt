package com.example.shelfspace.calculator

import com.example.shelfspace.model.ItemFitResult
import com.example.shelfspace.model.ItemOrientation
import com.example.shelfspace.model.StorageItem
import com.example.shelfspace.model.StorageUtilizationResult
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

/**
 * Engine for calculating item capacity, orientation optimization,
 * vertical stacking, clearances, and storage volume utilization.
 */
object StorageCapacityCalculator {

    /**
     * Calculate number of items fitting across shelf width:
     * floor(shelfWidth / itemWidth)
     */
    fun calculateItemsAcross(
        shelfWidthMm: Double,
        itemWidthMm: Double
    ): Int {
        if (shelfWidthMm <= 0.0 || itemWidthMm <= 0.0) return 0
        return floor(shelfWidthMm / itemWidthMm).toInt()
    }

    /**
     * Calculate number of items fitting across shelf depth:
     * floor(shelfDepth / itemDepth)
     */
    fun calculateItemsDeep(
        shelfDepthMm: Double,
        itemDepthMm: Double
    ): Int {
        if (shelfDepthMm <= 0.0 || itemDepthMm <= 0.0) return 0
        return floor(shelfDepthMm / itemDepthMm).toInt()
    }

    /**
     * Calculate vertical stacking layers in compartment:
     * If stacking disabled: 1 (if fits) else 0
     * If stacking enabled: floor(compartmentHeight / itemHeight)
     */
    fun calculateVerticalLayers(
        compartmentHeightMm: Double,
        itemHeightMm: Double,
        stackingEnabled: Boolean
    ): Int {
        if (compartmentHeightMm <= 0.0 || itemHeightMm <= 0.0) return 0
        if (itemHeightMm > compartmentHeightMm) return 0
        return if (stackingEnabled) {
            floor(compartmentHeightMm / itemHeightMm).toInt()
        } else {
            1
        }
    }

    /**
     * Calculate item fit, capacity, and clearance in a shelf compartment.
     */
    fun calculateItemFit(
        shelfWidthMm: Double,
        shelfDepthMm: Double,
        compartmentHeightMm: Double,
        item: StorageItem,
        totalCompartments: Int = 1
    ): ItemFitResult {
        // Validate inputs
        if (shelfWidthMm <= 0.0 || shelfDepthMm <= 0.0 || compartmentHeightMm <= 0.0) {
            return ItemFitResult(
                fits = false,
                itemsAcross = 0,
                itemsDeep = 0,
                verticalLayers = 0,
                itemsPerCompartment = 0,
                totalCapacity = 0,
                isRotated = false,
                effectiveItemWidthMm = item.widthMm,
                effectiveItemDepthMm = item.depthMm,
                clearanceWidthMm = 0.0,
                clearanceDepthMm = 0.0,
                clearanceHeightMm = 0.0,
                statusMessage = "✕ Invalid shelf dimensions",
                specificFailureReason = "Shelf dimensions are zero or invalid",
                enteredQuantity = item.quantity,
                overflow = item.quantity
            )
        }

        if (item.widthMm <= 0.0 || item.depthMm <= 0.0 || item.heightMm <= 0.0) {
            return ItemFitResult(
                fits = false,
                itemsAcross = 0,
                itemsDeep = 0,
                verticalLayers = 0,
                itemsPerCompartment = 0,
                totalCapacity = 0,
                isRotated = false,
                effectiveItemWidthMm = item.widthMm,
                effectiveItemDepthMm = item.depthMm,
                clearanceWidthMm = 0.0,
                clearanceDepthMm = 0.0,
                clearanceHeightMm = 0.0,
                statusMessage = "✕ Invalid item dimensions",
                specificFailureReason = "Item dimensions must be greater than zero",
                enteredQuantity = item.quantity,
                overflow = item.quantity
            )
        }

        // Check height first (height is NEVER rotated)
        if (item.heightMm > compartmentHeightMm) {
            return ItemFitResult(
                fits = false,
                itemsAcross = 0,
                itemsDeep = 0,
                verticalLayers = 0,
                itemsPerCompartment = 0,
                totalCapacity = 0,
                isRotated = false,
                effectiveItemWidthMm = item.widthMm,
                effectiveItemDepthMm = item.depthMm,
                clearanceWidthMm = shelfWidthMm,
                clearanceDepthMm = shelfDepthMm,
                clearanceHeightMm = compartmentHeightMm - item.heightMm,
                statusMessage = "✕ ITEM TOO TALL",
                specificFailureReason = "Item height (${formatMm(item.heightMm)}) exceeds compartment clearance (${formatMm(compartmentHeightMm)})",
                enteredQuantity = item.quantity,
                overflow = item.quantity
            )
        }

        // Evaluate orientation
        val allowRotation = item.orientation == ItemOrientation.AUTOMATIC ||
                item.orientation == ItemOrientation.ROTATION_ALLOWED

        val acrossA = calculateItemsAcross(shelfWidthMm, item.widthMm)
        val deepA = calculateItemsDeep(shelfDepthMm, item.depthMm)
        val footprintA = acrossA * deepA

        var acrossB = 0
        var deepB = 0
        var footprintB = 0

        if (allowRotation) {
            acrossB = calculateItemsAcross(shelfWidthMm, item.depthMm)
            deepB = calculateItemsDeep(shelfDepthMm, item.widthMm)
            footprintB = acrossB * deepB
        }

        val useRotated = allowRotation && (footprintB > footprintA)
        val finalAcross = if (useRotated) acrossB else acrossA
        val finalDeep = if (useRotated) deepB else deepA
        val effectiveWidth = if (useRotated) item.depthMm else item.widthMm
        val effectiveDepth = if (useRotated) item.widthMm else item.depthMm

        val verticalLayers = calculateVerticalLayers(compartmentHeightMm, item.heightMm, item.allowStacking)
        val itemsPerCompartment = finalAcross * finalDeep * verticalLayers
        val totalCapacity = itemsPerCompartment * max(1, totalCompartments)

        val fits = itemsPerCompartment > 0

        val clearanceWidth = shelfWidthMm - (finalAcross * effectiveWidth)
        val clearanceDepth = shelfDepthMm - (finalDeep * effectiveDepth)
        val clearanceHeight = compartmentHeightMm - (verticalLayers * item.heightMm)

        val isLowClearance = fits && (clearanceHeight in 0.01..20.0) // less than 2 cm

        val overflow = max(0, item.quantity - totalCapacity)

        val statusMessage: String
        val specificFailureReason: String?

        if (!fits) {
            if (effectiveWidth > shelfWidthMm && effectiveDepth > shelfDepthMm) {
                statusMessage = "✕ NO ITEMS FIT"
                specificFailureReason = "Item width and depth exceed shelf dimensions"
            } else if (effectiveWidth > shelfWidthMm) {
                statusMessage = "✕ Item is too wide"
                specificFailureReason = "Item width (${formatMm(effectiveWidth)}) exceeds shelf width (${formatMm(shelfWidthMm)})"
            } else if (effectiveDepth > shelfDepthMm) {
                statusMessage = "✕ Item is too deep"
                specificFailureReason = "Item depth (${formatMm(effectiveDepth)}) exceeds shelf depth (${formatMm(shelfDepthMm)})"
            } else {
                statusMessage = "✕ ITEM DOES NOT FIT"
                specificFailureReason = "Item cannot fit on shelf"
            }
        } else {
            statusMessage = "✓ ITEM FITS"
            specificFailureReason = null
        }

        return ItemFitResult(
            fits = fits,
            itemsAcross = finalAcross,
            itemsDeep = finalDeep,
            verticalLayers = verticalLayers,
            itemsPerCompartment = itemsPerCompartment,
            totalCapacity = totalCapacity,
            isRotated = useRotated,
            effectiveItemWidthMm = effectiveWidth,
            effectiveItemDepthMm = effectiveDepth,
            clearanceWidthMm = clearanceWidth,
            clearanceDepthMm = clearanceDepth,
            clearanceHeightMm = clearanceHeight,
            statusMessage = statusMessage,
            specificFailureReason = specificFailureReason,
            isLowClearance = isLowClearance,
            enteredQuantity = item.quantity,
            overflow = overflow
        )
    }

    /**
     * Calculate storage utilization across multiple items.
     * Note: Utilization is an informational metric comparing liquid/bulk volumes.
     */
    fun calculateStorageUtilization(
        usableStorageVolumeMm3: Double,
        totalShelfAreaMm2: Double,
        items: List<StorageItem>
    ): StorageUtilizationResult {
        if (usableStorageVolumeMm3 <= 0.0) {
            return StorageUtilizationResult(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false)
        }

        var totalOccupiedVolume = 0.0
        var totalOccupiedArea = 0.0

        for (item in items) {
            if (item.quantity > 0 && item.widthMm > 0 && item.depthMm > 0 && item.heightMm > 0) {
                val itemVolume = item.widthMm * item.depthMm * item.heightMm
                totalOccupiedVolume += itemVolume * item.quantity.toDouble()

                val itemFootprint = item.widthMm * item.depthMm
                totalOccupiedArea += itemFootprint * item.quantity.toDouble()
            }
        }

        val rawUtilization = (totalOccupiedVolume / usableStorageVolumeMm3) * 100.0
        val clampedUtilization = min(100.0, max(0.0, rawUtilization))
        val freePercentage = max(0.0, 100.0 - rawUtilization)
        val exceedsVolume = totalOccupiedVolume > usableStorageVolumeMm3
        val freeArea = max(0.0, totalShelfAreaMm2 - totalOccupiedArea)

        return StorageUtilizationResult(
            occupiedVolumeMm3 = totalOccupiedVolume,
            totalVolumeMm3 = usableStorageVolumeMm3,
            utilizationPercentage = clampedUtilization,
            freeVolumePercentage = freePercentage,
            occupiedShelfAreaMm2 = totalOccupiedArea,
            totalShelfAreaMm2 = totalShelfAreaMm2,
            freeShelfAreaMm2 = freeArea,
            exceedsVolume = exceedsVolume
        )
    }

    private fun formatMm(mm: Double): String {
        return "${String.format("%.1f", mm / 10.0)} cm"
    }
}
