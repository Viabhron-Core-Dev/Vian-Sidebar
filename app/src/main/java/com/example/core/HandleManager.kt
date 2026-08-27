package com.example.core

import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

data class HandleConfig(val id: String, var name: String, var enabled: Boolean)

object HandleManager {
    fun getHandles(prefs: SharedPreferences): List<HandleConfig> {
        val jsonStr = prefs.getString("handles_list", null)
        val list = mutableListOf<HandleConfig>()
        if (jsonStr == null) {
            val defaultHandle = HandleConfig(id = "sidebar", name = "Handle 1 | Right (Bottom)", enabled = true)
            list.add(defaultHandle)
            prefs.edit().putString("handle_sidebar_swipe_left", "open_page:default_hybrid").apply()
            prefs.edit().remove("handle_sidebar_tap").apply()
            prefs.edit().putString("handle_sidebar_color", "#242962ff").apply() // 14% opacity deep blue/purple
            saveHandles(prefs, list)
            return list
        }
        try {
            val arr = JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val id = obj.optString("id")
                list.add(HandleConfig(
                    id = id,
                    name = obj.optString("name", "Handle"),
                    enabled = obj.optBoolean("enabled", true)
                ))
            }
        } catch (e: Exception) {}
        return list
    }

    fun createNewHandle(prefs: SharedPreferences, customName: String? = null): HandleConfig {
        val existing = getHandles(prefs)
        val count = existing.size + 1
        val newId = "handle_${System.currentTimeMillis()}"
        val name = customName ?: "Handle $count"
        
        // Stagger initial Y offset so handles do not stack invisibly on top of each other
        val initialY = (20 + (existing.size * 25)) % 80
        val prefix = "handle_${newId}_"
        
        prefs.edit().apply {
            putString("${prefix}edge", "right")
            putInt("${prefix}y", initialY)
            putInt("${prefix}height", 120)
            putInt("${prefix}width", 12)
            putString("${prefix}color", "#242962ff")
            putString("${prefix}shape", "slanted_block")
            // Default swipe gesture to open Home Grid sidebar
            putString("${prefix}swipe_left", "open_page:default_hybrid")
            putString("${prefix}swipe_right", "none")
            putString("${prefix}tap", "none")
            putString("${prefix}double_tap", "none")
            putString("${prefix}long_press", "none")
            putString("${prefix}swipe_up", "none")
            putString("${prefix}swipe_down", "none")
            apply()
        }

        val newHandle = HandleConfig(id = newId, name = name, enabled = true)
        val updated = existing + newHandle
        saveHandles(prefs, updated)
        return newHandle
    }

    fun saveHandles(prefs: SharedPreferences, handles: List<HandleConfig>) {
        val arr = JSONArray()
        for (h in handles) {
            val obj = JSONObject()
            obj.put("id", h.id)
            obj.put("name", h.name)
            obj.put("enabled", h.enabled)
            arr.put(obj)
        }
        prefs.edit().putString("handles_list", arr.toString()).apply()
    }
}
