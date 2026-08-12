with open('app/src/main/java/com/example/feature/settings/AddElementActivity.kt', 'r') as f:
    text = f.read()

text = text.replace("""        addItem(android.R.drawable.ic_menu_manage, "Intent") {
//             startActivityForResult(Intent(this, IntentPickerActivity::class.java), 301)
        }""", """        addItem(android.R.drawable.ic_menu_manage, "Intent") {
            // startActivityForResult(Intent(this, IntentPickerActivity::class.java), 301)
        }""")

text = text.replace("""        addItem(android.R.drawable.ic_menu_gallery, "Widget") {
//             val intent = Intent(this, WidgetPickerActivity::class.java).apply {
                putExtra("ACTION_TYPE", "RETURN_ID")
            }
            startActivityForResult(intent, 400)
        }""", """        addItem(android.R.drawable.ic_menu_gallery, "Widget") {
            // Stubbed
        }""")

text = text.replace("""        addItem(android.R.drawable.ic_menu_gallery, "Popup Widget") {
//             val intent = Intent(this, WidgetPickerActivity::class.java).apply {
                putExtra("ACTION_TYPE", "RETURN_ID")
            }
            startActivityForResult(intent, 500)
        }""", """        addItem(android.R.drawable.ic_menu_gallery, "Popup Widget") {
            // Stubbed
        }""")

with open('app/src/main/java/com/example/feature/settings/AddElementActivity.kt', 'w') as f:
    f.write(text)
