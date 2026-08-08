with open("app/src/main/java/com/example/service/DictionaryWindowManager.kt", "r") as f:
    lines = f.readlines()

for i, line in enumerate(lines):
    if "btnSpeakWord.setOnClickListener" in line:
        start_idx = i
        break

# Let's see lines[start_idx:start_idx+10]
print("Before fix:", "".join(lines[start_idx:start_idx+10]))

# Find the stray }
for i in range(start_idx+3, start_idx+10):
    if lines[i].strip() == "}":
        print(f"Removing stray '}}' at line {i+1}")
        del lines[i]
        break

# Also let's re-add btnSettings.setOnClickListener
insert_code = """        btnSettings.setOnClickListener {
            val intent = android.content.Intent(context, com.example.SettingsActivity::class.java)
            intent.putExtra("START_ROUTE", "dict")
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
"""
lines.insert(start_idx+3, insert_code)

with open("app/src/main/java/com/example/service/DictionaryWindowManager.kt", "w") as f:
    f.writelines(lines)
