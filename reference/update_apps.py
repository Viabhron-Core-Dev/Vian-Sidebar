import re

with open('app/src/main/java/com/example/service/AppsPageView.kt', 'r') as f:
    content = f.read()

old_calc = """        // Calculate exact size for compact wrap_content appearance
        val itemWidthDp = 56 // 44dp icon + 6dp padding on each side
        val itemHeightDp = 56
        val rows = Math.ceil(folderItems.size.toDouble() / validCols).toInt()
        val totalWidth = (validCols * itemWidthDp * density + padding * 2).toInt()
        val totalHeight = (rows * itemHeightDp * density + padding * 2).toInt()
        
        val popupWindow = android.widget.PopupWindow(
            popupView,
            totalWidth,
            totalHeight,
            true
        )"""

new_calc = """        // Calculate exact size for compact wrap_content appearance
        val itemWidthDp = 56 // 44dp icon + 6dp padding on each side
        val itemHeightDp = 56
        val autoRows = Math.ceil(folderItems.size.toDouble() / validCols).toInt()
        val rows = if (folder.popupRows > 0) minOf(folder.popupRows, autoRows) else autoRows
        val displayRows = if (folder.popupRows > 0) folder.popupRows else rows
        
        val totalWidth = (validCols * itemWidthDp * density + padding * 2).toInt()
        val totalHeight = (displayRows * itemHeightDp * density + padding * 2).toInt()
        
        val popupWindow = android.widget.PopupWindow(
            popupView,
            totalWidth,
            totalHeight,
            true
        )"""
content = content.replace(old_calc, new_calc)

with open('app/src/main/java/com/example/service/AppsPageView.kt', 'w') as f:
    f.write(content)
