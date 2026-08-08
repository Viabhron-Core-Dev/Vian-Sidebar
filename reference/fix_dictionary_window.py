import re

with open("app/src/main/java/com/example/service/DictionaryWindowManager.kt", "r") as f:
    content = f.read()

# Replace HTML parsing in tvDefinition
content = content.replace("import androidx.recyclerview.widget.RecyclerView", "import androidx.recyclerview.widget.RecyclerView\nimport androidx.core.text.HtmlCompat\nimport android.content.Intent\nimport com.example.SettingsActivity")

old_init = """        val btnBack = floatingView!!.findViewById<Button>(R.id.btn_back)
        val tvWord = floatingView!!.findViewById<TextView>(R.id.tv_word)
        val tvDefinition = floatingView!!.findViewById<TextView>(R.id.tv_definition)
        val btnSpeakWord = floatingView!!.findViewById<ImageView>(R.id.btn_speak_word)
        val btnSpeakDef = floatingView!!.findViewById<View>(R.id.btn_speak_def)"""

new_init = """        val btnBack = floatingView!!.findViewById<Button>(R.id.btn_back)
        val tvWord = floatingView!!.findViewById<TextView>(R.id.tv_word)
        val tvDefinition = floatingView!!.findViewById<TextView>(R.id.tv_definition)
        val btnSpeakWord = floatingView!!.findViewById<ImageView>(R.id.btn_speak_word)
        val btnSettings = floatingView!!.findViewById<ImageView>(R.id.btn_settings)"""

content = content.replace(old_init, new_init)

old_text_set = """            searchLayout.visibility = View.GONE
            detailLayout.visibility = View.VISIBLE
            tvWord.text = entry.word
            tvDefinition.text = entry.definition"""

new_text_set = """            searchLayout.visibility = View.GONE
            detailLayout.visibility = View.VISIBLE
            tvWord.text = entry.word
            tvDefinition.text = HtmlCompat.fromHtml(entry.definition, HtmlCompat.FROM_HTML_MODE_COMPACT)"""

content = content.replace(old_text_set, new_text_set)

old_clicks = """        btnSpeakWord.setOnClickListener {
            selectedEntry?.let { tts?.speak(it.word, TextToSpeech.QUEUE_FLUSH, null, null) }
        }
        btnSpeakDef.setOnClickListener {
            selectedEntry?.let { tts?.speak(it.definition, TextToSpeech.QUEUE_FLUSH, null, null) }
        }"""

new_clicks = """        btnSpeakWord.setOnClickListener {
            selectedEntry?.let { tts?.speak(it.word, TextToSpeech.QUEUE_FLUSH, null, null) }
        }
        btnSettings.setOnClickListener {
            val intent = Intent(context, SettingsActivity::class.java)
            intent.putExtra("START_ROUTE", "dict")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }"""

content = content.replace(old_clicks, new_clicks)

# Add searchWord method
new_method = """    fun searchWord(query: String) {
        if (floatingView == null) {
            show(false)
        }
        val etSearch = floatingView?.findViewById<EditText>(R.id.et_search)
        if (isFolded) {
            val bubbleIcon = floatingView?.findViewById<ImageView>(R.id.bubble_icon)
            bubbleIcon?.performClick()
        }
        etSearch?.setText(query)
        
        scope.launch {
            val activeDict = prefs.getString("active_dict", "English") ?: "English"
            val results = withContext(Dispatchers.IO) {
                db.dictionaryDao().searchWords("$query%", activeDict)
            }
            if (results.isNotEmpty()) {
                val entry = results.first()
                floatingView?.findViewById<LinearLayout>(R.id.search_layout)?.visibility = View.GONE
                floatingView?.findViewById<LinearLayout>(R.id.detail_layout)?.visibility = View.VISIBLE
                floatingView?.findViewById<TextView>(R.id.tv_word)?.text = entry.word
                floatingView?.findViewById<TextView>(R.id.tv_definition)?.text = HtmlCompat.fromHtml(entry.definition, HtmlCompat.FROM_HTML_MODE_COMPACT)
                
                // Need to set selectedEntry somehow... we can just do it via the adapter, 
                // but since we are modifying state outside the adapter scope, we should
                // let the adapter's onClick do it. Let's just simulate the click or update manually.
                
                // For simplicity, we just set the text. The TTS btnSpeakWord won't have selectedEntry, 
                // wait, selectedEntry is a local var in show(). Let's make it a class member.
            }
        }
    }"""

# Wait, `selectedEntry` is a local variable in `show()`. Let's elevate it to a class member.
with open("app/src/main/java/com/example/service/DictionaryWindowManager.kt", "w") as f:
    f.write(content)
