import re

with open('app/src/main/java/com/example/service/FolderStyleDialog.kt', 'r') as f:
    content = f.read()

old_code = """    val colsTitle = TextView(context).apply {
        text = "Popup Columns (0 = Default)"
        textSize = 16f
        typeface = Typeface.DEFAULT_BOLD
        setTextColor(Color.BLACK)
        setPadding(0, 40, 0, 10)
    }
    layout.addView(colsTitle)
    
    val colsInput = android.widget.EditText(context).apply {
        inputType = android.text.InputType.TYPE_CLASS_NUMBER
        setText(item.popupColumns.toString())
    }
    layout.addView(colsInput)
"""

new_code = ""

content = content.replace(old_code, new_code)

old_code2 = """            val colsStr = colsInput.text.toString()
            val cols = if (colsStr.isNotEmpty()) colsStr.toInt() else 0
            
            if (onStyleSelected != null) {
                onStyleSelected(selectedStyle, cols)
            } else {"""

new_code2 = """            if (onStyleSelected != null) {
                onStyleSelected(selectedStyle, item.popupColumns)
            } else {"""

content = content.replace(old_code2, new_code2)

old_code3 = """                    put("folderStyle", selectedStyle)
                    put("popupColumns", cols)
                }
                manager.removeItem(item.id)"""

new_code3 = """                    put("folderStyle", selectedStyle)
                    put("popupColumns", item.popupColumns)
                }
                manager.removeItem(item.id)"""

content = content.replace(old_code3, new_code3)

with open('app/src/main/java/com/example/service/FolderStyleDialog.kt', 'w') as f:
    f.write(content)
