import re

with open('app/src/main/java/com/example/service/AppsPageView.kt', 'r') as f:
    content = f.read()

popup_code = """
    private var currentFolderPopup: android.widget.PopupWindow? = null

    private fun showFolderPopup(anchor: View, folder: SidebarItem.Folder) {
        currentFolderPopup?.dismiss()
        
        val density = context.resources.displayMetrics.density
        val popupView = FrameLayout(context)
        val recyclerView = RecyclerView(context)
        val padding = (8 * density).toInt()
        recyclerView.setPadding(padding, padding, padding, padding)
        popupView.addView(recyclerView, FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT))
        
        val folderItems = mutableListOf<SidebarItem>()
        for (itemId in folder.items) {
            val parsedItem = manager.parseId(itemId)
            if (parsedItem != null) {
                folderItems.add(parsedItem)
            }
        }
        
        val maxCols = prefs.getInt("sidebar_columns", 4)
        val columns = if (folderItems.size <= maxCols && folderItems.isNotEmpty()) folderItems.size else maxCols
        val validCols = if (columns > 0) columns else 1
        
        recyclerView.layoutManager = GridLayoutManager(context, validCols)
        val popupAdapter = AppsAdapter(folderItems)
        recyclerView.adapter = popupAdapter
        
        val popupOpacity = prefs.getFloat("sidebar_transparency", 0.9f)
        val popupBg = android.graphics.drawable.GradientDrawable()
        val cHex = try { android.graphics.Color.parseColor(folder.colorHex) } catch(e:Exception){ android.graphics.Color.parseColor("#333333") }
        // Use folder color or sidebar color? User said "same color and transparency as sidebar". 
        // We'll use dark gray which is default sidebar color, or #1A1A1A
        popupBg.setColor(android.graphics.Color.parseColor("#1A1A1A"))
        popupBg.alpha = (popupOpacity * 255).toInt()
        popupBg.cornerRadius = 16 * density
        popupView.background = popupBg
        
        val popupWindow = android.widget.PopupWindow(popupView, 
            ViewGroup.LayoutParams.WRAP_CONTENT, 
            ViewGroup.LayoutParams.WRAP_CONTENT, 
            true)
            
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            popupWindow.elevation = 10f * density
        }
        popupWindow.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
        
        currentFolderPopup = popupWindow
        
        // When an item in the popup is clicked, the adapter calls onCloseSidebar. 
        // We need to also close the popup. 
        // Since AppsAdapter uses onCloseSidebar, we can just intercept it or let it close the whole sidebar.
        // If we want it to close the popup, we can dismiss it here. The adapter calls onCloseSidebar() which closes the sidebar.
        // Wait, onCloseSidebar() is fine.
        
        // Show next to anchor
        popupWindow.showAsDropDown(anchor, 0, (4 * density).toInt())
    }
"""

# Insert popup_code before `private inner class AppsAdapter`
content = content.replace('private inner class AppsAdapter', popup_code.strip() + '\n\n    private inner class AppsAdapter')

with open('app/src/main/java/com/example/service/AppsPageView.kt', 'w') as f:
    f.write(content)

print("Added showFolderPopup.")
