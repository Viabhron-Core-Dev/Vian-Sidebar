filepath = 'app/src/main/java/com/example/feature/settings/AddElementActivity.kt'
with open(filepath, 'r') as f:
    content = f.read()

old_default_actions = """        addHeader("Default actions")
        addItem(android.R.drawable.ic_menu_agenda, "App") {
            // Stubbed
        }
        addItem(android.R.drawable.ic_menu_share, "Shortcut") {
            // Stubbed
        }
        addItem(android.R.drawable.ic_menu_manage, "Intent") {
            // startActivityForResult(Intent(this, IntentPickerActivity::class.java), 301)
        }
        addItem(android.R.drawable.ic_menu_gallery, "Widget") {
            // Stubbed
        }
        
        addItem(android.R.drawable.ic_menu_gallery, "Popup Widget") {
            // Stubbed
        }"""

new_default_actions = """        addHeader("Default actions")
        addItem(android.R.drawable.ic_menu_agenda, "App") {
            startActivityForResult(Intent(this, AppPickerActivity::class.java), 200)
        }
        addItem(android.R.drawable.ic_menu_share, "Shortcut") {
            startActivityForResult(Intent(this, ShortcutPickerActivity::class.java), 300)
        }
        addItem(android.R.drawable.ic_menu_manage, "Intent") {
            startActivityForResult(Intent(this, IntentPickerActivity::class.java), 301)
        }
        addItem(android.R.drawable.ic_menu_gallery, "Widget") {
            val intent = Intent(this, com.example.WidgetPickerActivity::class.java).apply {
                putExtra("ACTION_TYPE", "RETURN_ID")
            }
            startActivityForResult(intent, 400)
        }
        
        addItem(android.R.drawable.ic_menu_gallery, "Popup Widget") {
            val intent = Intent(this, com.example.WidgetPickerActivity::class.java).apply {
                putExtra("ACTION_TYPE", "RETURN_ID")
            }
            startActivityForResult(intent, 500)
        }"""
content = content.replace(old_default_actions, new_default_actions)

old_link = """        addItem(android.R.drawable.ic_menu_set_as, "Link") {
            // Need a link picker in future, for now just create dummy link
            val uuid = java.util.UUID.randomUUID().toString()
            val linkJson = JSONObject().apply {
                put("url", "https://google.com")
                put("label", "Google")
            }
            finishWithId("link:$uuid:${linkJson.toString()}")
        }"""

new_link = """        addItem(android.R.drawable.ic_menu_set_as, "Link") {
            val layout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
            }
            val titleInput = android.widget.EditText(this).apply {
                hint = "Title (e.g. Google)"
            }
            val urlInput = android.widget.EditText(this).apply {
                hint = "URL (e.g. https://google.com)"
            }
            layout.addView(titleInput)
            layout.addView(urlInput)
            
            android.app.AlertDialog.Builder(this)
                .setTitle("Add Link")
                .setView(layout)
                .setPositiveButton("OK") { _, _ ->
                    val titleStr = titleInput.text.toString().takeIf { it.isNotEmpty() } ?: "Link"
                    val urlStr = urlInput.text.toString().takeIf { it.isNotEmpty() } ?: "https://"
                    val uuid = java.util.UUID.randomUUID().toString()
                    val linkJson = JSONObject().apply {
                        put("url", urlStr)
                        put("label", titleStr)
                    }
                    finishWithId("link:$uuid:${linkJson.toString()}")
                }
                .setNegativeButton("Cancel", null)
                .show()
        }"""
content = content.replace(old_link, new_link)

with open(filepath, 'w') as f:
    f.write(content)
print("Updated AddElementActivity.kt")
