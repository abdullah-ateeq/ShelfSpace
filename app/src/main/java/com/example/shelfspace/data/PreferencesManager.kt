package com.example.shelfspace.data

import android.content.Context
import android.content.SharedPreferences
import com.example.shelfspace.model.AppSettings
import com.example.shelfspace.model.CustomSection
import com.example.shelfspace.model.DimensionUnit
import com.example.shelfspace.model.SavedCalculation
import com.example.shelfspace.model.ShelfMode
import com.example.shelfspace.model.ShelfResult
import com.example.shelfspace.model.SpacingMode
import com.example.shelfspace.model.StorageItem
import com.example.shelfspace.model.StorageUnit
import org.json.JSONArray
import org.json.JSONObject

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "shelf_space_preferences"
        private const val KEY_SETTINGS = "app_settings"
        private const val KEY_HISTORY = "history_calculations"
        private const val KEY_MULTIPLE_UNITS = "multiple_storage_units"
        private const val KEY_CUSTOM_SECTIONS = "custom_sections"
        private const val KEY_LAST_UNIT = "last_saved_unit"
        private const val MAX_HISTORY = 10
    }

    // --- Settings ---

    fun saveSettings(settings: AppSettings) {
        val json = JSONObject().apply {
            put("themeMode", settings.themeMode)
            put("dynamicColor", settings.dynamicColor)
            put("defaultUnit", settings.defaultUnit.symbol)
            put("defaultShelfThicknessMm", settings.defaultShelfThicknessMm)
            put("defaultSidePanelThicknessMm", settings.defaultSidePanelThicknessMm)
            put("defaultBackPanelThicknessMm", settings.defaultBackPanelThicknessMm)
            put("defaultTopThicknessMm", settings.defaultTopThicknessMm)
            put("defaultBottomThicknessMm", settings.defaultBottomThicknessMm)
            put("defaultMinimumClearanceMm", settings.defaultMinimumClearanceMm)
            put("showFrontDiagram", settings.showFrontDiagram)
            put("showSideDiagram", settings.showSideDiagram)
            put("showTopDiagram", settings.showTopDiagram)
            put("showCalculationBreakdown", settings.showCalculationBreakdown)
            put("showItemCapacity", settings.showItemCapacity)
            put("showStorageUtilization", settings.showStorageUtilization)
            put("decimalPlaces", settings.decimalPlaces)
            put("liveCalculation", settings.liveCalculation)
            put("hapticsEnabled", settings.hapticsEnabled)
            put("animationsEnabled", settings.animationsEnabled)
            put("confirmDelete", settings.confirmDelete)
            put("restoreLastCalculation", settings.restoreLastCalculation)
        }
        prefs.edit().putString(KEY_SETTINGS, json.toString()).apply()
    }

    fun loadSettings(): AppSettings {
        val jsonStr = prefs.getString(KEY_SETTINGS, null) ?: return AppSettings()
        return try {
            val json = JSONObject(jsonStr)
            AppSettings(
                themeMode = json.optString("themeMode", "System"),
                dynamicColor = json.optBoolean("dynamicColor", true),
                defaultUnit = DimensionUnit.fromSymbol(json.optString("defaultUnit", "cm")),
                defaultShelfThicknessMm = json.optDouble("defaultShelfThicknessMm", 18.0),
                defaultSidePanelThicknessMm = json.optDouble("defaultSidePanelThicknessMm", 18.0),
                defaultBackPanelThicknessMm = json.optDouble("defaultBackPanelThicknessMm", 6.0),
                defaultTopThicknessMm = json.optDouble("defaultTopThicknessMm", 18.0),
                defaultBottomThicknessMm = json.optDouble("defaultBottomThicknessMm", 18.0),
                defaultMinimumClearanceMm = json.optDouble("defaultMinimumClearanceMm", 300.0),
                showFrontDiagram = json.optBoolean("showFrontDiagram", true),
                showSideDiagram = json.optBoolean("showSideDiagram", true),
                showTopDiagram = json.optBoolean("showTopDiagram", true),
                showCalculationBreakdown = json.optBoolean("showCalculationBreakdown", true),
                showItemCapacity = json.optBoolean("showItemCapacity", true),
                showStorageUtilization = json.optBoolean("showStorageUtilization", true),
                decimalPlaces = json.optInt("decimalPlaces", 2),
                liveCalculation = json.optBoolean("liveCalculation", true),
                hapticsEnabled = json.optBoolean("hapticsEnabled", true),
                animationsEnabled = json.optBoolean("animationsEnabled", true),
                confirmDelete = json.optBoolean("confirmDelete", true),
                restoreLastCalculation = json.optBoolean("restoreLastCalculation", true)
            )
        } catch (_: Exception) {
            AppSettings()
        }
    }

    fun resetSettings(): AppSettings {
        val defaultSettings = AppSettings()
        saveSettings(defaultSettings)
        return defaultSettings
    }

    // --- History (Maximum 10, FIFO) ---

    fun saveHistoryItem(item: SavedCalculation): List<SavedCalculation> {
        val currentList = loadHistory().toMutableList()
        // If already exists with same id, remove it first
        currentList.removeAll { it.id == item.id }
        // Prepend new item
        currentList.add(0, item)
        // Keep maximum 10 items
        while (currentList.size > MAX_HISTORY) {
            currentList.removeAt(currentList.lastIndex)
        }
        persistHistory(currentList)
        return currentList
    }

    fun deleteHistoryItem(id: String): List<SavedCalculation> {
        val currentList = loadHistory().toMutableList()
        currentList.removeAll { it.id == id }
        persistHistory(currentList)
        return currentList
    }

    fun clearHistory() {
        prefs.edit().remove(KEY_HISTORY).apply()
    }

    fun loadHistory(): List<SavedCalculation> {
        val jsonStr = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        return try {
            val jsonArray = JSONArray(jsonStr)
            val list = mutableListOf<SavedCalculation>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(parseSavedCalculation(obj))
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun persistHistory(list: List<SavedCalculation>) {
        val jsonArray = JSONArray()
        for (item in list) {
            jsonArray.put(serializeSavedCalculation(item))
        }
        prefs.edit().putString(KEY_HISTORY, jsonArray.toString()).apply()
    }

    // --- Multiple Units ---

    fun saveMultipleUnits(units: List<StorageUnit>) {
        val jsonArray = JSONArray()
        for (u in units) {
            jsonArray.put(serializeStorageUnit(u))
        }
        prefs.edit().putString(KEY_MULTIPLE_UNITS, jsonArray.toString()).apply()
    }

    fun loadMultipleUnits(): List<StorageUnit> {
        val jsonStr = prefs.getString(KEY_MULTIPLE_UNITS, null)
        if (jsonStr == null) {
            // Provide sample initial units
            return listOf(
                StorageUnit("unit-1", "Wardrobe A", 1200.0, 400.0, 1800.0, 18.0, 18.0, 18.0, 6.0, true, 18.0, 4),
                StorageUnit("unit-2", "Bookcase B", 800.0, 300.0, 1500.0, 18.0, 18.0, 18.0, 6.0, true, 18.0, 5),
                StorageUnit("unit-3", "Cabinet C", 1000.0, 450.0, 1200.0, 18.0, 18.0, 18.0, 6.0, true, 18.0, 3)
            )
        }
        return try {
            val jsonArray = JSONArray(jsonStr)
            val list = mutableListOf<StorageUnit>()
            for (i in 0 until jsonArray.length()) {
                list.add(parseStorageUnit(jsonArray.getJSONObject(i)))
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    // --- Custom Sections ---

    fun saveCustomSections(sections: List<CustomSection>) {
        val jsonArray = JSONArray()
        for (s in sections) {
            val obj = JSONObject().apply {
                put("id", s.id)
                put("name", s.name)
                put("widthMm", s.widthMm)
                put("depthMm", s.depthMm)
                put("heightMm", s.heightMm)
                put("shelfCount", s.shelfCount)
                put("shelfThicknessMm", s.shelfThicknessMm)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_CUSTOM_SECTIONS, jsonArray.toString()).apply()
    }

    fun loadCustomSections(): List<CustomSection> {
        val jsonStr = prefs.getString(KEY_CUSTOM_SECTIONS, null)
        if (jsonStr == null) {
            return listOf(
                CustomSection("sec-1", "Pantry Shelves", 900.0, 350.0, 2000.0, 5, 18.0),
                CustomSection("sec-2", "Kitchen Base Cabinet", 600.0, 550.0, 850.0, 2, 18.0),
                CustomSection("sec-3", "Built-in Cupboard", 1100.0, 500.0, 2100.0, 6, 18.0)
            )
        }
        return try {
            val jsonArray = JSONArray(jsonStr)
            val list = mutableListOf<CustomSection>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    CustomSection(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        widthMm = obj.getDouble("widthMm"),
                        depthMm = obj.getDouble("depthMm"),
                        heightMm = obj.getDouble("heightMm"),
                        shelfCount = obj.optInt("shelfCount", 3),
                        shelfThicknessMm = obj.optDouble("shelfThicknessMm", 18.0)
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    // --- Last Calculation State ---

    fun saveLastUnit(unit: StorageUnit) {
        prefs.edit().putString(KEY_LAST_UNIT, serializeStorageUnit(unit).toString()).apply()
    }

    fun loadLastUnit(): StorageUnit? {
        val jsonStr = prefs.getString(KEY_LAST_UNIT, null) ?: return null
        return try {
            parseStorageUnit(JSONObject(jsonStr))
        } catch (_: Exception) {
            null
        }
    }

    // --- Helpers ---

    private fun serializeStorageUnit(unit: StorageUnit): JSONObject {
        return JSONObject().apply {
            put("id", unit.id)
            put("name", unit.name)
            put("widthMm", unit.widthMm)
            put("depthMm", unit.depthMm)
            put("heightMm", unit.heightMm)
            put("sidePanelThicknessMm", unit.sidePanelThicknessMm)
            put("topThicknessMm", unit.topThicknessMm)
            put("bottomThicknessMm", unit.bottomThicknessMm)
            put("backPanelThicknessMm", unit.backPanelThicknessMm)
            put("hasBackPanel", unit.hasBackPanel)
            put("shelfThicknessMm", unit.shelfThicknessMm)
            put("shelfCount", unit.shelfCount)
            put("shelfMode", unit.shelfMode.name)
            put("spacingMode", unit.spacingMode.name)
            val customSpacingArr = JSONArray()
            unit.customSpacingMm.forEach { customSpacingArr.put(it) }
            put("customSpacingMm", customSpacingArr)
            val customThickArr = JSONArray()
            unit.customShelfThicknessesMm.forEach { customThickArr.put(it) }
            put("customShelfThicknessesMm", customThickArr)
        }
    }

    private fun parseStorageUnit(obj: JSONObject): StorageUnit {
        val customSpacingList = mutableListOf<Double>()
        val customSpacingArr = obj.optJSONArray("customSpacingMm")
        if (customSpacingArr != null) {
            for (i in 0 until customSpacingArr.length()) {
                customSpacingList.add(customSpacingArr.getDouble(i))
            }
        }

        val customThickList = mutableListOf<Double>()
        val customThickArr = obj.optJSONArray("customShelfThicknessesMm")
        if (customThickArr != null) {
            for (i in 0 until customThickArr.length()) {
                customThickList.add(customThickArr.getDouble(i))
            }
        }

        return StorageUnit(
            id = obj.getString("id"),
            name = obj.getString("name"),
            widthMm = obj.getDouble("widthMm"),
            depthMm = obj.getDouble("depthMm"),
            heightMm = obj.getDouble("heightMm"),
            sidePanelThicknessMm = obj.optDouble("sidePanelThicknessMm", 18.0),
            topThicknessMm = obj.optDouble("topThicknessMm", 18.0),
            bottomThicknessMm = obj.optDouble("bottomThicknessMm", 18.0),
            backPanelThicknessMm = obj.optDouble("backPanelThicknessMm", 6.0),
            hasBackPanel = obj.optBoolean("hasBackPanel", true),
            shelfThicknessMm = obj.optDouble("shelfThicknessMm", 18.0),
            shelfCount = obj.optInt("shelfCount", 4),
            shelfMode = try { ShelfMode.valueOf(obj.optString("shelfMode", "FIXED")) } catch (_: Exception) { ShelfMode.FIXED },
            spacingMode = try { SpacingMode.valueOf(obj.optString("spacingMode", "EQUAL")) } catch (_: Exception) { SpacingMode.EQUAL },
            customSpacingMm = customSpacingList,
            customShelfThicknessesMm = customThickList
        )
    }

    private fun serializeSavedCalculation(saved: SavedCalculation): JSONObject {
        return JSONObject().apply {
            put("id", saved.id)
            put("name", saved.name)
            put("timestamp", saved.timestamp)
            put("unit", serializeStorageUnit(saved.unit))
            put("result", JSONObject().apply {
                put("usableWidthMm", saved.result.usableWidthMm)
                put("usableDepthMm", saved.result.usableDepthMm)
                put("usableHeightMm", saved.result.usableHeightMm)
                put("shelfAreaMm2", saved.result.shelfAreaMm2)
                put("totalShelfAreaMm2", saved.result.totalShelfAreaMm2)
                put("linearShelfLengthMm", saved.result.linearShelfLengthMm)
                put("storageVolumeMm3", saved.result.storageVolumeMm3)
                put("compartmentHeightMm", saved.result.compartmentHeightMm)
                put("shelfCount", saved.result.shelfCount)
                put("compartmentCount", saved.result.compartmentCount)
                put("isValid", saved.result.isValid)
            })
            val itemsArr = JSONArray()
            for (item in saved.items) {
                itemsArr.put(JSONObject().apply {
                    put("id", item.id)
                    put("name", item.name)
                    put("widthMm", item.widthMm)
                    put("depthMm", item.depthMm)
                    put("heightMm", item.heightMm)
                    put("quantity", item.quantity)
                    put("allowStacking", item.allowStacking)
                })
            }
            put("items", itemsArr)
        }
    }

    private fun parseSavedCalculation(obj: JSONObject): SavedCalculation {
        val unit = parseStorageUnit(obj.getJSONObject("unit"))
        val resObj = obj.getJSONObject("result")
        val result = ShelfResult(
            usableWidthMm = resObj.getDouble("usableWidthMm"),
            usableDepthMm = resObj.getDouble("usableDepthMm"),
            usableHeightMm = resObj.getDouble("usableHeightMm"),
            shelfAreaMm2 = resObj.getDouble("shelfAreaMm2"),
            totalShelfAreaMm2 = resObj.getDouble("totalShelfAreaMm2"),
            linearShelfLengthMm = resObj.getDouble("linearShelfLengthMm"),
            storageVolumeMm3 = resObj.getDouble("storageVolumeMm3"),
            compartmentHeightMm = resObj.getDouble("compartmentHeightMm"),
            shelfCount = resObj.getInt("shelfCount"),
            compartmentCount = resObj.optInt("compartmentCount", resObj.getInt("shelfCount") + 1),
            isValid = resObj.optBoolean("isValid", true)
        )
        val itemsList = mutableListOf<StorageItem>()
        val itemsArr = obj.optJSONArray("items")
        if (itemsArr != null) {
            for (i in 0 until itemsArr.length()) {
                val itObj = itemsArr.getJSONObject(i)
                itemsList.add(
                    StorageItem(
                        id = itObj.getString("id"),
                        name = itObj.getString("name"),
                        widthMm = itObj.getDouble("widthMm"),
                        depthMm = itObj.getDouble("depthMm"),
                        heightMm = itObj.getDouble("heightMm"),
                        quantity = itObj.optInt("quantity", 1),
                        allowStacking = itObj.optBoolean("allowStacking", false)
                    )
                )
            }
        }
        return SavedCalculation(
            id = obj.getString("id"),
            name = obj.getString("name"),
            timestamp = obj.getLong("timestamp"),
            unit = unit,
            result = result,
            items = itemsList
        )
    }
}
