package com.example

import android.app.Activity
import android.os.Bundle
import org.json.JSONArray
import org.json.JSONObject

class TestActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val jArr = JSONArray()
        val str = "folder:123:{\"name\":\"Test\",\"items\":[]}"
        jArr.put(str)
        val saved = jArr.toString()
        println("Saved: $saved")
        
        val loadedArr = JSONArray(saved)
        val loadedStr = loadedArr.getString(0)
        println("Loaded: $loadedStr")
    }
}
