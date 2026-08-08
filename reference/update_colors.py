import re

with open('app/src/main/java/com/example/HandleEditScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
    '                    "#80FF5252", "#804CAF50", "#802196F3", "#80FFEB3B", "#8087CEEB"',
    '                    "#80FF5252", "#804CAF50", "#802196F3", "#80FFEB3B", "#8087CEEB", "#1d2962ff"'
)

with open('app/src/main/java/com/example/HandleEditScreen.kt', 'w') as f:
    f.write(content)
