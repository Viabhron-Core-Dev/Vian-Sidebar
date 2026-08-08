import re

with open("app/src/main/java/com/example/service/HybridGridPageView.kt", "r") as f:
    content = f.read()

# Revert grid items bindIcon
target1 = """                            appsManager.bindIcon(item.id, icon, prefs, CoroutineScope(Dispatchers.Main)) {
                                // For folders, we need to refresh the view to show the loaded mini icons
                                appsManager.bindIcon(item.id, icon, prefs, CoroutineScope(Dispatchers.Main)) {}
                            }"""
replacement1 = """                            CoroutineScope(Dispatchers.Main).launch {
                                val bmp = appsManager.getIconBitmap(item.id)
                                if (bmp != null) {
                                    icon.setImageBitmap(bmp)
                                }
                            }"""
content = content.replace(target1, replacement1)

# Revert popup popupWindow and popupView layout logic
target2 = """        val maxCols = if (folder.popupColumns > 0) folder.popupColumns else prefs.getInt("sidebar_columns", 3)
        val columns = if (folder.items.size <= maxCols && folder.items.isNotEmpty()) folder.items.size else maxCols
        val validCols = if (columns > 0) columns else 1
        gridLayout.columnCount = validCols
        val padding = (16 * density).toInt()
        gridLayout.setPadding(padding, padding, padding, padding)
        popupView.addView(gridLayout, FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT))

        val popupOpacity = prefs.getFloat("sidebar_transparency", 0.9f)
        val popupBg = android.graphics.drawable.GradientDrawable()
        popupBg.setColor(Color.parseColor("#1A1A1A"))
        popupBg.alpha = (popupOpacity * 255).toInt()
        popupBg.cornerRadius = 16 * density
        popupView.background = popupBg

        // Calculate exact size for compact wrap_content appearance
        val itemWidthDp = 72
        val itemHeightDp = 72
        val autoRows = Math.ceil(folder.items.size.toDouble() / validCols).toInt()
        val rows = if (folder.popupRows > 0) kotlin.math.min(folder.popupRows, autoRows) else autoRows
        val displayRows = if (folder.popupRows > 0) folder.popupRows else rows

        val totalWidth = (validCols * itemWidthDp * density + padding * 2).toInt()
        val totalHeight = (displayRows * itemHeightDp * density + padding * 2).toInt()
        
        popupView.layoutParams = ViewGroup.LayoutParams(totalWidth, totalHeight)

        var popupWindow: PopupWindow? = null
        for (itemId in folder.items) {
            val parsed = appsManager.parseId(itemId) ?: continue
            val elementView = android.view.LayoutInflater.from(context).inflate(com.example.R.layout.item_sidebar_app, null, false)
            val icon = elementView.findViewById<android.widget.ImageView>(com.example.R.id.app_icon)
            val label = elementView.findViewById<android.widget.TextView>(com.example.R.id.app_label)
            label.text = parsed.label
            
            appsManager.bindIcon(itemId, icon, prefs, CoroutineScope(Dispatchers.Main)) {}
            
            elementView.setOnClickListener {
                if (parsed is SidebarItem.App) {
                    val intent = context.packageManager.getLaunchIntentForPackage(parsed.packageName)
                    if (intent != null) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        try { context.startActivity(intent) } catch (e: Exception) {}
                        popupWindow?.dismiss()
                    }
                } else if (parsed is SidebarItem.Link) {
                    try {
                        val intent = if (parsed.url.startsWith("intent:")) {
                            Intent.parseUri(parsed.url, Intent.URI_INTENT_SCHEME)
                        } else {
                            Intent(Intent.ACTION_VIEW, android.net.Uri.parse(parsed.url))
                        }
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                        popupWindow?.dismiss()
                    } catch (e: Exception) {}
                }
            }
            
            elementView.setOnLongClickListener {
                val actionList = mutableListOf<String>()
                if (parsed is SidebarItem.App) {
                    actionList.add("App Info")
                }
                actionList.add("Change Icon")
                val customIconFile = java.io.File(context.filesDir, "custom_icons/${itemId.replace(Regex("[^a-zA-Z0-9.-]"), "_")}.webp")
                if (customIconFile.exists()) {
                    actionList.add("Reset Icon")
                }
                actionList.add("Remove")
                
                var actionMenuPopup: PopupWindow? = null
                val popupLayout = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    val pad = (8 * density).toInt()
                    setPadding(pad, pad, pad, pad)
                }
                
                actionList.forEach { action ->
                    val actionView = TextView(context).apply {
                        text = action
                        setTextColor(Color.BLACK)
                        val padV = (10 * density).toInt()
                        val padH = (16 * density).toInt()
                        setPadding(padH, padV, padH, padV)
                        textSize = 14f
                        
                        val shape = android.graphics.drawable.GradientDrawable()
                        shape.shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                        shape.cornerRadius = 24f * density
                        shape.setColor(Color.WHITE)
                        shape.setStroke(1, Color.LTGRAY)
                        background = shape
                        
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins(0, 0, 0, (8 * density).toInt())
                        }
                        
                        setOnClickListener {
                            actionMenuPopup?.dismiss()
                            when (action) {
                                "Remove" -> appsManager.removeItem(itemId)
                                "Change Icon" -> {
                                    val intent = Intent(context, com.example.IconPickerActivity::class.java).apply {
                                        putExtra("item_id", itemId)
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                    popupWindow?.dismiss()
                                }
                                "Reset Icon" -> {
                                    val file = java.io.File(context.filesDir, "custom_icons/${itemId.replace(Regex("[^a-zA-Z0-9.-]"), "_")}.webp")
                                    if (file.exists()) file.delete()
                                    appsManager.iconCache.remove("custom_${itemId}")
                                    appsManager.iconCache.remove(itemId)
                                    context.sendBroadcast(Intent("com.example.UPDATE_SIDEBAR_ICONS").apply {
                                        putExtra("item_id", itemId)
                                    })
                                    popupWindow?.dismiss()
                                }
                                "App Info" -> {
                                    if (parsed is SidebarItem.App) {
                                        try {
                                            val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                            intent.data = android.net.Uri.parse("package:${parsed.packageName}")
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            context.startActivity(intent)
                                            popupWindow?.dismiss()
                                        } catch (e: Exception) {}
                                    }
                                }
                            }
                        }
                    }
                    popupLayout.addView(actionView)
                }
                
                actionMenuPopup = PopupWindow(popupLayout, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true).apply {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        windowLayoutType = android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                    } else {
                        @Suppress("DEPRECATION")
                        windowLayoutType = android.view.WindowManager.LayoutParams.TYPE_PHONE
                    }
                    setBackgroundDrawable(android.graphics.drawable.ColorDrawable(Color.TRANSPARENT))
                    elevation = 8f * density
                }
                
                val loc = IntArray(2)
                elementView.getLocationOnScreen(loc)
                actionMenuPopup?.showAtLocation(elementView, Gravity.NO_GRAVITY, loc[0] + elementView.width / 4, loc[1] + elementView.height / 2)
                
                true
            }
            
            val params = android.widget.GridLayout.LayoutParams()
            params.width = (72 * density).toInt()
            params.height = (72 * density).toInt()
            gridLayout.addView(elementView, params)
        }"""
        
replacement2 = """        val maxCols = if (folder.popupColumns > 0) folder.popupColumns else prefs.getInt("sidebar_columns", 3)
        val validCols = if (maxCols > 0) maxCols else 1
        gridLayout.columnCount = validCols
        val padding = (16 * density).toInt()
        gridLayout.setPadding(padding, padding, padding, padding)
        popupView.addView(gridLayout, FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT))

        val popupOpacity = prefs.getFloat("sidebar_transparency", 0.9f)
        val popupBg = android.graphics.drawable.GradientDrawable()
        popupBg.setColor(Color.parseColor("#1A1A1A"))
        popupBg.alpha = (popupOpacity * 255).toInt()
        popupBg.cornerRadius = 16 * density
        popupView.background = popupBg

        var popupWindow: PopupWindow? = null
        for (itemId in folder.items) {
            val parsed = appsManager.parseId(itemId) ?: continue
            val elementView = android.view.LayoutInflater.from(context).inflate(com.example.R.layout.item_sidebar_app, null, false)
            val icon = elementView.findViewById<android.widget.ImageView>(com.example.R.id.app_icon)
            val label = elementView.findViewById<android.widget.TextView>(com.example.R.id.app_label)
            label.text = parsed.label
            CoroutineScope(Dispatchers.Main).launch {
                val bmp = appsManager.getIconBitmap(itemId)
                if (bmp != null) icon.setImageBitmap(bmp)
            }
            elementView.setOnClickListener {
                if (parsed is SidebarItem.App) {
                    val intent = context.packageManager.getLaunchIntentForPackage(parsed.packageName)
                    if (intent != null) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        try { context.startActivity(intent) } catch (e: Exception) {}
                        popupWindow?.dismiss()
                    }
                } else if (parsed is SidebarItem.Link) {
                    try {
                        val intent = if (parsed.url.startsWith("intent:")) {
                            Intent.parseUri(parsed.url, Intent.URI_INTENT_SCHEME)
                        } else {
                            Intent(Intent.ACTION_VIEW, android.net.Uri.parse(parsed.url))
                        }
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                        popupWindow?.dismiss()
                    } catch (e: Exception) {}
                }
            }
            
            val params = android.widget.GridLayout.LayoutParams()
            params.width = (72 * density).toInt()
            params.height = (72 * density).toInt()
            gridLayout.addView(elementView, params)
        }

        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val totalWidth = popupView.measuredWidth
        val totalHeight = popupView.measuredHeight"""

content = content.replace(target1, replacement1)
content = content.replace(target2, replacement2)

with open("app/src/main/java/com/example/service/HybridGridPageView.kt", "w") as f:
    f.write(content)
