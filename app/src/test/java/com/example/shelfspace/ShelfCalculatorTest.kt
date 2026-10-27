package com.example.shelfspace

import com.example.shelfspace.calculator.ShelfCalculator
import com.example.shelfspace.calculator.StorageCapacityCalculator
import com.example.shelfspace.model.ItemOrientation
import com.example.shelfspace.model.ShelfMode
import com.example.shelfspace.model.StorageItem
import com.example.shelfspace.model.StorageUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShelfCalculatorTest {

    private val standardUnit = StorageUnit(
        id = "test-1",
        name = "Standard Cabinet",
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
        shelfMode = ShelfMode.FIXED
    )

    @Test
    fun testBasicShelfUsableDimensions() {
        val usableWidth = ShelfCalculator.calculateUsableWidth(1200.0, 18.0)
        assertEquals(1164.0, usableWidth, 0.001) // 116.4 cm

        val usableDepth = ShelfCalculator.calculateUsableDepth(400.0, 6.0, true)
        assertEquals(394.0, usableDepth, 0.001) // 39.4 cm

        val usableHeight = ShelfCalculator.calculateUsableHeight(1800.0, 18.0, 18.0)
        assertEquals(1764.0, usableHeight, 0.001) // 176.4 cm

        // Without back panel
        val usableDepthNoBack = ShelfCalculator.calculateUsableDepth(400.0, 6.0, false)
        assertEquals(400.0, usableDepthNoBack, 0.001)
    }

    @Test
    fun testShelfArea() {
        // 116.4 cm * 39.4 cm = 4586.16 cm² = 0.458616 m²
        val areaMm2 = ShelfCalculator.calculateShelfArea(1164.0, 394.0)
        assertEquals(458616.0, areaMm2, 0.01)

        val areaM2 = areaMm2 / 1_000_000.0
        assertEquals(0.458616, areaM2, 0.000001)
    }

    @Test
    fun testTotalShelfArea() {
        // 4 shelves * 0.458616 m² = 1.834464 m²
        val singleShelfAreaMm2 = ShelfCalculator.calculateShelfArea(1164.0, 394.0)
        val totalAreaMm2 = ShelfCalculator.calculateTotalShelfArea(singleShelfAreaMm2, 4)
        val totalAreaM2 = totalAreaMm2 / 1_000_000.0
        assertEquals(1.834464, totalAreaM2, 0.000001)
    }

    @Test
    fun testLinearShelfLength() {
        // 116.4 cm * 4 = 465.6 cm = 4.656 m
        val linearMm = ShelfCalculator.calculateLinearShelfLength(1164.0, 4)
        assertEquals(4656.0, linearMm, 0.001)
        val linearM = linearMm / 1000.0
        assertEquals(4.656, linearM, 0.001)
    }

    @Test
    fun testVolume() {
        // 116.4 * 39.4 * 176.4 cm = 808,998.624 cm³ = 808.998624 L (~0.81 m³)
        val volumeMm3 = ShelfCalculator.calculateStorageVolume(1164.0, 394.0, 1764.0)
        val volumeLiters = volumeMm3 / 1_000_000.0
        assertEquals(808.998624, volumeLiters, 0.0001)
    }

    @Test
    fun testCompartmentHeight() {
        // Usable height: 176.4 cm (1764 mm)
        // Shelf count: 4, thickness: 1.8 cm (18 mm)
        // 176.4 - (4 * 1.8) = 169.2 cm (1692 mm)
        // Compartments: 5
        // Each: 169.2 / 5 = 33.84 cm (338.4 mm)
        val compHeightMm = ShelfCalculator.calculateCompartmentHeight(1764.0, 18.0, 4)
        assertEquals(338.4, compHeightMm, 0.001)
    }

    @Test
    fun testMaximumShelves() {
        // Usable height: 176.4 cm (1764 mm)
        // Shelf thickness: 1.8 cm (18 mm)
        // Minimum clearance: 30 cm (300 mm)
        // Inequality: shelves * 18 + (shelves + 1) * 300 <= 1764
        val maxResult = ShelfCalculator.calculateMaxShelves(
            usableHeightMm = 1764.0,
            shelfThicknessMm = 18.0,
            minimumClearanceMm = 300.0
        )
        assertTrue(maxResult.isValid)
        val n = maxResult.maxShelves
        // Verify inequality: n * 18 + (n + 1) * 300 <= 1764
        val totalOccupied = (n * 18.0) + ((n + 1) * 300.0)
        assertTrue("Total occupied $totalOccupied must be <= 1764.0", totalOccupied <= 1764.0)
        // Verify that n+1 shelves would violate the minimum clearance
        val totalOccupiedNext = ((n + 1) * 18.0) + ((n + 2) * 300.0)
        assertTrue("Next shelf $totalOccupiedNext must be > 1764.0", totalOccupiedNext > 1764.0)
    }

    @Test
    fun testItemFit() {
        // Shelf: 116.4 x 39.4 cm (1164 x 394 mm)
        // Item: 30 x 20 cm (300 x 200 mm)
        val across = StorageCapacityCalculator.calculateItemsAcross(1164.0, 300.0)
        assertEquals(3, across)

        val deep = StorageCapacityCalculator.calculateItemsDeep(394.0, 200.0)
        assertEquals(1, deep)
    }

    @Test
    fun testItemHeightWithoutAndWithStacking() {
        // Compartment: 33.84 cm (338.4 mm)
        // Item: 20 cm (200 mm)
        val layersNoStack = StorageCapacityCalculator.calculateVerticalLayers(338.4, 200.0, false)
        assertEquals(1, layersNoStack)

        val layersStack = StorageCapacityCalculator.calculateVerticalLayers(338.4, 200.0, true)
        assertEquals(1, layersStack)
    }

    @Test
    fun testStackingMultiLayer() {
        // Compartment: 60 cm (600 mm)
        // Item height: 20 cm (200 mm)
        // floor(600 / 200) = 3
        val layers = StorageCapacityCalculator.calculateVerticalLayers(600.0, 200.0, true)
        assertEquals(3, layers)
    }

    @Test
    fun testTooTallItem() {
        // Compartment: 30 cm (300 mm)
        // Item: 35 cm (350 mm)
        val layers = StorageCapacityCalculator.calculateVerticalLayers(300.0, 350.0, true)
        assertEquals(0, layers)

        val item = StorageItem("id1", "Tall Item", 200.0, 200.0, 350.0, quantity = 1, allowStacking = true)
        val fitResult = StorageCapacityCalculator.calculateItemFit(1164.0, 394.0, 300.0, item)
        assertFalse(fitResult.fits)
        assertEquals(0, fitResult.totalCapacity)
        assertEquals("✕ ITEM TOO TALL", fitResult.statusMessage)
    }

    @Test
    fun testMultipleUnitsCalculation() {
        val unitA = StorageUnit(
            id = "unit-a",
            name = "Unit A",
            widthMm = 1200.0,
            depthMm = 400.0,
            heightMm = 1800.0,
            sidePanelThicknessMm = 18.0,
            topThicknessMm = 18.0,
            bottomThicknessMm = 18.0,
            backPanelThicknessMm = 6.0,
            shelfThicknessMm = 18.0,
            shelfCount = 4
        )
        val unitB = StorageUnit(
            id = "unit-b",
            name = "Unit B",
            widthMm = 800.0,
            depthMm = 300.0,
            heightMm = 1500.0,
            sidePanelThicknessMm = 18.0,
            topThicknessMm = 18.0,
            bottomThicknessMm = 18.0,
            backPanelThicknessMm = 6.0,
            shelfThicknessMm = 18.0,
            shelfCount = 5
        )

        val resA = ShelfCalculator.calculate(unitA)
        val resB = ShelfCalculator.calculate(unitB)

        assertTrue(resA.isValid)
        assertTrue(resB.isValid)

        val totalShelfAreaM2 = resA.totalShelfAreaM2 + resB.totalShelfAreaM2
        val totalLinearLengthM = resA.linearShelfLengthM + resB.linearShelfLengthM
        val totalVolumeLiters = resA.storageVolumeLiters + resB.storageVolumeLiters

        assertEquals(1.834464 + resB.totalShelfAreaM2, totalShelfAreaM2, 0.0001)
        assertEquals(4.656 + resB.linearShelfLengthM, totalLinearLengthM, 0.0001)
        assertEquals(resA.storageVolumeLiters + resB.storageVolumeLiters, totalVolumeLiters, 0.0001)
    }

    @Test
    fun testInvalidInputs() {
        val zeroWidth = standardUnit.copy(widthMm = 0.0)
        assertFalse(ShelfCalculator.calculate(zeroWidth).isValid)

        val negativeHeight = standardUnit.copy(heightMm = -100.0)
        assertFalse(ShelfCalculator.calculate(negativeHeight).isValid)

        val negativeShelves = standardUnit.copy(shelfCount = -1)
        assertFalse(ShelfCalculator.calculate(negativeShelves).isValid)

        val zeroShelfThickness = standardUnit.copy(shelfThicknessMm = 0.0)
        assertFalse(ShelfCalculator.calculate(zeroShelfThickness).isValid)
    }

    @Test
    fun testShelfFitValidation() {
        // If shelfCount * shelfThickness > usableHeight
        // usable height = 100 mm, 10 shelves of 18 mm = 180 mm > 100 mm
        val crampedUnit = standardUnit.copy(heightMm = 136.0, shelfCount = 10, shelfThicknessMm = 18.0)
        val result = ShelfCalculator.calculate(crampedUnit)
        assertFalse(result.isValid)
        assertTrue(result.validationError?.contains("SHELVES DO NOT FIT") == true)
    }

    @Test
    fun testCustomSpacingValidation() {
        val usableHeight = 1764.0 // 176.4 cm
        val compartmentHeights = listOf(300.0, 350.0, 400.0, 300.0, 400.0) // sum = 1750 mm
        val shelfThicknesses = listOf(18.0, 18.0, 18.0, 18.0) // sum = 72 mm
        // Total required = 1750 + 72 = 1822 mm > 1764 mm
        val validation = ShelfCalculator.validateCustomSpacing(usableHeight, compartmentHeights, shelfThicknesses)
        assertFalse(validation.isValid)
        assertEquals(58.0, validation.excessHeightMm, 0.001)
        assertTrue(validation.errorMessage?.contains("SHELVES DO NOT FIT") == true)
    }

    @Test
    fun testItemOrientationAutomatic() {
        // Shelf: 100 x 50 cm (1000 x 500 mm)
        // Item: 40 x 60 cm (400 x 600 mm)
        // If unrotated: across = floor(1000/400) = 2, deep = floor(500/600) = 0 -> capacity 0
        // If rotated (600 x 400): across = floor(1000/600) = 1, deep = floor(500/400) = 1 -> capacity 1
        val item = StorageItem("item1", "Box", 400.0, 600.0, 200.0, orientation = ItemOrientation.AUTOMATIC)
        val fitResult = StorageCapacityCalculator.calculateItemFit(1000.0, 500.0, 300.0, item)
        assertTrue(fitResult.fits)
        assertTrue(fitResult.isRotated)
        assertEquals(1, fitResult.itemsPerCompartment)
    }
}
