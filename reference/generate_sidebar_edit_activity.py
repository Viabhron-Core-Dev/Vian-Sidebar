import sys

content = """package com.example

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.service.SidebarAppsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.util.Collections

class SidebarEditActivity : ComponentActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EditAdapter
    private lateinit var prefs: android.content.SharedPreferences
    val localIds = mutableListOf<String>()
    
    private lateinit var manager: SidebarAppsManager
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        prefs = getSharedPreferences("FloatingReaderPrefs", Context.MODE_PRIVATE)
        manager = SidebarAppsManager(this, prefs, serviceScope) {}
        
        loadLocalIds()

        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setPadding(32, 32, 32, 32)
            gravity = Gravity.CENTER_HORIZONTAL
            setBackgroundColor(Color.BLACK)
        }

        // Header
        val headerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 0, 0, 16)
        }

        val btnAdd = Button(this).apply {
            text = "Add"
            setOnClickListener {
                val intent = Intent(this@SidebarEditActivity, AddElementActivity::class.java)
                startActivityForResult(intent, 100)
            }
        }

        val btnEmpty = Button(this).apply {
            text = "Empty"
            setOnClickListener {
                localIds.add("empty:${System.currentTimeMillis()}")
                adapter.notifyItemInserted(localIds.size - 1)
            }
        }
        
        val btnSave = Button(this).apply {
            text = "Save"
            setOnClickListener {
                saveIds()
                finish()
            }
        }

        val btnCancel = Button(this).apply {
            text = "Cancel"
            setOnClickListener {
                finish()
            }
        }
        
        headerLayout.addView(btnAdd)
        headerLayout.addView(btnEmpty)
        headerLayout.addView(btnSave)
        headerLayout.addView(btnCancel)
        mainLayout.addView(headerLayout)

        adapter = EditAdapter()
        recyclerView = RecyclerView(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)
            layoutManager = GridLayoutManager(this@SidebarEditActivity, 3)
            this.adapter = this@SidebarEditActivity.adapter
        }
        mainLayout.addView(recyclerView)

        setContentView(mainLayout)
        setupItemTouchHelper()
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK) {
            val id = data?.getStringExtra("ELEMENT_ID")
            if (id != null) {
                localIds.add(id)
                adapter.notifyItemInserted(localIds.size - 1)
            }
        }
    }

    private fun loadLocalIds() {
        val jsonStr = prefs.getString("sidebar_items", "[]") ?: "[]"
        val arr = JSONArray(jsonStr)
        localIds.clear()
        for (i in 0 until arr.length()) {
            localIds.add(arr.getString(i))
        }
    }

    private fun saveIds() {
        val arr = JSONArray()
        localIds.forEach { arr.put(it) }
        prefs.edit().putString("sidebar_items", arr.toString()).apply()
        
        val intent = Intent("UPDATE_SIDEBAR_UI")
        sendBroadcast(intent)
    }

    private fun setupItemTouchHelper() {
        val callback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT,
            0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPos = viewHolder.bindingAdapterPosition
                val toPos = target.bindingAdapterPosition
                if (fromPos == RecyclerView.NO_POSITION || toPos == RecyclerView.NO_POSITION) return false
                Collections.swap(localIds, fromPos, toPos)
                adapter.notifyItemMoved(fromPos, toPos)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
            override fun isLongPressDragEnabled(): Boolean = true
        }
        ItemTouchHelper(callback).attachToRecyclerView(recyclerView)
    }

    inner class EditAdapter : RecyclerView.Adapter<EditAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val iconView: ImageView = view.findViewById(1)
            val nameView: TextView = view.findViewById(2)
            val btnRemove: ImageView = view.findViewById(3)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val layout = FrameLayout(this@SidebarEditActivity).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    250 // dp roughly
                )
                setPadding(8, 8, 8, 8)
            }
            
            val contentLayout = LinearLayout(this@SidebarEditActivity).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                gravity = Gravity.CENTER
                setBackgroundColor(Color.parseColor("#33FFFFFF"))
            }

            val iconView = ImageView(this@SidebarEditActivity).apply {
                id = 1
                layoutParams = LinearLayout.LayoutParams(100, 100)
                scaleType = ImageView.ScaleType.FIT_CENTER
            }

            val nameView = TextView(this@SidebarEditActivity).apply {
                id = 2
                setTextColor(Color.WHITE)
                textSize = 12f
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    topMargin = 8
                }
                maxLines = 2
            }

            contentLayout.addView(iconView)
            contentLayout.addView(nameView)
            
            val btnRemove = ImageView(this@SidebarEditActivity).apply {
                id = 3
                setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
                layoutParams = FrameLayout.LayoutParams(64, 64).apply {
                    gravity = Gravity.TOP or Gravity.END
                }
                setColorFilter(Color.RED)
            }

            layout.addView(contentLayout)
            layout.addView(btnRemove)

            return ViewHolder(layout)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val id = localIds[position]
            
            if (id.startsWith("empty:")) {
                holder.iconView.setImageDrawable(null)
                holder.nameView.text = "Empty Space"
            } else {
                serviceScope.launch {
                    val info = manager.getItemInfoBlocking(id)
                    withContext(Dispatchers.Main) {
                        if (info != null) {
                            holder.nameView.text = info.label
                            holder.iconView.setImageDrawable(info.icon)
                        } else {
                            holder.nameView.text = "Unknown"
                            holder.iconView.setImageResource(android.R.drawable.sym_def_app_icon)
                        }
                    }
                }
            }

            holder.btnRemove.setOnClickListener {
                val pos = holder.bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    localIds.removeAt(pos)
                    notifyItemRemoved(pos)
                }
            }
        }

        override fun getItemCount(): Int = localIds.size
    }
}
"""

with open('app/src/main/java/com/example/SidebarEditActivity.kt', 'w') as f:
    f.write(content)
