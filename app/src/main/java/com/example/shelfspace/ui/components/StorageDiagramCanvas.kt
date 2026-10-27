package com.example.shelfspace.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shelfspace.model.DimensionUnit
import com.example.shelfspace.model.ShelfResult
import com.example.shelfspace.model.StorageUnit
import java.util.Locale
import kotlin.math.min

enum class DiagramViewMode {
    FRONT,
    SIDE,
    TOP
}

/**
 * Professional architectural storage diagram rendered with Compose Canvas.
 * Strictly maintains exact physical proportions (scale = min(availW/objW, availH/objH)).
 */
@Composable
fun StorageDiagramCanvas(
    unit: StorageUnit,
    result: ShelfResult,
    viewMode: DiagramViewMode = DiagramViewMode.FRONT,
    unitSystem: DimensionUnit = DimensionUnit.CENTIMETERS,
    decimals: Int = 1,
    modifier: Modifier = Modifier,
    onShelfSelected: ((Int) -> Unit)? = null
) {
    val textMeasurer = rememberTextMeasurer()
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val outlineColor = MaterialTheme.colorScheme.outline
    val surfaceColor = MaterialTheme.colorScheme.surface
    val woodColor = Color(0xFFC4824A)
    val highlightColor = MaterialTheme.colorScheme.secondary

    var selectedShelfIndex by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(surfaceColor, RoundedCornerShape(12.dp))
                .border(1.dp, outlineColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .matchParentSize()
                    .pointerInput(unit, result, viewMode) {
                        detectTapGestures { tapOffset ->
                            if (viewMode == DiagramViewMode.FRONT && result.shelfCount > 0) {
                                // Hit detection for shelves
                                val shelfTapped = detectShelfTap(
                                    tapOffset,
                                    size.width.toFloat(),
                                    size.height.toFloat(),
                                    unit,
                                    result
                                )
                                selectedShelfIndex = shelfTapped
                                if (shelfTapped != null) {
                                    onShelfSelected?.invoke(shelfTapped)
                                }
                            }
                        }
                    }
            ) {
                when (viewMode) {
                    DiagramViewMode.FRONT -> drawFrontElevation(
                        unit = unit,
                        result = result,
                        unitSystem = unitSystem,
                        decimals = decimals,
                        primaryColor = primaryColor,
                        onSurfaceColor = onSurfaceColor,
                        outlineColor = outlineColor,
                        woodColor = woodColor,
                        highlightColor = highlightColor,
                        selectedShelf = selectedShelfIndex,
                        textMeasurer = textMeasurer
                    )
                    DiagramViewMode.SIDE -> drawSideView(
                        unit = unit,
                        result = result,
                        unitSystem = unitSystem,
                        decimals = decimals,
                        primaryColor = primaryColor,
                        onSurfaceColor = onSurfaceColor,
                        outlineColor = outlineColor,
                        woodColor = woodColor,
                        textMeasurer = textMeasurer
                    )
                    DiagramViewMode.TOP -> drawTopView(
                        unit = unit,
                        result = result,
                        unitSystem = unitSystem,
                        decimals = decimals,
                        primaryColor = primaryColor,
                        onSurfaceColor = onSurfaceColor,
                        outlineColor = outlineColor,
                        woodColor = woodColor,
                        textMeasurer = textMeasurer
                    )
                }
            }
        }

        // Shelf inspection card when tapped
        AnimatedVisibility(visible = selectedShelfIndex != null && viewMode == DiagramViewMode.FRONT) {
            val shelfNum = selectedShelfIndex?.plus(1) ?: 1
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Shelf $shelfNum Inspection",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Width: ${unitSystem.format(result.usableWidthMm, decimals)} | Depth: ${unitSystem.format(result.usableDepthMm, decimals)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = String.format(Locale.US, "%.${decimals}f m²", result.shelfAreaM2),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Clearance: ${unitSystem.format(result.compartmentHeightMm, decimals)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawFrontElevation(
    unit: StorageUnit,
    result: ShelfResult,
    unitSystem: DimensionUnit,
    decimals: Int,
    primaryColor: Color,
    onSurfaceColor: Color,
    outlineColor: Color,
    woodColor: Color,
    highlightColor: Color,
    selectedShelf: Int?,
    textMeasurer: TextMeasurer
) {
    if (unit.widthMm <= 0 || unit.heightMm <= 0) return

    val marginHoriz = 50f
    val marginVert = 35f
    val availW = size.width - (marginHoriz * 2)
    val availH = size.height - (marginVert * 2)

    val scale = min(availW / unit.widthMm.toFloat(), availH / unit.heightMm.toFloat())

    val cabW = (unit.widthMm * scale).toFloat()
    val cabH = (unit.heightMm * scale).toFloat()
    val cabX = marginHoriz + (availW - cabW) / 2f
    val cabY = marginVert + (availH - cabH) / 2f

    val sideW = (unit.sidePanelThicknessMm * scale).toFloat()
    val topH = (unit.topThicknessMm * scale).toFloat()
    val botH = (unit.bottomThicknessMm * scale).toFloat()
    val shelfH = (unit.shelfThicknessMm * scale).toFloat().coerceAtLeast(2f)

    // Draw cabinet background interior
    drawRect(
        color = outlineColor.copy(alpha = 0.1f),
        topLeft = Offset(cabX + sideW, cabY + topH),
        size = Size(cabW - (sideW * 2), cabH - topH - botH)
    )

    // Draw cabinet outer frame panels
    // Left side panel
    drawRect(color = woodColor.copy(alpha = 0.85f), topLeft = Offset(cabX, cabY), size = Size(sideW, cabH))
    drawRect(color = onSurfaceColor, topLeft = Offset(cabX, cabY), size = Size(sideW, cabH), style = Stroke(1.5f))

    // Right side panel
    drawRect(color = woodColor.copy(alpha = 0.85f), topLeft = Offset(cabX + cabW - sideW, cabY), size = Size(sideW, cabH))
    drawRect(color = onSurfaceColor, topLeft = Offset(cabX + cabW - sideW, cabY), size = Size(sideW, cabH), style = Stroke(1.5f))

    // Top panel
    drawRect(color = woodColor.copy(alpha = 0.85f), topLeft = Offset(cabX + sideW, cabY), size = Size(cabW - (sideW * 2), topH))
    drawRect(color = onSurfaceColor, topLeft = Offset(cabX + sideW, cabY), size = Size(cabW - (sideW * 2), topH), style = Stroke(1.5f))

    // Bottom panel
    drawRect(color = woodColor.copy(alpha = 0.85f), topLeft = Offset(cabX + sideW, cabY + cabH - botH), size = Size(cabW - (sideW * 2), botH))
    drawRect(color = onSurfaceColor, topLeft = Offset(cabX + sideW, cabY + cabH - botH), size = Size(cabW - (sideW * 2), botH), style = Stroke(1.5f))

    // Draw shelves
    val internalH = cabH - topH - botH
    val shelfCount = unit.shelfCount
    if (shelfCount > 0 && result.isValid) {
        val compCount = shelfCount + 1
        val compH = (internalH - (shelfCount * shelfH)) / compCount

        var currentY = cabY + topH
        for (i in 0 until shelfCount) {
            currentY += compH
            val isSelected = selectedShelf == i
            val shelfColor = if (isSelected) highlightColor else woodColor

            // Shelf board
            drawRect(
                color = shelfColor,
                topLeft = Offset(cabX + sideW, currentY),
                size = Size(cabW - (sideW * 2), shelfH)
            )
            drawRect(
                color = if (isSelected) highlightColor else onSurfaceColor,
                topLeft = Offset(cabX + sideW, currentY),
                size = Size(cabW - (sideW * 2), shelfH),
                style = Stroke(if (isSelected) 2.5f else 1.2f)
            )

            // Shelf label (e.g. S1, S2, ...)
            val labelText = "S${i + 1}"
            val textLayout = textMeasurer.measure(
                text = labelText,
                style = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Bold, color = primaryColor, fontFamily = FontFamily.Monospace)
            )
            drawText(
                textLayoutResult = textLayout,
                topLeft = Offset(cabX + sideW + 4f, currentY - 14f)
            )

            currentY += shelfH
        }
    }

    // Dimension Arrow: Width (Top)
    drawDimensionArrowHoriz(
        startX = cabX,
        endX = cabX + cabW,
        y = cabY - 14f,
        text = unitSystem.format(unit.widthMm, decimals),
        color = primaryColor,
        textMeasurer = textMeasurer
    )

    // Dimension Arrow: Height (Right)
    drawDimensionArrowVert(
        startY = cabY,
        endY = cabY + cabH,
        x = cabX + cabW + 16f,
        text = unitSystem.format(unit.heightMm, decimals),
        color = primaryColor,
        textMeasurer = textMeasurer
    )

    // Usable Internal Width Arrow (Inside Bottom)
    if (cabW > 60f) {
        drawDimensionArrowHoriz(
            startX = cabX + sideW,
            endX = cabX + cabW - sideW,
            y = cabY + cabH + 16f,
            text = "Usable: ${unitSystem.format(result.usableWidthMm, decimals)}",
            color = onSurfaceColor.copy(alpha = 0.7f),
            textMeasurer = textMeasurer,
            fontSize = 9
        )
    }
}

private fun DrawScope.drawSideView(
    unit: StorageUnit,
    result: ShelfResult,
    unitSystem: DimensionUnit,
    decimals: Int,
    primaryColor: Color,
    onSurfaceColor: Color,
    outlineColor: Color,
    woodColor: Color,
    textMeasurer: TextMeasurer
) {
    if (unit.depthMm <= 0 || unit.heightMm <= 0) return

    val marginHoriz = 50f
    val marginVert = 35f
    val availW = size.width - (marginHoriz * 2)
    val availH = size.height - (marginVert * 2)

    val scale = min(availW / unit.depthMm.toFloat(), availH / unit.heightMm.toFloat())

    val cabD = (unit.depthMm * scale).toFloat()
    val cabH = (unit.heightMm * scale).toFloat()
    val cabX = marginHoriz + (availW - cabD) / 2f
    val cabY = marginVert + (availH - cabH) / 2f

    val backW = if (unit.hasBackPanel) (unit.backPanelThicknessMm * scale).toFloat().coerceAtLeast(3f) else 0f
    val topH = (unit.topThicknessMm * scale).toFloat()
    val botH = (unit.bottomThicknessMm * scale).toFloat()
    val shelfH = (unit.shelfThicknessMm * scale).toFloat().coerceAtLeast(2f)

    // Draw cabinet side profile
    drawRect(
        color = outlineColor.copy(alpha = 0.1f),
        topLeft = Offset(cabX + backW, cabY + topH),
        size = Size(cabD - backW, cabH - topH - botH)
    )

    // Back panel
    if (unit.hasBackPanel) {
        drawRect(color = woodColor.copy(alpha = 0.6f), topLeft = Offset(cabX, cabY), size = Size(backW, cabH))
        drawRect(color = onSurfaceColor, topLeft = Offset(cabX, cabY), size = Size(backW, cabH), style = Stroke(1.5f))
    }

    // Top panel
    drawRect(color = woodColor.copy(alpha = 0.85f), topLeft = Offset(cabX, cabY), size = Size(cabD, topH))
    drawRect(color = onSurfaceColor, topLeft = Offset(cabX, cabY), size = Size(cabD, topH), style = Stroke(1.5f))

    // Bottom panel
    drawRect(color = woodColor.copy(alpha = 0.85f), topLeft = Offset(cabX, cabY + cabH - botH), size = Size(cabD, botH))
    drawRect(color = onSurfaceColor, topLeft = Offset(cabX, cabY + cabH - botH), size = Size(cabD, botH), style = Stroke(1.5f))

    // Front edge indicator
    drawLine(
        color = onSurfaceColor.copy(alpha = 0.5f),
        start = Offset(cabX + cabD, cabY + topH),
        end = Offset(cabX + cabD, cabY + cabH - botH),
        strokeWidth = 1.5f
    )

    // Shelves seen from side
    val internalH = cabH - topH - botH
    val shelfCount = unit.shelfCount
    if (shelfCount > 0 && result.isValid) {
        val compCount = shelfCount + 1
        val compH = (internalH - (shelfCount * shelfH)) / compCount
        var currentY = cabY + topH
        for (i in 0 until shelfCount) {
            currentY += compH
            drawRect(
                color = woodColor,
                topLeft = Offset(cabX + backW, currentY),
                size = Size(cabD - backW, shelfH)
            )
            drawRect(
                color = onSurfaceColor,
                topLeft = Offset(cabX + backW, currentY),
                size = Size(cabD - backW, shelfH),
                style = Stroke(1.2f)
            )
            currentY += shelfH
        }
    }

    // Depth Arrow (Top)
    drawDimensionArrowHoriz(
        startX = cabX,
        endX = cabX + cabD,
        y = cabY - 14f,
        text = unitSystem.format(unit.depthMm, decimals),
        color = primaryColor,
        textMeasurer = textMeasurer
    )

    // Usable Depth Arrow (Bottom)
    drawDimensionArrowHoriz(
        startX = cabX + backW,
        endX = cabX + cabD,
        y = cabY + cabH + 16f,
        text = "Usable: ${unitSystem.format(result.usableDepthMm, decimals)}",
        color = onSurfaceColor.copy(alpha = 0.7f),
        textMeasurer = textMeasurer,
        fontSize = 9
    )
}

private fun DrawScope.drawTopView(
    unit: StorageUnit,
    result: ShelfResult,
    unitSystem: DimensionUnit,
    decimals: Int,
    primaryColor: Color,
    onSurfaceColor: Color,
    outlineColor: Color,
    woodColor: Color,
    textMeasurer: TextMeasurer
) {
    if (unit.widthMm <= 0 || unit.depthMm <= 0) return

    val marginHoriz = 50f
    val marginVert = 40f
    val availW = size.width - (marginHoriz * 2)
    val availH = size.height - (marginVert * 2)

    val scale = min(availW / unit.widthMm.toFloat(), availH / unit.depthMm.toFloat())

    val cabW = (unit.widthMm * scale).toFloat()
    val cabD = (unit.depthMm * scale).toFloat()
    val cabX = marginHoriz + (availW - cabW) / 2f
    val cabY = marginVert + (availH - cabD) / 2f

    val sideW = (unit.sidePanelThicknessMm * scale).toFloat()
    val backW = if (unit.hasBackPanel) (unit.backPanelThicknessMm * scale).toFloat().coerceAtLeast(3f) else 0f

    // Outer Cabinet Top outline
    drawRect(color = outlineColor.copy(alpha = 0.15f), topLeft = Offset(cabX, cabY), size = Size(cabW, cabD))
    drawRect(color = onSurfaceColor, topLeft = Offset(cabX, cabY), size = Size(cabW, cabD), style = Stroke(1.5f))

    // Usable shelf surface area
    val shelfX = cabX + sideW
    val shelfY = cabY + backW
    val shelfW = cabW - (sideW * 2)
    val shelfD = cabD - backW

    drawRect(
        color = woodColor.copy(alpha = 0.7f),
        topLeft = Offset(shelfX, shelfY),
        size = Size(shelfW, shelfD)
    )
    drawRect(
        color = primaryColor,
        topLeft = Offset(shelfX, shelfY),
        size = Size(shelfW, shelfD),
        style = Stroke(2f)
    )

    // Center text: Shelf Area
    val areaText = "${unitSystem.format(result.usableWidthMm, decimals)} × ${unitSystem.format(result.usableDepthMm, decimals)}\nArea: ${String.format(Locale.US, "%.${decimals}f m²", result.shelfAreaM2)}"
    val textLayout = textMeasurer.measure(
        text = areaText,
        style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = primaryColor, fontFamily = FontFamily.SansSerif)
    )
    drawText(
        textLayoutResult = textLayout,
        topLeft = Offset(shelfX + (shelfW - textLayout.size.width) / 2f, shelfY + (shelfD - textLayout.size.height) / 2f)
    )

    // Width arrow
    drawDimensionArrowHoriz(
        startX = cabX,
        endX = cabX + cabW,
        y = cabY - 14f,
        text = unitSystem.format(unit.widthMm, decimals),
        color = primaryColor,
        textMeasurer = textMeasurer
    )

    // Depth arrow
    drawDimensionArrowVert(
        startY = cabY,
        endY = cabY + cabD,
        x = cabX + cabW + 14f,
        text = unitSystem.format(unit.depthMm, decimals),
        color = primaryColor,
        textMeasurer = textMeasurer
    )
}

private fun DrawScope.drawDimensionArrowHoriz(
    startX: Float,
    endX: Float,
    y: Float,
    text: String,
    color: Color,
    textMeasurer: TextMeasurer,
    fontSize: Int = 10
) {
    if (endX <= startX) return
    drawLine(color = color, start = Offset(startX, y), end = Offset(endX, y), strokeWidth = 1.2f)

    // Arrow ticks
    drawLine(color = color, start = Offset(startX, y - 4f), end = Offset(startX, y + 4f), strokeWidth = 1.2f)
    drawLine(color = color, start = Offset(endX, y - 4f), end = Offset(endX, y + 4f), strokeWidth = 1.2f)

    // Arrowheads
    val arrowSize = 4f
    val pathLeft = Path().apply {
        moveTo(startX, y)
        lineTo(startX + arrowSize, y - 2.5f)
        lineTo(startX + arrowSize, y + 2.5f)
        close()
    }
    drawPath(pathLeft, color)

    val pathRight = Path().apply {
        moveTo(endX, y)
        lineTo(endX - arrowSize, y - 2.5f)
        lineTo(endX - arrowSize, y + 2.5f)
        close()
    }
    drawPath(pathRight, color)

    // Text centered above arrow
    val textLayout = textMeasurer.measure(
        text = text,
        style = TextStyle(fontSize = fontSize.sp, fontWeight = FontWeight.Medium, color = color)
    )
    drawText(
        textLayoutResult = textLayout,
        topLeft = Offset(startX + (endX - startX - textLayout.size.width) / 2f, y - textLayout.size.height - 2f)
    )
}

private fun DrawScope.drawDimensionArrowVert(
    startY: Float,
    endY: Float,
    x: Float,
    text: String,
    color: Color,
    textMeasurer: TextMeasurer,
    fontSize: Int = 10
) {
    if (endY <= startY) return
    drawLine(color = color, start = Offset(x, startY), end = Offset(x, endY), strokeWidth = 1.2f)

    // Arrow ticks
    drawLine(color = color, start = Offset(x - 4f, startY), end = Offset(x + 4f, startY), strokeWidth = 1.2f)
    drawLine(color = color, start = Offset(x - 4f, endY), end = Offset(x + 4f, endY), strokeWidth = 1.2f)

    // Arrowheads
    val arrowSize = 4f
    val pathTop = Path().apply {
        moveTo(x, startY)
        lineTo(x - 2.5f, startY + arrowSize)
        lineTo(x + 2.5f, startY + arrowSize)
        close()
    }
    drawPath(pathTop, color)

    val pathBottom = Path().apply {
        moveTo(x, endY)
        lineTo(x - 2.5f, endY - arrowSize)
        lineTo(x + 2.5f, endY - arrowSize)
        close()
    }
    drawPath(pathBottom, color)

    // Text right of arrow
    val textLayout = textMeasurer.measure(
        text = text,
        style = TextStyle(fontSize = fontSize.sp, fontWeight = FontWeight.Medium, color = color)
    )
    drawText(
        textLayoutResult = textLayout,
        topLeft = Offset(x + 4f, startY + (endY - startY - textLayout.size.height) / 2f)
    )
}

private fun detectShelfTap(
    tap: Offset,
    canvasW: Float,
    canvasH: Float,
    unit: StorageUnit,
    result: ShelfResult
): Int? {
    if (unit.widthMm <= 0 || unit.heightMm <= 0 || unit.shelfCount <= 0) return null

    val marginHoriz = 50f
    val marginVert = 35f
    val availW = canvasW - (marginHoriz * 2)
    val availH = canvasH - (marginVert * 2)

    val scale = min(availW / unit.widthMm.toFloat(), availH / unit.heightMm.toFloat())
    val cabW = (unit.widthMm * scale).toFloat()
    val cabH = (unit.heightMm * scale).toFloat()
    val cabX = marginHoriz + (availW - cabW) / 2f
    val cabY = marginVert + (availH - cabH) / 2f

    val sideW = (unit.sidePanelThicknessMm * scale).toFloat()
    val topH = (unit.topThicknessMm * scale).toFloat()
    val botH = (unit.bottomThicknessMm * scale).toFloat()
    val shelfH = (unit.shelfThicknessMm * scale).toFloat().coerceAtLeast(2f)

    val internalH = cabH - topH - botH
    val compCount = unit.shelfCount + 1
    val compH = (internalH - (unit.shelfCount * shelfH)) / compCount

    val tolerance = 16f // tap padding
    var currentY = cabY + topH

    for (i in 0 until unit.shelfCount) {
        currentY += compH
        val shelfTop = currentY - tolerance
        val shelfBottom = currentY + shelfH + tolerance
        val shelfLeft = cabX + sideW
        val shelfRight = cabX + cabW - sideW

        if (tap.x in shelfLeft..shelfRight && tap.y in shelfTop..shelfBottom) {
            return i
        }
        currentY += shelfH
    }
    return null
}
