package com.example

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.service.PwaDatabase
import com.example.service.PwaEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PwaPickerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setBackgroundColor(Color.BLACK)
            setPadding(32, 32, 32, 32)
        }
        
        val titleView = TextView(this).apply {
            text = "Select PWA"
            setTextColor(Color.WHITE)
            textSize = 24f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 32)
        }
        mainLayout.addView(titleView)
        
        val recyclerView = RecyclerView(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)
            layoutManager = LinearLayoutManager(this@PwaPickerActivity)
        }
        mainLayout.addView(recyclerView)
        setContentView(mainLayout)

        lifecycleScope.launch(Dispatchers.IO) {
            val db = PwaDatabase.getDatabase(applicationContext)
            val pwas = db.pwaDao().getAllPwasSync()
            
            withContext(Dispatchers.Main) {
                if (pwas.isEmpty()) {
                    val emptyText = TextView(this@PwaPickerActivity).apply {
                        text = "No PWAs imported."
                        setTextColor(Color.GRAY)
                        textSize = 18f
                        gravity = Gravity.CENTER
                        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    }
                    mainLayout.removeView(recyclerView)
                    mainLayout.addView(emptyText)
                } else {
                    recyclerView.adapter = PwaAdapter(pwas) { selectedPwa ->
                        val resultIntent = Intent().apply { putExtra("ELEMENT_ID", "pwa:${selectedPwa.id}") }
                        setResult(Activity.RESULT_OK, resultIntent)
                        finish()
                    }
                }
            }
        }
    }

    inner class PwaAdapter(private val items: List<PwaEntry>, private val onClick: (PwaEntry) -> Unit) : RecyclerView.Adapter<PwaAdapter.ViewHolder>() {
        inner class ViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
            val iconView: ImageView = view.findViewById(1)
            val nameView: TextView = view.findViewById(2)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val layout = LinearLayout(parent.context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                setPadding(0, 24, 0, 24)
                gravity = Gravity.CENTER_VERTICAL
                isClickable = true
                isFocusable = true
                setBackgroundResource(android.R.drawable.list_selector_background)
            }
            
            val icon = ImageView(parent.context).apply {
                id = 1
                layoutParams = LinearLayout.LayoutParams(96, 96).apply { setMargins(0, 0, 32, 0) }
                setImageResource(android.R.drawable.ic_menu_add) // Default icon for PWAs
                setColorFilter(Color.WHITE)
            }
            
            val name = TextView(parent.context).apply {
                id = 2
                setTextColor(Color.WHITE)
                textSize = 18f
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            }
            
            layout.addView(icon)
            layout.addView(name)
            
            return ViewHolder(layout)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.nameView.text = item.name
            holder.itemView.setOnClickListener { onClick(item) }
        }

        override fun getItemCount() = items.size
    }
}
