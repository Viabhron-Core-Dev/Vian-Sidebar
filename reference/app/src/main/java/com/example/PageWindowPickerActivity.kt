package com.example

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView

class PageWindowPickerActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val listView = ListView(this)
        val options = listOf(
            "Short Reminders" to "scheduler",
            "Calculator" to "calculator",
            "Compass" to "compass",
            "Notifications" to "notifications",
            "App Tracker" to "app_tracker",
            "Resources Tracker" to "resources_tracker",
            "File Explorer" to "file_explorer",
            "Local Terminal" to "local_terminal",
            "Termux (PRoot)" to "termux",
        )
        listView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, options.map { it.first })
        listView.setOnItemClickListener { _, _, position, _ ->
            val resultIntent = Intent().apply { 
                putExtra("ELEMENT_ID", "page_window:${options[position].second}")
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
        setContentView(listView)
    }
}
