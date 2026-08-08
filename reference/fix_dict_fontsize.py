import re
with open("app/src/main/java/com/example/service/DictionaryWindowManager.kt", "r") as f:
    content = f.read()

old_init = """        val btnBack = floatingView!!.findViewById<Button>(R.id.btn_back)
        val tvWord = floatingView!!.findViewById<TextView>(R.id.tv_word)
        val tvDefinition = floatingView!!.findViewById<TextView>(R.id.tv_definition)
        val btnSpeakWord = floatingView!!.findViewById<ImageView>(R.id.btn_speak_word)
        val btnSettings = floatingView!!.findViewById<ImageView>(R.id.btn_settings)"""

new_init = """        val btnBack = floatingView!!.findViewById<Button>(R.id.btn_back)
        val tvWord = floatingView!!.findViewById<TextView>(R.id.tv_word)
        val tvDefinition = floatingView!!.findViewById<TextView>(R.id.tv_definition)
        val btnSpeakWord = floatingView!!.findViewById<ImageView>(R.id.btn_speak_word)
        val btnSettings = floatingView!!.findViewById<ImageView>(R.id.btn_settings)
        
        val fontScale = prefs.getFloat("dict_font_size_scale", 1.0f)
        tvDefinition.textSize = 16f * fontScale
        tvWord.textSize = 20f * fontScale"""

content = content.replace(old_init, new_init)

with open("app/src/main/java/com/example/service/DictionaryWindowManager.kt", "w") as f:
    f.write(content)
