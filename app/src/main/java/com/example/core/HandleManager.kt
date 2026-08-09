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
            list.add(HandleConfig(id = "sidebar", name = "Handle 1 | Right (Bottom)", enabled = true))
            prefs.edit().putString("handle_sidebar_tap", "toggle_sidebar").apply()
            prefs.edit().putString("handle_sidebar_swipe_left", "open_page:default_hybrid").apply()
            return list
        }
        try {
            val arr = JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val id = obj.optString("id")
                if (!prefs.contains("handle_${id}_tap")) {
                    prefs.edit().putString("handle_${id}_tap", "toggle_sidebar").apply()
                }
                if (!prefs.contains("handle_${id}_swipe_left")) {
                    val defaultPageId = if (id == "sidebar") "default_hybrid" else "default_hybrid_$id"
                    prefs.edit().putString("handle_${id}_swipe_left", "open_page:$defaultPageId").apply()
                }
                list.add(HandleConfig(
                    id = id,
                    name = obj.optString("name", "Handle"),
                    enabled = obj.optBoolean("enabled", true)
                ))
            }
        } catch (e: Exception) {}
        return list
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
