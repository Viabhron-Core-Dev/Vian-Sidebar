with open("app/src/main/java/com/example/service/DictionaryWindowManager.kt", "r") as f:
    lines = f.readlines()

new_method = """    fun searchWord(query: String) {
        if (floatingView == null) {
            show(false)
        }
        val etSearch = floatingView?.findViewById<android.widget.EditText>(R.id.et_search)
        if (isFolded) {
            val bubbleIcon = floatingView?.findViewById<android.widget.ImageView>(R.id.bubble_icon)
            bubbleIcon?.performClick()
        }
        etSearch?.setText(query)
        
        scope.launch {
            val activeDict = prefs.getString("active_dict", "English") ?: "English"
            val results = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                db.dictionaryDao().searchWords("$query%", activeDict)
            }
            if (results.isNotEmpty()) {
                val entry = results.first()
                selectedEntry = entry
                floatingView?.findViewById<android.widget.LinearLayout>(R.id.search_layout)?.visibility = android.view.View.GONE
                floatingView?.findViewById<android.widget.LinearLayout>(R.id.detail_layout)?.visibility = android.view.View.VISIBLE
                floatingView?.findViewById<android.widget.TextView>(R.id.tv_word)?.text = entry.word
                floatingView?.findViewById<android.widget.TextView>(R.id.tv_definition)?.text = androidx.core.text.HtmlCompat.fromHtml(entry.definition, androidx.core.text.HtmlCompat.FROM_HTML_MODE_COMPACT)
            }
        }
    }
"""

lines.insert(-1, new_method)

with open("app/src/main/java/com/example/service/DictionaryWindowManager.kt", "w") as f:
    f.writelines(lines)
