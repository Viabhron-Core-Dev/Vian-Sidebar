package com.example.utils

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class SidebarPage(
    val id: String,
    val type: String,
    var title: String,
    var gridColumns: Int = 3,
    var gridWrapContent: Boolean = true,
    var stickAlignment: String = "bottom",
    var useCustomSettings: Boolean = false,
    var width: Int = 180,
    var height: Int = 450,
    var wrapContentHeight: Boolean = true,
    var transparency: Float = 0.9f
) {
    fun toJson(): JSONObject {
        val obj = JSONObject()
        obj.put("id", id)
        obj.put("type", type)
        obj.put("title", title)
        obj.put("gridColumns", gridColumns)
        obj.put("gridWrapContent", gridWrapContent)
        obj.put("stickAlignment", stickAlignment)
        obj.put("useCustomSettings", useCustomSettings)
        obj.put("width", width)
        obj.put("height", height)
        obj.put("wrapContentHeight", wrapContentHeight)
        obj.put("transparency", transparency.toDouble())
        return obj
    }

    companion object {
        fun createDefault(id: String, type: String, title: String): SidebarPage {
            val isStandaloneTool = type in listOf("calculator", "compass", "notification", "notifications", "scheduler", "app_tracker", "resources_tracker")
            val wrap = when (type) {
                "calculator", "compass", "notification", "notifications", "scheduler", "app_tracker", "resources_tracker" -> false
                "media_player" -> true
                else -> true
            }
            val h = when (type) {
                "calculator" -> 460
                "compass" -> 480
                "notification", "notifications", "scheduler" -> 520
                "resources_tracker" -> 460
                "app_tracker" -> 560
                "media_player" -> 360
                "widget", "widgets_grid", "hybrid_grid" -> 500
                else -> 450
            }
            val w = when (type) {
                "calculator", "compass", "resources_tracker" -> 320
                "notification", "notifications", "scheduler", "app_tracker" -> 330
                "media_player" -> 300
                "widgets_grid", "hybrid_grid" -> 260
                else -> 216
            }
            return SidebarPage(
                id = id, type = type, title = title,
                useCustomSettings = isStandaloneTool,
                wrapContentHeight = wrap, height = h, width = w
            )
        }
        
        fun fromJson(obj: JSONObject): SidebarPage {
            return SidebarPage(
                id = obj.getString("id"),
                type = obj.getString("type"),
                title = obj.getString("title"),
                gridColumns = obj.optInt("gridColumns", 3),
                gridWrapContent = obj.optBoolean("gridWrapContent", true),
                stickAlignment = obj.optString("stickAlignment", "bottom"),
                useCustomSettings = obj.optBoolean("useCustomSettings", false),
                width = obj.optInt("width", 180),
                height = obj.optInt("height", 450),
                wrapContentHeight = obj.optBoolean("wrapContentHeight", true),
                transparency = obj.optDouble("transparency", 0.9).toFloat()
            )
        }
    }
}

object PageManager {
    fun getCleanHandleId(handleOrContainerId: String): String {
        return when {
            handleOrContainerId.contains("_swipe_") -> handleOrContainerId.substringBefore("_swipe_")
            handleOrContainerId.endsWith("_tap") -> handleOrContainerId.removeSuffix("_tap")
            handleOrContainerId.endsWith("_double_tap") -> handleOrContainerId.removeSuffix("_double_tap")
            handleOrContainerId.endsWith("_long_press") -> handleOrContainerId.removeSuffix("_long_press")
            else -> handleOrContainerId
        }
    }

    fun getPages(prefs: SharedPreferences, rawHandleId: String): List<SidebarPage> {
        val cleanHandleId = getCleanHandleId(rawHandleId)
        val containerSpecificJson = prefs.getString("handle_${rawHandleId}_pages", null)
        val legacy = if (rawHandleId == "sidebar" || cleanHandleId == "sidebar") prefs.getString("sidebar_pages", null) else null
        val pagesJson = containerSpecificJson ?: prefs.getString("handle_${cleanHandleId}_pages", legacy)
        
        val defaultPageId = if (rawHandleId == "sidebar") "default_hybrid" else "default_hybrid_$rawHandleId"
        val defaultPage = SidebarPage(id = defaultPageId, type = "hybrid_grid", title = "Home Grid")
        if (!prefs.contains("hybrid_grid_" + defaultPageId)) {
            val jsonStr = "[{\"id\": \"system:ebook_reader\", \"cols\": 1, \"rows\": 1, \"x\": 0, \"y\": 0}, {\"id\": \"system:log_keeper\", \"cols\": 1, \"rows\": 1, \"x\": 1, \"y\": 0}]"
            prefs.edit().putString("hybrid_grid_" + defaultPageId, jsonStr).apply()
            prefs.edit().putInt("hybrid_grid_cols_$defaultPageId", 3).apply()
            prefs.edit().putBoolean("handle_${rawHandleId}_sidebar_wrap_content", true).apply()
        }
        if (pagesJson == null) {
            // Default setup
            return listOf(defaultPage)
        }
        val list = mutableListOf<SidebarPage>()
        val seenIds = mutableSetOf<String>()
        try {
            val arr = JSONArray(pagesJson)
            for (i in 0 until arr.length()) {
                val page = SidebarPage.fromJson(arr.getJSONObject(i))
                val sanitizedType = if (page.type == "default_hybrid") "hybrid_grid" else page.type
                val sanitizedTitle = if (page.id.startsWith("default_hybrid") || page.title.equals("default_hybrid", ignoreCase = true)) {
                    "Home Grid"
                } else if (page.title == "Home Grid" && !page.id.startsWith("default_hybrid")) {
                    "Hybrid"
                } else {
                    page.title
                }
                val sanitizedPage = if (sanitizedType != page.type || sanitizedTitle != page.title) {
                    page.copy(type = sanitizedType, title = sanitizedTitle)
                } else page
                
                // Only prevent duplicate default home grid if one with defaultPageId is already present
                val isDuplicateDefaultHomeGrid = (sanitizedPage.id == defaultPageId && list.any { it.id == defaultPageId })
                
                if (sanitizedPage.type != "dictionary" && sanitizedPage.type != "pwa_loader" && !isDuplicateDefaultHomeGrid && seenIds.add(sanitizedPage.id)) {
                    list.add(sanitizedPage)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return listOf(defaultPage)
        }
        
        return if (list.isEmpty()) listOf(defaultPage) else list
    }

    fun savePages(prefs: SharedPreferences, rawHandleId: String, pages: List<SidebarPage>) {
        val arr = JSONArray()
        val seenIds = mutableSetOf<String>()
        pages.filter { seenIds.add(it.id) }.forEach { arr.put(it.toJson()) }
        prefs.edit().putString("handle_${rawHandleId}_pages", arr.toString()).apply()
    }

    fun getDefaultPageIndex(prefs: SharedPreferences, rawHandleId: String): Int {
        val cleanHandleId = getCleanHandleId(rawHandleId)
        return prefs.getInt("handle_${rawHandleId}_default_page_index", prefs.getInt("handle_${cleanHandleId}_default_page_index", prefs.getInt("sidebar_default_page_index", 0)))
    }

    fun saveDefaultPageIndex(prefs: SharedPreferences, rawHandleId: String, index: Int) {
        prefs.edit().putInt("handle_${rawHandleId}_default_page_index", index).apply()
    }

    fun isPageTypePresent(prefs: SharedPreferences, pageType: String): Boolean {
        for ((key, value) in prefs.all) {
            if (value is String && (key.endsWith("_pages") || key == "sidebar_pages" || key.contains("pages") || key.contains("handle"))) {
                if (value.contains("\"$pageType\"") || value.contains(":$pageType") || value.contains("/$pageType")) {
                    return true
                }
                try {
                    val arr = JSONArray(value)
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        val type = obj.optString("type")
                        val id = obj.optString("id")
                        if (type == pageType || id.contains(pageType)) {
                            return true
                        }
                    }
                } catch (e: Exception) {}
            }
        }
        return false
    }
}
