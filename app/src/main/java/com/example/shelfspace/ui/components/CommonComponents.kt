package com.example.shelfspace.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shelfspace.model.DimensionUnit
import com.example.shelfspace.model.ItemFitResult
import com.example.shelfspace.model.ShelfResult
import com.example.shelfspace.model.StorageItem
import com.example.shelfspace.model.StorageUnit
import com.example.shelfspace.model.StorageUtilizationResult
import com.example.shelfspace.ui.theme.ErrorRed
import com.example.shelfspace.ui.theme.SuccessGreen
import com.example.shelfspace.ui.theme.WarningAmber
import java.util.Locale

/**
 * Dimension input text field with unit label and error validation.
 */
@Composable
fun DimensionInputField(
    label: String,
    valueText: String,
    onValueChange: (String) -> Unit,
    unitSymbol: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null,
    keyboardType: KeyboardType = KeyboardType.Decimal
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = valueText,
            onValueChange = onValueChange,
            label = { Text(label) },
            trailingIcon = {
                Text(
                    text = unitSymbol,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 12.dp)
                )
            },
            isError = isError,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        )
        if (isError && !errorMessage.isNullOrEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }
    }
}

/**
 * Main Hero Result Card matching specification:
 * ┌─────────────────────────────┐
 * │         SHELFSPACE          │
 * │                             │
 * │ TOTAL SHELF LENGTH          │
 * │          4.66 m             │
 * │                             │
 * │ SHELF AREA                  │
 * │          1.83 m²            │
 * │                             │
 * │ STORAGE VOLUME              │
 * │          80.8 L             │
 * │                             │
 * │ SHELVES                     │
 * │             4               │
 * │                             │
 * │ COMPARTMENTS                │
 * │             5               │
 * └─────────────────────────────┘
 */
@Composable
fun MainHeroResultCard(
    result: ShelfResult,
    decimals: Int = 2,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "SHELFSPACE",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "TOTAL USABLE SHELF LENGTH",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
            Text(
                text = String.format(Locale.US, "%.${decimals}f m", result.linearShelfLengthM),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Shelf Area",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Text(
                        text = String.format(Locale.US, "%.${decimals}f m²", result.totalShelfAreaM2),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Storage Volume",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Text(
                        text = String.format(Locale.US, "%.1f L", result.storageVolumeLiters),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Shelves",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "${result.shelfCount}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Compartments",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "${result.compartmentCount}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

/**
 * Secondary Detail Cards:
 * - Usable Dimensions
 * - Shelf
 * - Storage
 */
@Composable
fun SecondaryResultCards(
    unit: StorageUnit,
    result: ShelfResult,
    unitSystem: DimensionUnit = DimensionUnit.CENTIMETERS,
    decimals: Int = 2,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Usable Dimensions Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "USABLE INTERNAL DIMENSIONS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MetricItem("Width", unitSystem.format(result.usableWidthMm, decimals))
                    MetricItem("Depth", unitSystem.format(result.usableDepthMm, decimals))
                    MetricItem("Height", unitSystem.format(result.usableHeightMm, decimals))
                }
            }
        }

        // Shelf Details Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "SHELF DETAILS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MetricItem("Area / shelf", String.format(Locale.US, "%.${decimals}f m²", result.shelfAreaM2))
                    MetricItem("Length / shelf", String.format(Locale.US, "%.${decimals}f m", result.usableWidthMm / 1000.0))
                    MetricItem("Clearance", unitSystem.format(result.compartmentHeightMm, decimals))
                }
            }
        }
    }
}

@Composable
private fun MetricItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Expandable "How was this calculated?" breakdown card.
 */
@Composable
fun CalculationBreakdownCard(
    unit: StorageUnit,
    result: ShelfResult,
    unitSystem: DimensionUnit = DimensionUnit.CENTIMETERS,
    decimals: Int = 2,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "How was this calculated?",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BreakdownStep(
                        title = "1. Usable Internal Width",
                        formula = "${unitSystem.format(unit.widthMm, decimals)} − (2 × ${unit.sidePanelThicknessMm} mm side panels)",
                        result = unitSystem.format(result.usableWidthMm, decimals)
                    )

                    BreakdownStep(
                        title = "2. Usable Internal Depth",
                        formula = if (unit.hasBackPanel) {
                            "${unitSystem.format(unit.depthMm, decimals)} − (${unit.backPanelThicknessMm} mm back panel)"
                        } else {
                            "${unitSystem.format(unit.depthMm, decimals)} (no back panel)"
                        },
                        result = unitSystem.format(result.usableDepthMm, decimals)
                    )

                    BreakdownStep(
                        title = "3. Usable Internal Height",
                        formula = "${unitSystem.format(unit.heightMm, decimals)} − (${unit.topThicknessMm} mm top + ${unit.bottomThicknessMm} mm bottom)",
                        result = unitSystem.format(result.usableHeightMm, decimals)
                    )

                    BreakdownStep(
                        title = "4. Single Shelf Surface Area",
                        formula = "${unitSystem.format(result.usableWidthMm, decimals)} × ${unitSystem.format(result.usableDepthMm, decimals)}",
                        result = String.format(Locale.US, "%.${decimals}f m²", result.shelfAreaM2)
                    )

                    BreakdownStep(
                        title = "5. Total Shelf Area",
                        formula = "${String.format(Locale.US, "%.4f", result.shelfAreaM2)} m² × ${result.shelfCount} shelves",
                        result = String.format(Locale.US, "%.${decimals}f m²", result.totalShelfAreaM2)
                    )

                    BreakdownStep(
                        title = "6. Total Usable Shelf Length",
                        formula = "${unitSystem.format(result.usableWidthMm, decimals)} × ${result.shelfCount} shelves",
                        result = String.format(Locale.US, "%.${decimals}f m", result.linearShelfLengthM)
                    )

                    BreakdownStep(
                        title = "7. Compartment Spacing",
                        formula = "[${unitSystem.format(result.usableHeightMm, decimals)} − (${result.shelfCount} × ${unit.shelfThicknessMm} mm)] ÷ ${result.compartmentCount} compartments",
                        result = unitSystem.format(result.compartmentHeightMm, decimals)
                    )

                    BreakdownStep(
                        title = "8. Total Storage Volume",
                        formula = "${unitSystem.format(result.usableWidthMm, decimals)} × ${unitSystem.format(result.usableDepthMm, decimals)} × ${unitSystem.format(result.usableHeightMm, decimals)}",
                        result = String.format(Locale.US, "%.1f L", result.storageVolumeLiters)
                    )
                }
            }
        }
    }
}

@Composable
private fun BreakdownStep(title: String, formula: String, result: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(6.dp))
            .padding(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = formula,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = "= $result",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * Storage utilization progress bar card:
 * Visual progress bar showing occupied vs free space.
 */
@Composable
fun StorageUtilizationCard(
    utilization: StorageUtilizationResult,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "STORAGE UTILIZATION",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(8.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { (utilization.utilizationPercentage / 100f).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = if (utilization.exceedsVolume) ErrorRed else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${String.format(Locale.US, "%.1f", utilization.utilizationPercentage)}% occupied (${String.format(Locale.US, "%.1f", utilization.occupiedVolumeLiters)} L)",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = if (utilization.exceedsVolume) ErrorRed else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${String.format(Locale.US, "%.1f", utilization.freeVolumePercentage)}% free (${String.format(Locale.US, "%.1f", utilization.freeVolumeLiters)} L)",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            if (utilization.exceedsVolume) {
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = WarningAmber,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "⚠ VOLUME EXCEEDS STORAGE (Volume alone does not guarantee physical packing)",
                        style = MaterialTheme.typography.labelSmall,
                        color = WarningAmber
                    )
                }
            }
        }
    }
}

/**
 * Item Fit and Capacity Result Card.
 */
@Composable
fun ItemCapacityResultCard(
    fitResult: ItemFitResult,
    unitSystem: DimensionUnit = DimensionUnit.CENTIMETERS,
    decimals: Int = 1,
    modifier: Modifier = Modifier
) {
    val statusColor = when {
        !fitResult.fits -> ErrorRed
        fitResult.isLowClearance -> WarningAmber
        else -> SuccessGreen
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = statusColor.copy(alpha = 0.08f)
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (fitResult.fits) Icons.Default.CheckCircle else Icons.Default.Error,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = fitResult.statusMessage,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }

                if (fitResult.isRotated) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "Rotated for max capacity",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            if (!fitResult.fits && fitResult.specificFailureReason != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = fitResult.specificFailureReason,
                    style = MaterialTheme.typography.bodySmall,
                    color = ErrorRed
                )
            }

            if (fitResult.fits) {
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Across", style = MaterialTheme.typography.labelSmall)
                        Text("${fitResult.itemsAcross}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Deep", style = MaterialTheme.typography.labelSmall)
                        Text("${fitResult.itemsDeep}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Layers", style = MaterialTheme.typography.labelSmall)
                        Text("${fitResult.verticalLayers}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("/ Comp.", style = MaterialTheme.typography.labelSmall)
                        Text("${fitResult.itemsPerCompartment}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        Text("${fitResult.totalCapacity}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Clearances: Width: ${unitSystem.format(fitResult.clearanceWidthMm, decimals)} | Depth: ${unitSystem.format(fitResult.clearanceDepthMm, decimals)} | Height: ${unitSystem.format(fitResult.clearanceHeightMm, decimals)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )

                if (fitResult.isLowClearance) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "⚠ LOW CLEARANCE: Only ${unitSystem.format(fitResult.clearanceHeightMm, decimals)} remains above item.",
                        style = MaterialTheme.typography.labelSmall,
                        color = WarningAmber,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (fitResult.overflow > 0) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "⚠ ${fitResult.overflow} items need additional space",
                        style = MaterialTheme.typography.labelSmall,
                        color = ErrorRed,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
