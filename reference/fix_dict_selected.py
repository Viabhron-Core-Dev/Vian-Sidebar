with open("app/src/main/java/com/example/service/DictionaryWindowManager.kt", "r") as f:
    lines = f.readlines()

# find where tts is
for i, line in enumerate(lines):
    if "private var tts: TextToSpeech? = null" in line:
        lines.insert(i + 1, "    private var selectedEntry: DictionaryEntry? = null\n")
        break

# remove repeated private if any
lines = [l for l in lines if l.strip() != "private"]

with open("app/src/main/java/com/example/service/DictionaryWindowManager.kt", "w") as f:
    f.writelines(lines)
