package com.example.shelfspace.calculator

import com.example.shelfspace.model.CustomSpacingValidation
import com.example.shelfspace.model.MaxShelfResult
import com.example.shelfspace.model.ShelfResult
import com.example.shelfspace.model.StorageUnit
import kotlin.math.floor
import kotlin.math.max

/**
 * Core engine for calculating usable dimensions, shelf area, linear length,
 * storage volume, and compartment spacing.
 *
 * All internal math uses exact millimeters (Double) without premature rounding.
 */
object ShelfCalculator {

    /**
     * Calculate usable internal width:
     * usableWidth = overallWidth - 2 * sidePanelThickness
     */
    fun calculateUsableWidth(
        overallWidthMm: Double,
        sidePanelThicknessMm: Double
    ): Double {
        return overallWidthMm - (2.0 * sidePanelThicknessMm)
    }

    /**
     * Calculate usable internal depth:
     * usableDepth = overallDepth - backPanelThickness (if back panel present)
     */
    fun calculateUsableDepth(
        overallDepthMm: Double,
        backPanelThicknessMm: Double,
        hasBackPanel: Boolean
    ): Double {
        return if (hasBackPanel) {
            overallDepthMm - backPanelThicknessMm
        } else {
            overallDepthMm
        }
    }

    /**
     * Calculate usable internal height:
     * usableInternalHeight = overallHeight - topThickness - bottomThickness
     */
    fun calculateUsableHeight(
        overallHeightMm: Double,
        topThicknessMm: Double,
        bottomThicknessMm: Double
    ): Double {
        return overallHeightMm - topThicknessMm - bottomThicknessMm
    }

    /**
     * Calculate surface area of a single shelf:
     * shelfArea = usableWidth * usableDepth
     */
    fun calculateShelfArea(
        usableWidthMm: Double,
        usableDepthMm: Double
    ): Double {
        if (usableWidthMm <= 0.0 || usableDepthMm <= 0.0) return 0.0
        return usableWidthMm * usableDepthMm
    }

    /**
     * Calculate total shelf surface area:
     * totalShelfArea = shelfArea * numberOfShelves
     */
    fun calculateTotalShelfArea(
        shelfAreaMm2: Double,
        shelfCount: Int
    ): Double {
        if (shelfCount <= 0) return 0.0
        return shelfAreaMm2 * shelfCount.toDouble()
    }

    /**
     * Calculate total usable linear shelf length (or frontage):
     * linearShelfLength = usableWidth * numberOfShelves
     */
    fun calculateLinearShelfLength(
        usableWidthMm: Double,
        shelfCount: Int
    ): Double {
        if (usableWidthMm <= 0.0 || shelfCount <= 0) return 0.0
        return usableWidthMm * shelfCount.toDouble()
    }

    /**
     * Calculate total storage volume:
     * usableStorageVolume = usableWidth * usableDepth * usableHeight
     */
    fun calculateStorageVolume(
        usableWidthMm: Double,
        usableDepthMm: Double,
        usableHeightMm: Double
    ): Double {
        if (usableWidthMm <= 0.0 || usableDepthMm <= 0.0 || usableHeightMm <= 0.0) return 0.0
        return usableWidthMm * usableDepthMm * usableHeightMm
    }

    /**
     * Calculate compartment height for equally spaced shelves:
     * compartments = shelfCount + 1
     * storageHeight = usableHeight - (shelfCount * shelfThickness)
     * compartmentHeight = storageHeight / compartments
     */
    fun calculateCompartmentHeight(
        usableHeightMm: Double,
        shelfThicknessMm: Double,
        shelfCount: Int
    ): Double {
        if (shelfCount < 0) return 0.0
        val compartments = shelfCount + 1
        val totalShelfThickness = shelfCount * shelfThicknessMm
        val availableVerticalSpace = usableHeightMm - totalShelfThickness
        return if (availableVerticalSpace > 0.0) availableVerticalSpace / compartments.toDouble() else 0.0
    }

    /**
     * Calculate the maximum practical number of shelves that preserve minimum clearance:
     * inequality: shelfCount * shelfThickness + (shelfCount + 1) * minimumClearance <= usableHeight
     * maxShelves = floor((usableHeight - minimumClearance) / (shelfThickness + minimumClearance))
     */
    fun calculateMaxShelves(
        usableHeightMm: Double,
        shelfThicknessMm: Double,
        minimumClearanceMm: Double,
        usableWidthMm: Double = 0.0,
        usableDepthMm: Double = 0.0
    ): MaxShelfResult {
        if (usableHeightMm <= 0.0 || minimumClearanceMm <= 0.0 || shelfThicknessMm <= 0.0) {
            return MaxShelfResult(
                maxShelves = 0,
                resultingCompartments = 0,
                resultingClearanceMm = 0.0,
                totalShelfAreaM2 = 0.0,
                totalLinearLengthM = 0.0,
                isValid = false,
                message = "Invalid dimensions for shelf calculation"
            )
        }

        if (usableHeightMm < minimumClearanceMm) {
            return MaxShelfResult(
                maxShelves = 0,
                resultingCompartments = 1,
                resultingClearanceMm = usableHeightMm,
                totalShelfAreaM2 = 0.0,
                totalLinearLengthM = 0.0,
                isValid = false,
                message = "Usable height is less than minimum clearance"
            )
        }

        val numerator = usableHeightMm - minimumClearanceMm
        val denominator = shelfThicknessMm + minimumClearanceMm
        var count = floor(numerator / denominator).toInt()
        if (count < 0) count = 0

        // Validate inequality strictly:
        while (count > 0 && (count * shelfThicknessMm + (count + 1) * minimumClearanceMm > usableHeightMm)) {
            count--
        }

        val compartments = count + 1
        val resultingClearance = if (compartments > 0) {
            (usableHeightMm - (count * shelfThicknessMm)) / compartments.toDouble()
        } else {
            0.0
        }

        val shelfAreaMm2 = calculateShelfArea(usableWidthMm, usableDepthMm)
        val totalAreaM2 = (shelfAreaMm2 * count) / 1_000_000.0
        val totalLinearM = (usableWidthMm * count) / 1000.0

        return MaxShelfResult(
            maxShelves = count,
            resultingCompartments = compartments,
            resultingClearanceMm = resultingClearance,
            totalShelfAreaM2 = totalAreaM2,
            totalLinearLengthM = totalLinearM,
            isValid = count >= 0,
            message = "Max $count shelves with approx. ${String.format("%.1f", resultingClearance / 10.0)} cm clearance"
        )
    }

    /**
     * Validate custom shelf spacing against usable height:
     * sum(compartmentHeights) + sum(shelfThicknesses) <= usableHeight
     */
    fun validateCustomSpacing(
        usableHeightMm: Double,
        compartmentHeightsMm: List<Double>,
        shelfThicknessesMm: List<Double>
    ): CustomSpacingValidation {
        val totalCompartmentHeight = compartmentHeightsMm.sum()
        val totalShelfThickness = shelfThicknessesMm.sum()
        val requiredHeightMm = totalCompartmentHeight + totalShelfThickness
        val excessMm = requiredHeightMm - usableHeightMm

        val isValid = excessMm <= 0.001 // tolerance for floating point
        val errorMessage = if (!isValid) {
            "⚠ SHELVES DO NOT FIT\nRequired: ${String.format("%.1f", requiredHeightMm / 10.0)} cm\nAvailable: ${String.format("%.1f", usableHeightMm / 10.0)} cm\nExcess: ${String.format("%.1f", excessMm / 10.0)} cm"
        } else null

        return CustomSpacingValidation(
            isValid = isValid,
            requiredHeightMm = requiredHeightMm,
            availableHeightMm = usableHeightMm,
            excessHeightMm = max(0.0, excessMm),
            errorMessage = errorMessage
        )
    }

    /**
     * Complete calculation for a StorageUnit.
     */
    fun calculate(unit: StorageUnit): ShelfResult {
        // Validate basic inputs
        if (unit.widthMm <= 0.0) {
            return ShelfResult(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, unit.shelfCount, isValid = false, validationError = "Width must be greater than zero")
        }
        if (unit.depthMm <= 0.0) {
            return ShelfResult(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, unit.shelfCount, isValid = false, validationError = "Depth must be greater than zero")
        }
        if (unit.heightMm <= 0.0) {
            return ShelfResult(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, unit.shelfCount, isValid = false, validationError = "Height must be greater than zero")
        }
        if (unit.shelfCount < 0) {
            return ShelfResult(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0, isValid = false, validationError = "Shelf count cannot be negative")
        }
        if (unit.shelfThicknessMm <= 0.0) {
            return ShelfResult(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, unit.shelfCount, isValid = false, validationError = "Shelf thickness must be greater than zero")
        }

        val usableWidth = calculateUsableWidth(unit.widthMm, unit.sidePanelThicknessMm)
        if (usableWidth <= 0.0) {
            return ShelfResult(usableWidth, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, unit.shelfCount, isValid = false, validationError = "Side panels exceed overall width")
        }

        val usableDepth = calculateUsableDepth(unit.depthMm, unit.backPanelThicknessMm, unit.hasBackPanel)
        if (usableDepth <= 0.0) {
            return ShelfResult(usableWidth, usableDepth, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, unit.shelfCount, isValid = false, validationError = "Back panel exceeds overall depth")
        }

        val usableHeight = calculateUsableHeight(unit.heightMm, unit.topThicknessMm, unit.bottomThicknessMm)
        if (usableHeight <= 0.0) {
            return ShelfResult(usableWidth, usableDepth, usableHeight, 0.0, 0.0, 0.0, 0.0, 0.0, unit.shelfCount, isValid = false, validationError = "Top and bottom panels exceed overall height")
        }

        val totalShelfThickness = unit.shelfCount * unit.shelfThicknessMm
        if (totalShelfThickness >= usableHeight) {
            return ShelfResult(
                usableWidthMm = usableWidth,
                usableDepthMm = usableDepth,
                usableHeightMm = usableHeight,
                shelfAreaMm2 = calculateShelfArea(usableWidth, usableDepth),
                totalShelfAreaMm2 = 0.0,
                linearShelfLengthMm = 0.0,
                storageVolumeMm3 = calculateStorageVolume(usableWidth, usableDepth, usableHeight),
                compartmentHeightMm = 0.0,
                shelfCount = unit.shelfCount,
                isValid = false,
                validationError = "⚠ SHELVES DO NOT FIT\nShelf thickness exceeds available vertical space."
            )
        }

        val shelfAreaMm2 = calculateShelfArea(usableWidth, usableDepth)
        val totalShelfAreaMm2 = calculateTotalShelfArea(shelfAreaMm2, unit.shelfCount)
        val linearShelfLengthMm = calculateLinearShelfLength(usableWidth, unit.shelfCount)
        val storageVolumeMm3 = calculateStorageVolume(usableWidth, usableDepth, usableHeight)
        val compartmentHeightMm = calculateCompartmentHeight(usableHeight, unit.shelfThicknessMm, unit.shelfCount)

        return ShelfResult(
            usableWidthMm = usableWidth,
            usableDepthMm = usableDepth,
            usableHeightMm = usableHeight,
            shelfAreaMm2 = shelfAreaMm2,
            totalShelfAreaMm2 = totalShelfAreaMm2,
            linearShelfLengthMm = linearShelfLengthMm,
            storageVolumeMm3 = storageVolumeMm3,
            compartmentHeightMm = compartmentHeightMm,
            shelfCount = unit.shelfCount,
            compartmentCount = unit.shelfCount + 1,
            isValid = true,
            validationError = null
        )
    }
}
