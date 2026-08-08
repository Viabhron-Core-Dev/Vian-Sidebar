import sys

with open('app/src/main/java/com/example/service/WidgetsGridEditOverlayView.kt', 'r') as f:
    content = f.read()

# Replace imports
content = content.replace('import androidx.recyclerview.widget.LinearLayoutManager', 
                          'import androidx.recyclerview.widget.LinearLayoutManager\nimport androidx.recyclerview.widget.GridLayoutManager\nimport android.graphics.drawable.BitmapDrawable\nimport android.graphics.Bitmap\nimport android.graphics.Canvas\nimport android.graphics.drawable.Drawable\n')

# Replace RecyclerView initialization
target_rv = """        recyclerView = RecyclerView(context).apply {
            layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f)
            layoutManager = LinearLayoutManager(context)
            this.adapter = this@WidgetsGridEditOverlayView.adapter
        }"""
replacement_rv = """        recyclerView = RecyclerView(context).apply {
            layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f)
            val columns = prefs.getInt("sidebar_columns", 3)
            layoutManager = GridLayoutManager(context, columns)
            this.adapter = this@WidgetsGridEditOverlayView.adapter
        }"""
content = content.replace(target_rv, replacement_rv)

# Replace WidgetEditAdapter
target_adapter = """    inner class WidgetEditAdapter : RecyclerView.Adapter<WidgetEditAdapter.ViewHolder>() {
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvName: TextView = view.findViewById(1)
            val btnRemove: ImageView = view.findViewById(2)
            val dragHandle: ImageView = view.findViewById(3)
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val layout = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins(0, 0, 0, 8) }
                gravity = Gravity.CENTER_VERTICAL
                setPadding(16, 24, 16, 24)
                setBackgroundColor(Color.parseColor("#33FFFFFF"))
            }

            val dragHandle = ImageView(context).apply {
                id = 3
                setImageResource(android.R.drawable.ic_menu_sort_by_size)
                setColorFilter(Color.WHITE)
                setPadding(16, 16, 16, 16)
            }

            val tvName = TextView(context).apply {
                id = 1
                setTextColor(Color.WHITE)
                textSize = 18f
                layoutParams = LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f).apply {
                    marginStart = 16
                }
            }

            val btnRemove = ImageView(context).apply {
                id = 2
                setImageResource(android.R.drawable.ic_menu_delete)
                setColorFilter(Color.RED)
                setPadding(16, 16, 16, 16)
            }

            layout.addView(dragHandle)
            layout.addView(tvName)
            layout.addView(btnRemove)

            return ViewHolder(layout)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val widgetId = localIds[position]
            val info = appWidgetManager.getAppWidgetInfo(widgetId)
            
            holder.tvName.text = info?.loadLabel(context.packageManager) ?: "Widget $widgetId (Unknown)"
            holder.btnRemove.setOnClickListener {
                val pos = holder.adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    val removedId = localIds.removeAt(pos)
                    notifyItemRemoved(pos)
                    saveIds()
                    
                    // Also delete from host
                    val host = AppWidgetHelper.getHost(context)
                    host.deleteAppWidgetId(removedId)
                }
            }
        }
        override fun getItemCount(): Int = localIds.size
    }"""
    
replacement_adapter = """    inner class WidgetEditAdapter : RecyclerView.Adapter<WidgetEditAdapter.ViewHolder>() {
        inner class ViewHolder(val view: LinearLayout) : RecyclerView.ViewHolder(view) {
            val icon = ImageView(context)
            val label = TextView(context)
            val removeBadge = ImageView(context)
            
            init {
                view.orientation = LinearLayout.VERTICAL
                view.gravity = Gravity.CENTER
                val density = context.resources.displayMetrics.density
                val size = (48 * density).toInt()
                
                icon.layoutParams = LinearLayout.LayoutParams(size, size)
                icon.scaleType = ImageView.ScaleType.FIT_CENTER
                
                label.setTextColor(Color.WHITE)
                label.textSize = 10f
                label.gravity = Gravity.CENTER
                label.maxLines = 2
                label.setPadding(0, 8, 0, 0)
                
                val iconContainer = FrameLayout(context).apply {
                    layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                }
                
                removeBadge.apply {
                    setImageResource(android.R.drawable.ic_menu_delete)
                    setColorFilter(Color.RED)
                    layoutParams = FrameLayout.LayoutParams((20 * density).toInt(), (20 * density).toInt()).apply {
                        gravity = Gravity.TOP or Gravity.END
                    }
                    setPadding(4, 4, 4, 4)
                    setBackgroundColor(Color.parseColor("#88FFFFFF"))
                }
                
                iconContainer.addView(icon)
                iconContainer.addView(removeBadge)
                
                view.addView(iconContainer)
                view.addView(label)
                
                view.layoutParams = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    setMargins(0, 16, 0, 16)
                }
            }
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LinearLayout(context))
        }

        private fun drawableToBitmap(drawable: Drawable): Bitmap {
            if (drawable is BitmapDrawable) {
                if (drawable.bitmap != null) {
                    return drawable.bitmap
                }
            }
            val bitmap = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
                Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
            } else {
                Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            }
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            return bitmap
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val widgetId = localIds[position]
            val info = appWidgetManager.getAppWidgetInfo(widgetId)
            
            holder.label.text = info?.loadLabel(context.packageManager) ?: "Widget $widgetId"
            if (info != null) {
                val iconDrawable = info.loadIcon(context, context.resources.displayMetrics.densityDpi)
                if (iconDrawable != null) {
                    holder.icon.setImageBitmap(drawableToBitmap(iconDrawable))
                } else {
                    holder.icon.setImageResource(android.R.mipmap.sym_def_app_icon)
                }
            } else {
                holder.icon.setImageResource(android.R.mipmap.sym_def_app_icon)
            }
            
            holder.removeBadge.setOnClickListener {
                val pos = holder.adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    val removedId = localIds.removeAt(pos)
                    notifyItemRemoved(pos)
                    saveIds()
                    
                    // Also delete from host
                    val host = AppWidgetHelper.getHost(context)
                    host.deleteAppWidgetId(removedId)
                }
            }
        }
        override fun getItemCount(): Int = localIds.size
    }"""
    
content = content.replace(target_adapter, replacement_adapter)

with open('app/src/main/java/com/example/service/WidgetsGridEditOverlayView.kt', 'w') as f:
    f.write(content)
