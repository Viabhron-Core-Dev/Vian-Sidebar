import sys

content = """package com.example

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.utils.AppWidgetHelper
import com.example.service.WidgetsGridPageView
import com.example.service.GridWidgetItem
import org.json.JSONArray
import org.json.JSONObject
import java.util.Collections

class WidgetsGridEditActivity : ComponentActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: WidgetEditAdapter
    private lateinit var prefs: android.content.SharedPreferences
    private lateinit var pageId: String
    
    val localItems = mutableListOf<GridWidgetItem>()
    private lateinit var appWidgetManager: AppWidgetManager
    
    private lateinit var previewContainer: FrameLayout
    private lateinit var tvTotalCols: TextView
    private var totalCols = 4

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        pageId = intent.getStringExtra("PAGE_ID") ?: run {
            finish()
            return
        }
        
        prefs = getSharedPreferences("FloatingReaderPrefs", Context.MODE_PRIVATE)
        appWidgetManager = AppWidgetManager.getInstance(this)
        
        loadLocalItems()
        
        totalCols = prefs.getInt("widgets_grid_cols_$pageId", 4)

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

        val titleView = TextView(this).apply {
            text = "Edit Widgets Grid"
            setTextColor(Color.WHITE)
            textSize = 20f
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }

        val btnClose = Button(this).apply {
            text = "Done"
            setOnClickListener {
                saveItems()
                finish()
            }
        }

        headerLayout.addView(titleView)
        headerLayout.addView(btnClose)
        mainLayout.addView(headerLayout)
        
        // Grid Total Size Editor
        val gridTotalLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 0, 0, 16)
        }
        
        tvTotalCols = TextView(this).apply {
            text = "Grid Columns: $totalCols"
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        
        val btnMinusCol = Button(this).apply {
            text = "-"
            setOnClickListener {
                if (totalCols > 1) {
                    totalCols--
                    prefs.edit().putInt("widgets_grid_cols_$pageId", totalCols).apply()
                    tvTotalCols.text = "Grid Columns: $totalCols"
                    updatePreview()
                }
            }
        }
        
        val btnPlusCol = Button(this).apply {
            text = "+"
            setOnClickListener {
                if (totalCols < 10) {
                    totalCols++
                    prefs.edit().putInt("widgets_grid_cols_$pageId", totalCols).apply()
                    tvTotalCols.text = "Grid Columns: $totalCols"
                    updatePreview()
                }
            }
        }
        
        gridTotalLayout.addView(tvTotalCols)
        gridTotalLayout.addView(btnMinusCol)
        gridTotalLayout.addView(btnPlusCol)
        mainLayout.addView(gridTotalLayout)
        
        // Preview Box
        previewContainer = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 0.4f).apply {
                setMargins(0, 16, 0, 16)
            }
            setBackgroundColor(Color.parseColor("#22FFFFFF"))
        }
        mainLayout.addView(previewContainer)
        updatePreview()

        // Recycler View for drag-and-drop & editing sizes
        adapter = WidgetEditAdapter()
        recyclerView = RecyclerView(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 0.6f)
            layoutManager = LinearLayoutManager(this@WidgetsGridEditActivity)
            this.adapter = this@WidgetsGridEditActivity.adapter
        }
        mainLayout.addView(recyclerView)

        // Add Widget Button
        val btnAdd = Button(this).apply {
            text = "Add Widget"
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                topMargin = 16
            }
            setOnClickListener {
                saveItems() 
                val intent = Intent(this@WidgetsGridEditActivity, WidgetPickerActivity::class.java).apply {
                    putExtra("ACTION_TYPE", "ADD_TO_WIDGETS_GRID")
                    putExtra("PAGE_ID", pageId)
                }
                startActivity(intent)
            }
        }
        mainLayout.addView(btnAdd)

        setContentView(mainLayout)
        setupItemTouchHelper()
        
        registerReceiver(receiver, android.content.IntentFilter("WIDGET_ADDED_TO_GRID"), Context.RECEIVER_NOT_EXPORTED)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        try { unregisterReceiver(receiver) } catch (e: Exception) {}
    }

    private fun updatePreview() {
        previewContainer.removeAllViews()
        val pagePreview = WidgetsGridPageView(this, pageId) {}
        previewContainer.addView(pagePreview)
    }

    private fun loadLocalItems() {
        val jsonStr = prefs.getString("widgets_grid_$pageId", "[]") ?: "[]"
        val arr = JSONArray(jsonStr)
        localItems.clear()
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i)
            if (obj != null) {
                localItems.add(GridWidgetItem(
                    obj.getInt("id"),
                    obj.optInt("cols", 2),
                    obj.optInt("rows", 2)
                ))
            } else {
                val id = arr.optInt(i, -1)
                if (id != -1) {
                    localItems.add(GridWidgetItem(id, 2, 2))
                }
            }
        }
    }

    private fun saveItems() {
        val arr = JSONArray()
        localItems.forEach { 
            val obj = JSONObject()
            obj.put("id", it.id)
            obj.put("cols", it.cols)
            obj.put("rows", it.rows)
            arr.put(obj)
        }
        prefs.edit().putString("widgets_grid_$pageId", arr.toString()).apply()
        
        // Notify grid to update
        val intent = Intent("WIDGET_ADDED_TO_GRID")
        intent.putExtra("PAGE_ID", pageId)
        sendBroadcast(intent)
        
        updatePreview()
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "WIDGET_ADDED_TO_GRID") {
                loadLocalItems()
                adapter.notifyDataSetChanged()
                updatePreview()
            }
        }
    }

    private fun setupItemTouchHelper() {
        val callback = object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPos = viewHolder.adapterPosition
                val toPos = target.adapterPosition
                Collections.swap(localItems, fromPos, toPos)
                adapter.notifyItemMoved(fromPos, toPos)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
            override fun isLongPressDragEnabled(): Boolean = true
            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)
                saveItems()
            }
        }
        ItemTouchHelper(callback).attachToRecyclerView(recyclerView)
    }

    inner class WidgetEditAdapter : RecyclerView.Adapter<WidgetEditAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvName: TextView = view.findViewById(1)
            val btnRemove: ImageView = view.findViewById(2)
            val dragHandle: ImageView = view.findViewById(3)
            
            val tvCols: TextView = view.findViewById(4)
            val btnColMinus: Button = view.findViewById(5)
            val btnColPlus: Button = view.findViewById(6)
            
            val tvRows: TextView = view.findViewById(7)
            val btnRowMinus: Button = view.findViewById(8)
            val btnRowPlus: Button = view.findViewById(9)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val layout = LinearLayout(this@WidgetsGridEditActivity).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins(0, 0, 0, 16) }
                setPadding(16, 16, 16, 16)
                setBackgroundColor(Color.parseColor("#33FFFFFF"))
            }
            
            val topRow = LinearLayout(this@WidgetsGridEditActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                gravity = Gravity.CENTER_VERTICAL
            }
            
            val dragHandle = ImageView(this@WidgetsGridEditActivity).apply {
                id = 3
                setImageResource(android.R.drawable.ic_menu_sort_by_size)
                setColorFilter(Color.WHITE)
                setPadding(16, 16, 16, 16)
            }
            
            val tvName = TextView(this@WidgetsGridEditActivity).apply {
                id = 1
                setTextColor(Color.WHITE)
                textSize = 16f
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                    marginStart = 16
                }
            }
            
            val btnRemove = ImageView(this@WidgetsGridEditActivity).apply {
                id = 2
                setImageResource(android.R.drawable.ic_menu_delete)
                setColorFilter(Color.RED)
                setPadding(16, 16, 16, 16)
            }
            
            topRow.addView(dragHandle)
            topRow.addView(tvName)
            topRow.addView(btnRemove)
            layout.addView(topRow)
            
            // Editors Row
            val editorsRow = LinearLayout(this@WidgetsGridEditActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                gravity = Gravity.CENTER_VERTICAL
                setPadding(16, 16, 16, 0)
            }
            
            // Cols Editor
            val colsLayout = LinearLayout(this@WidgetsGridEditActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                gravity = Gravity.CENTER_VERTICAL
            }
            colsLayout.addView(TextView(this@WidgetsGridEditActivity).apply { text = "Cols: "; setTextColor(Color.LTGRAY) })
            
            val btnColMinus = Button(this@WidgetsGridEditActivity).apply { id = 5; text = "-"; textSize = 10f; layoutParams = LinearLayout.LayoutParams(100, 100) }
            val tvCols = TextView(this@WidgetsGridEditActivity).apply { id = 4; setTextColor(Color.WHITE); setPadding(16, 0, 16, 0) }
            val btnColPlus = Button(this@WidgetsGridEditActivity).apply { id = 6; text = "+"; textSize = 10f; layoutParams = LinearLayout.LayoutParams(100, 100) }
            
            colsLayout.addView(btnColMinus)
            colsLayout.addView(tvCols)
            colsLayout.addView(btnColPlus)
            
            // Rows Editor
            val rowsLayout = LinearLayout(this@WidgetsGridEditActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                gravity = Gravity.CENTER_VERTICAL
            }
            rowsLayout.addView(TextView(this@WidgetsGridEditActivity).apply { text = "Rows: "; setTextColor(Color.LTGRAY) })
            
            val btnRowMinus = Button(this@WidgetsGridEditActivity).apply { id = 8; text = "-"; textSize = 10f; layoutParams = LinearLayout.LayoutParams(100, 100) }
            val tvRows = TextView(this@WidgetsGridEditActivity).apply { id = 7; setTextColor(Color.WHITE); setPadding(16, 0, 16, 0) }
            val btnRowPlus = Button(this@WidgetsGridEditActivity).apply { id = 9; text = "+"; textSize = 10f; layoutParams = LinearLayout.LayoutParams(100, 100) }
            
            rowsLayout.addView(btnRowMinus)
            rowsLayout.addView(tvRows)
            rowsLayout.addView(btnRowPlus)
            
            editorsRow.addView(colsLayout)
            editorsRow.addView(rowsLayout)
            layout.addView(editorsRow)

            return ViewHolder(layout)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = localItems[position]
            val info = appWidgetManager.getAppWidgetInfo(item.id)
            
            holder.tvName.text = info?.loadLabel(packageManager) ?: "Widget ${item.id} (Unknown)"
            
            holder.tvCols.text = item.cols.toString()
            holder.tvRows.text = item.rows.toString()
            
            holder.btnColMinus.setOnClickListener {
                if (item.cols > 1) {
                    item.cols--
                    holder.tvCols.text = item.cols.toString()
                    saveItems()
                }
            }
            holder.btnColPlus.setOnClickListener {
                item.cols++
                holder.tvCols.text = item.cols.toString()
                saveItems()
            }
            
            holder.btnRowMinus.setOnClickListener {
                if (item.rows > 1) {
                    item.rows--
                    holder.tvRows.text = item.rows.toString()
                    saveItems()
                }
            }
            holder.btnRowPlus.setOnClickListener {
                item.rows++
                holder.tvRows.text = item.rows.toString()
                saveItems()
            }
            
            holder.btnRemove.setOnClickListener {
                val pos = holder.bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    val removed = localItems.removeAt(pos)
                    notifyItemRemoved(pos)
                    saveItems()
                    
                    try {
                        AppWidgetHelper.getHost(this@WidgetsGridEditActivity).deleteAppWidgetId(removed.id)
                    } catch (e: Exception) {}
                }
            }
        }

        override fun getItemCount(): Int = localItems.size
    }
}
"""

with open('app/src/main/java/com/example/WidgetsGridEditActivity.kt', 'w') as f:
    f.write(content)
