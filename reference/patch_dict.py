import re

with open('app/src/main/java/com/example/service/DictionaryWindowManager.kt', 'r') as f:
    content = f.read()

# Insert the tab setup code right after inflating layout
injection = """
        val tabDict = floatingView!!.findViewById<TextView>(R.id.tab_dictionary)
        val tabTrans = floatingView!!.findViewById<TextView>(R.id.tab_translate)
        val dictArea = floatingView!!.findViewById<View>(R.id.dict_content_area)
        val transArea = floatingView!!.findViewById<View>(R.id.translate_content_area)

        fun selectTab(isDict: Boolean) {
            if (isDict) {
                tabDict.setBackgroundColor(android.graphics.Color.parseColor("#333344"))
                tabDict.setTextColor(android.graphics.Color.WHITE)
                tabTrans.setBackgroundColor(android.graphics.Color.parseColor("#222233"))
                tabTrans.setTextColor(android.graphics.Color.parseColor("#888888"))
                dictArea.visibility = View.VISIBLE
                transArea.visibility = View.GONE
            } else {
                tabTrans.setBackgroundColor(android.graphics.Color.parseColor("#333344"))
                tabTrans.setTextColor(android.graphics.Color.WHITE)
                tabDict.setBackgroundColor(android.graphics.Color.parseColor("#222233"))
                tabDict.setTextColor(android.graphics.Color.parseColor("#888888"))
                dictArea.visibility = View.GONE
                transArea.visibility = View.VISIBLE
            }
        }
        tabDict.setOnClickListener { selectTab(true) }
        tabTrans.setOnClickListener { selectTab(false) }
        selectTab(!initialTabTranslate)

        // ML Kit Translation Setup
        val translationPrefs = context.getSharedPreferences("TranslationPrefs", Context.MODE_PRIVATE)
        val allLanguages = TranslateLanguage.getAllLanguages()
        val languageNames = allLanguages.map { Locale(it).displayLanguage }
        
        val sourceLangSpinner = floatingView!!.findViewById<Spinner>(R.id.spinner_source)
        val targetLangSpinner = floatingView!!.findViewById<Spinner>(R.id.spinner_target)
        val sourceTextEdit = floatingView!!.findViewById<EditText>(R.id.edit_source)
        val targetTextOut = floatingView!!.findViewById<TextView>(R.id.text_target)
        val readSourceBtn = floatingView!!.findViewById<ImageView>(R.id.btn_read_source)
        val readTargetBtn = floatingView!!.findViewById<ImageView>(R.id.btn_read_target)
        
        val langAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, languageNames)
        langAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        val sourceOptions = listOf("Auto Detect") + languageNames
        val sourceAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, sourceOptions)
        sourceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        
        sourceLangSpinner.adapter = sourceAdapter
        targetLangSpinner.adapter = langAdapter
        
        val defaultTarget = translationPrefs.getString("default_target_lang", TranslateLanguage.ENGLISH) ?: TranslateLanguage.ENGLISH
        val targetIndex = allLanguages.indexOf(defaultTarget)
        if (targetIndex >= 0) {
            targetLangSpinner.setSelection(targetIndex)
        }

        fun performTranslation(text: String, srcLang: String, targetLang: String) {
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(srcLang)
                .setTargetLanguage(targetLang)
                .build()
            val translator = Translation.getClient(options)
            val conditions = DownloadConditions.Builder().build()
            translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener {
                    translator.translate(text)
                        .addOnSuccessListener { targetTextOut.text = it }
                        .addOnFailureListener { targetTextOut.text = "Error translating text." }
                }
                .addOnFailureListener { targetTextOut.text = "Error downloading model." }
        }

        fun translateText() {
            val text = sourceTextEdit.text.toString()
            if (text.isEmpty()) {
                targetTextOut.text = ""
                return
            }
            val srcPos = sourceLangSpinner.selectedItemPosition
            val targetPos = targetLangSpinner.selectedItemPosition
            val targetLang = allLanguages[targetPos]
            if (srcPos == 0) {
                LanguageIdentification.getClient().identifyLanguage(text)
                    .addOnSuccessListener { lc ->
                        performTranslation(text, if (lc == "und") TranslateLanguage.ENGLISH else lc, targetLang)
                    }
            } else {
                performTranslation(text, allLanguages[srcPos - 1], targetLang)
            }
        }
        
        sourceTextEdit.addTextChangedListener { translateText() }
        val onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (parent == targetLangSpinner) {
                    translationPrefs.edit().putString("default_target_lang", allLanguages[position]).apply()
                }
                translateText()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        sourceLangSpinner.onItemSelectedListener = onItemSelectedListener
        targetLangSpinner.onItemSelectedListener = onItemSelectedListener
        
        readSourceBtn.setOnClickListener {
            val text = sourceTextEdit.text.toString()
            if (text.isNotEmpty() && tts != null) {
                val srcPos = sourceLangSpinner.selectedItemPosition
                val lang = if (srcPos == 0) TranslateLanguage.ENGLISH else allLanguages[srcPos - 1]
                tts?.language = Locale(lang)
                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
            }
        }
        readTargetBtn.setOnClickListener {
            val text = targetTextOut.text.toString()
            if (text.isNotEmpty() && tts != null) {
                tts?.language = Locale(allLanguages[targetLangSpinner.selectedItemPosition])
                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
            }
        }
        
        sourceTextEdit.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                layoutParams?.let {
                    it.flags = it.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv()
                    windowManager.updateViewLayout(floatingView, it)
                }
            }
            false
        }
"""

content = re.sub(
    r'(com.example.utils.ActiveAppTracker.addApp\("dictionary", "Dictionary", "Tool", 25\))',
    r'\1\n' + injection,
    content
)

with open('app/src/main/java/com/example/service/DictionaryWindowManager.kt', 'w') as f:
    f.write(content)
