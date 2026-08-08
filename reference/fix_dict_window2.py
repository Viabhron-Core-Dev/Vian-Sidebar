with open("app/src/main/java/com/example/service/DictionaryWindowManager.kt", "r") as f:
    content = f.read()

content = content.replace("private var tts: TextToSpeech? = null", "private var tts: TextToSpeech? = null\n    private var selectedEntry: DictionaryEntry? = null")
content = content.replace("var selectedEntry: DictionaryEntry? = null", "")

with open("app/src/main/java/com/example/service/DictionaryWindowManager.kt", "w") as f:
    f.write(content)
