import re

with open('app/src/main/java/com/example/service/FolderStyleDialog.kt', 'r') as f:
    content = f.read()

old_code = """    gridView.adapter = adapter
    layout.addView(gridView, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))

    val dialog = AlertDialog.Builder(context, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert)
        .setView(layout)
        .setPositiveButton("OK", null)
        .create()

    gridView.setOnItemClickListener { _, _, position, _ ->
        if (onStyleSelected != null) {
            onStyleSelected(position)
        } else {
            val json = org.json.JSONObject().apply {
                put("name", item.name)
                put("colorHex", item.colorHex)
                val jArr = org.json.JSONArray()
                item.items.forEach { jArr.put(it) }
                put("items", jArr)
                put("folderStyle", position)
            }
            manager.removeItem(item.id)
            manager.addItem("folder:${item.uuid}:$json")
        }
        dialog.dismiss()
    }"""

new_code = """    gridView.adapter = adapter
    layout.addView(gridView, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))

    var selectedStyle = item.folderStyle

    gridView.setOnItemClickListener { _, _, position, _ ->
        selectedStyle = position
        adapter.notifyDataSetChanged()
    }
    
    val colsTitle = TextView(context).apply {
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

    val dialog = AlertDialog.Builder(context, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert)
        .setView(layout)
        .setPositiveButton("Save") { _, _ ->
            val colsStr = colsInput.text.toString()
            val cols = if (colsStr.isNotEmpty()) colsStr.toInt() else 0
            
            if (onStyleSelected != null) {
                onStyleSelected(selectedStyle, cols)
            } else {
                val json = org.json.JSONObject().apply {
                    put("name", item.name)
                    put("colorHex", item.colorHex)
                    val jArr = org.json.JSONArray()
                    item.items.forEach { jArr.put(it) }
                    put("items", jArr)
                    put("folderStyle", selectedStyle)
                    put("popupColumns", cols)
                }
                manager.removeItem(item.id)
                manager.addItem("folder:${item.uuid}:$json")
            }
        }
        .setNegativeButton("Cancel", null)
        .create()"""

content = content.replace(old_code, new_code)

old_adapter = """                if (position == item.folderStyle) {
                    setTypeface(null, Typeface.BOLD)
                }"""

new_adapter = """                if (position == selectedStyle) {
                    setTypeface(null, Typeface.BOLD)
                }"""
                
content = content.replace(old_adapter, new_adapter)

with open('app/src/main/java/com/example/service/FolderStyleDialog.kt', 'w') as f:
    f.write(content)
