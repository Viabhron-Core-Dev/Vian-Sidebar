import re

with open('app/src/main/java/com/example/DictionarySettingsScreen.kt', 'r') as f:
    content = f.read()

# Add TranslationSettingsSection(context) to the end of the Column
content = re.sub(
    r'(Text\(\s*"You can import StarDict dictionaries.*?)\n        }\n    }\n}',
    r'\1\n            Spacer(modifier = Modifier.height(16.dp))\n            TranslationSettingsSection(context)\n        }\n    }\n}',
    content, count=1, flags=re.DOTALL)

with open('app/src/main/java/com/example/DictionarySettingsScreen.kt', 'w') as f:
    f.write(content)
