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
            val wrap = when(type) { "calculator", "compass", "notification", "scheduler", "app_tracker", "resources_tracker", "media_player" -> false else -> true }
            val h = when(type) { "calculator" -> 450; "compass" -> 500; "notification", "scheduler", "resources_tracker", "media_player", "widget", "widgets_grid", "hybrid_grid" -> 500; "app_tracker" -> 600; else -> 450 }
            return SidebarPage(
                id = id, type = type, title = title,
                wrapContentHeight = wrap, height = h, width = 320
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
        val handleId = getCleanHandleId(rawHandleId)
        val legacy = if (handleId == "sidebar") prefs.getString("sidebar_pages", null) else null
        val pagesJson = prefs.getString("handle_${handleId}_pages", legacy)
        val defaultPageId = if (handleId == "sidebar") "default_hybrid" else "default_hybrid_$handleId"
        val defaultPage = SidebarPage(id = defaultPageId, type = "hybrid_grid", title = "Home Grid")
        if (!prefs.contains("hybrid_grid_" + defaultPageId)) {
            val jsonStr = "[{\"id\": \"system:ebook_reader\", \"cols\": 1, \"rows\": 1, \"x\": 0, \"y\": 0}, {\"id\": \"system:log_keeper\", \"cols\": 1, \"rows\": 1, \"x\": 1, \"y\": 0}]"
            prefs.edit().putString("hybrid_grid_" + defaultPageId, jsonStr).apply()
            prefs.edit().putInt("hybrid_grid_cols_$defaultPageId", 3).apply()
            prefs.edit().putBoolean("handle_${handleId}_sidebar_wrap_content", true).apply()
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
                if (page.type != "dictionary" && page.type != "pwa_loader" && seenIds.add(page.id)) {
                    list.add(page)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return listOf(defaultPage)
        }
        
        return if (list.isEmpty()) listOf(defaultPage) else list
    }

    fun savePages(prefs: SharedPreferences, rawHandleId: String, pages: List<SidebarPage>) {
        val handleId = getCleanHandleId(rawHandleId)
        val arr = JSONArray()
        val seenIds = mutableSetOf<String>()
        pages.filter { seenIds.add(it.id) }.forEach { arr.put(it.toJson()) }
        prefs.edit().putString("handle_${handleId}_pages", arr.toString()).apply()
    }

    fun getDefaultPageIndex(prefs: SharedPreferences, rawHandleId: String): Int {
        val handleId = getCleanHandleId(rawHandleId)
        return prefs.getInt("handle_${handleId}_default_page_index", prefs.getInt("sidebar_default_page_index", 0))
    }

    fun saveDefaultPageIndex(prefs: SharedPreferences, rawHandleId: String, index: Int) {
        val handleId = getCleanHandleId(rawHandleId)
        prefs.edit().putInt("handle_${handleId}_default_page_index", index).apply()
    }
}
