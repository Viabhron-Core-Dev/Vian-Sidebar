import re
with open("app/src/main/java/com/example/service/DictionaryWindowManager.kt", "r") as f:
    content = f.read()

old_search = """                val entry = results.first()
                selectedEntry = entry
                floatingView?.findViewById<android.widget.LinearLayout>(R.id.search_layout)?.visibility = android.view.View.GONE
                floatingView?.findViewById<android.widget.LinearLayout>(R.id.detail_layout)?.visibility = android.view.View.VISIBLE
                floatingView?.findViewById<android.widget.TextView>(R.id.tv_word)?.text = entry.word
                floatingView?.findViewById<android.widget.TextView>(R.id.tv_definition)?.text = androidx.core.text.HtmlCompat.fromHtml(entry.definition, androidx.core.text.HtmlCompat.FROM_HTML_MODE_COMPACT)
            }"""

new_search = """                val entry = results.first()
                selectedEntry = entry
                floatingView?.findViewById<android.widget.LinearLayout>(R.id.search_layout)?.visibility = android.view.View.GONE
                floatingView?.findViewById<android.widget.LinearLayout>(R.id.detail_layout)?.visibility = android.view.View.VISIBLE
                
                val fontScale = prefs.getFloat("dict_font_size_scale", 1.0f)
                val tvWord = floatingView?.findViewById<android.widget.TextView>(R.id.tv_word)
                val tvDefinition = floatingView?.findViewById<android.widget.TextView>(R.id.tv_definition)
                tvWord?.textSize = 20f * fontScale
                tvDefinition?.textSize = 16f * fontScale
                tvWord?.text = entry.word
                tvDefinition?.text = androidx.core.text.HtmlCompat.fromHtml(entry.definition, androidx.core.text.HtmlCompat.FROM_HTML_MODE_COMPACT)
            }"""

content = content.replace(old_search, new_search)

with open("app/src/main/java/com/example/service/DictionaryWindowManager.kt", "w") as f:
    f.write(content)
