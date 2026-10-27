package com.example.shelfspace.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.shelfspace.model.DimensionUnit
import com.example.shelfspace.model.ShelfResult
import com.example.shelfspace.model.StorageItem
import com.example.shelfspace.model.StorageUnit
import org.json.JSONObject
import java.io.OutputStream
import java.util.Locale

object ExportHelper {

    /**
     * Generate standard TXT export matching specification:
     * SHELFSPACE
     * Storage Unit: ...
     * Overall Size: ...
     * Usable Internal Size: ...
     * Shelf Thickness: ...
     * Number of Shelves: ...
     * Shelf Area: ...
     * Total Shelf Area: ...
     * Total Usable Shelf Length: ...
     * Storage Volume: ...
     * Compartment Height: ...
     */
    fun generateTxt(
        unit: StorageUnit,
        result: ShelfResult,
        unitSystem: DimensionUnit = DimensionUnit.CENTIMETERS,
        decimals: Int = 2
    ): String {
        val oW = unitSystem.format(unit.widthMm, decimals)
        val oD = unitSystem.format(unit.depthMm, decimals)
        val oH = unitSystem.format(unit.heightMm, decimals)

        val uW = unitSystem.format(result.usableWidthMm, decimals)
        val uD = unitSystem.format(result.usableDepthMm, decimals)
        val uH = unitSystem.format(result.usableHeightMm, decimals)

        val shelfThickStr = "${String.format(Locale.US, "%.1f", unit.shelfThicknessMm)} mm"
        val compHeightStr = unitSystem.format(result.compartmentHeightMm, decimals)

        val shelfAreaStr = String.format(Locale.US, "%.${decimals}f m²", result.shelfAreaM2)
        val totalAreaStr = String.format(Locale.US, "%.${decimals}f m²", result.totalShelfAreaM2)
        val linearLenStr = String.format(Locale.US, "%.${decimals}f m", result.linearShelfLengthM)
        val volumeStr = String.format(Locale.US, "%.1f L", result.storageVolumeLiters)

        return buildString {
            appendLine("SHELFSPACE")
            appendLine()
            appendLine("Storage Unit:")
            appendLine(unit.name)
            appendLine()
            appendLine("Overall Size:")
            appendLine("$oW × $oD × $oH")
            appendLine()
            appendLine("Usable Internal Size:")
            appendLine("$uW × $uD × $uH")
            appendLine()
            appendLine("Shelf Thickness:")
            appendLine(shelfThickStr)
            appendLine()
            appendLine("Number of Shelves:")
            appendLine("${result.shelfCount}")
            appendLine()
            appendLine("Shelf Area:")
            appendLine(shelfAreaStr)
            appendLine()
            appendLine("Total Shelf Area:")
            appendLine(totalAreaStr)
            appendLine()
            appendLine("Total Usable Shelf Length:")
            appendLine(linearLenStr)
            appendLine()
            appendLine("Storage Volume:")
            appendLine(volumeStr)
            appendLine()
            appendLine("Compartment Height:")
            appendLine(compHeightStr)
        }
    }

    /**
     * Generate standard CSV export matching specification:
     * Unit,Width,Depth,Height,Usable Width,Usable Depth,Usable Height,Shelves,Shelf Area,Total Shelf Area,Linear Shelf Length,Storage Volume
     */
    fun generateCsv(
        unit: StorageUnit,
        result: ShelfResult,
        unitSystem: DimensionUnit = DimensionUnit.CENTIMETERS,
        decimals: Int = 2
    ): String {
        val header = "Unit,Width,Depth,Height,Usable Width,Usable Depth,Usable Height,Shelves,Shelf Area,Total Shelf Area,Linear Shelf Length,Storage Volume"
        val w = unitSystem.formatValueOnly(unit.widthMm, decimals)
        val d = unitSystem.formatValueOnly(unit.depthMm, decimals)
        val h = unitSystem.formatValueOnly(unit.heightMm, decimals)
        val uw = unitSystem.formatValueOnly(result.usableWidthMm, decimals)
        val ud = unitSystem.formatValueOnly(result.usableDepthMm, decimals)
        val uh = unitSystem.formatValueOnly(result.usableHeightMm, decimals)
        val sCount = result.shelfCount
        val sArea = String.format(Locale.US, "%.${decimals}f", result.shelfAreaM2)
        val totArea = String.format(Locale.US, "%.${decimals}f", result.totalShelfAreaM2)
        val linLen = String.format(Locale.US, "%.${decimals}f", result.linearShelfLengthM)
        val vol = String.format(Locale.US, "%.1f", result.storageVolumeLiters)

        val row = "\"${unit.name}\",$w,$d,$h,$uw,$ud,$uh,$sCount,$sArea,$totArea,$linLen,$vol"
        return "$header\n$row"
    }

    /**
     * Generate standard JSON export matching specification:
     */
    fun generateJson(
        unit: StorageUnit,
        result: ShelfResult,
        items: List<StorageItem> = emptyList()
    ): String {
        val root = JSONObject().apply {
            put("name", unit.name)
            put("overall", JSONObject().apply {
                put("widthCm", unit.widthMm / 10.0)
                put("depthCm", unit.depthMm / 10.0)
                put("heightCm", unit.heightMm / 10.0)
            })
            put("usable", JSONObject().apply {
                put("widthCm", result.usableWidthMm / 10.0)
                put("depthCm", result.usableDepthMm / 10.0)
                put("heightCm", result.usableHeightMm / 10.0)
            })
            put("shelves", JSONObject().apply {
                put("count", result.shelfCount)
                put("thicknessMm", unit.shelfThicknessMm)
                put("areaPerShelfM2", result.shelfAreaM2)
                put("totalAreaM2", result.totalShelfAreaM2)
                put("linearLengthM", result.linearShelfLengthM)
                put("compartmentHeightCm", result.compartmentHeightMm / 10.0)
            })
            put("storage", JSONObject().apply {
                put("volumeLiters", String.format(Locale.US, "%.1f", result.storageVolumeLiters).toDoubleOrNull() ?: result.storageVolumeLiters)
            })
        }
        return root.toString(2)
    }

    /**
     * Generate Share text matching specification:
     */
    fun generateShareText(
        unit: StorageUnit,
        result: ShelfResult,
        unitSystem: DimensionUnit = DimensionUnit.CENTIMETERS,
        decimals: Int = 2
    ): String {
        val oW = unitSystem.format(unit.widthMm, decimals)
        val oD = unitSystem.format(unit.depthMm, decimals)
        val oH = unitSystem.format(unit.heightMm, decimals)
        val uW = unitSystem.format(result.usableWidthMm, decimals)
        val uD = unitSystem.format(result.usableDepthMm, decimals)
        val uH = unitSystem.format(result.usableHeightMm, decimals)
        val linearLenStr = String.format(Locale.US, "%.${decimals}f m", result.linearShelfLengthM)
        val totalAreaStr = String.format(Locale.US, "%.${decimals}f m²", result.totalShelfAreaM2)
        val volumeStr = String.format(Locale.US, "%.1f L", result.storageVolumeLiters)

        return """
            ShelfSpace
            
            ${unit.name}
            
            Overall:
            $oW × $oD × $oH
            
            Usable:
            $uW × $uD × $uH
            
            Shelves:
            ${result.shelfCount}
            
            Total Shelf Length:
            $linearLenStr
            
            Shelf Area:
            $totalAreaStr
            
            Storage Volume:
            $volumeStr
        """.trimIndent()
    }

    /**
     * Share text via Android Intent.ACTION_SEND
     */
    fun shareContent(context: Context, text: String, title: String = "Share ShelfSpace Result") {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "ShelfSpace Storage Calculation")
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, title))
    }

    /**
     * Write content to SAF Uri
     */
    fun writeToUri(context: Context, uri: Uri, content: String): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream: OutputStream ->
                outputStream.write(content.toByteArray(Charsets.UTF_8))
                outputStream.flush()
            }
            true
        } catch (_: Exception) {
            false
        }
    }
}
