import re

with open('app/src/main/java/com/example/service/SidebarEditOverlayView.kt', 'r') as f:
    content = f.read()

ui_old = """        val btnAdd = Button(context).apply {
            text = "Add"
            setOnClickListener { onAddClicked() }
        }
        val btnReset = Button(context).apply {
            text = "Empty"
            setOnClickListener {
                localIds.clear()
                refresh()
            }
        }
        val btnSave = Button(context).apply {
            text = "Save"
            setOnClickListener { saveAndClose() }
        }
        val btnCancel = Button(context).apply {
            text = "Cancel"
            setOnClickListener { close() }
        }

        buttonsLayout.addView(btnAdd)
        buttonsLayout.addView(btnReset)
        buttonsLayout.addView(btnSave)
        buttonsLayout.addView(btnCancel)

        rootLayout.addView(buttonsLayout)"""

ui_new = """        titleView = TextView(context).apply {
            text = "Edit Sidebar"
            setTextColor(Color.WHITE)
            textSize = 20f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 16)
        }
        rootLayout.addView(titleView)

        val btnAdd = Button(context).apply {
            text = "Add"
            setOnClickListener { onAddClicked() }
        }
        btnReset = Button(context).apply {
            text = "Empty"
            setOnClickListener {
                localIds.clear()
                refresh()
            }
        }
        btnBack = Button(context).apply {
            text = "Back"
            visibility = View.GONE
            setOnClickListener { exitFolder() }
        }
        val btnSave = Button(context).apply {
            text = "Save"
            setOnClickListener { saveAndClose() }
        }
        val btnCancel = Button(context).apply {
            text = "Cancel"
            setOnClickListener { close() }
        }

        buttonsLayout.addView(btnAdd)
        buttonsLayout.addView(btnReset!!)
        buttonsLayout.addView(btnBack!!)
        buttonsLayout.addView(btnSave)
        buttonsLayout.addView(btnCancel)

        rootLayout.addView(buttonsLayout)"""

content = content.replace(ui_old, ui_new)

with open('app/src/main/java/com/example/service/SidebarEditOverlayView.kt', 'w') as f:
    f.write(content)
