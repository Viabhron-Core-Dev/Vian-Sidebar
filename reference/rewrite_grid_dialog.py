with open('app/src/main/java/com/example/service/SidebarEditOverlayView.kt', 'r') as f:
    lines = f.readlines()

new_lines = lines[:560]

new_dialog = """private fun showGridSizeDialog(
    context: Context,
    item: SidebarItem.Folder,
    manager: SidebarAppsManager,
    onGridSizeSelected: (Int, Int) -> Unit
) {
    val layout = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(40, 40, 40, 40)
        setBackgroundColor(android.graphics.Color.WHITE)
    }

    val title = TextView(context).apply {
        text = "Popup Grid Size"
        textSize = 18f
        typeface = android.graphics.Typeface.DEFAULT_BOLD
        setTextColor(android.graphics.Color.BLACK)
        setPadding(0, 0, 0, 20)
    }
    layout.addView(title)
    
    val desc = TextView(context).apply {
        text = "0 = auto calculate based on items"
        textSize = 12f
        setTextColor(android.graphics.Color.DKGRAY)
        setPadding(0, 0, 0, 20)
    }
    layout.addView(desc)

    val rowColContainer = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER
    }
    layout.addView(rowColContainer)

    var currentCols = item.popupColumns
    var currentRows = item.popupRows

    // Function to create a spinner-like column/row picker
    fun createPicker(label: String, initialValue: Int, onValueChanged: (Int) -> Unit): LinearLayout {
        val picker = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(20, 0, 20, 0)
        }
        val labelView = TextView(context).apply {
            text = label
            textSize = 14f
            setTextColor(android.graphics.Color.BLACK)
            gravity = Gravity.CENTER
        }
        picker.addView(labelView)

        val btnUp = android.widget.ImageButton(context).apply {
            setImageResource(android.R.drawable.arrow_up_float)
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            setColorFilter(android.graphics.Color.DKGRAY)
        }
        picker.addView(btnUp)

        val valueView = TextView(context).apply {
            text = initialValue.toString()
            textSize = 24f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setTextColor(android.graphics.Color.BLACK)
            gravity = Gravity.CENTER
        }
        picker.addView(valueView)

        val btnDown = android.widget.ImageButton(context).apply {
            setImageResource(android.R.drawable.arrow_down_float)
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            setColorFilter(android.graphics.Color.DKGRAY)
        }
        picker.addView(btnDown)

        var v = initialValue
        btnUp.setOnClickListener {
            v++
            valueView.text = v.toString()
            onValueChanged(v)
        }
        btnDown.setOnClickListener {
            if (v > 0) {
                v--
                valueView.text = v.toString()
                onValueChanged(v)
            }
        }
        return picker
    }

    rowColContainer.addView(createPicker("Columns", currentCols) { currentCols = it })
    rowColContainer.addView(createPicker("Rows", currentRows) { currentRows = it })

    val dialog = android.app.AlertDialog.Builder(context)
        .setView(layout)
        .setPositiveButton("Save") { _, _ ->
            onGridSizeSelected(currentCols, currentRows)
        }
        .setNegativeButton("Cancel", null)
        .create()

    dialog.window?.setType(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE)
    dialog.show()
}
"""

with open('app/src/main/java/com/example/service/SidebarEditOverlayView.kt', 'w') as f:
    f.writelines(new_lines)
    f.write(new_dialog)
