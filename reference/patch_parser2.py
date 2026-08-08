import re

with open('app/src/main/java/com/example/utils/AppyworkParser.kt', 'r') as f:
    content = f.read()

target = """            val cleanLine = line.replace("*", "").replace("`", "").replace("#", "").trim(' ', ':')
            if (cleanLine.isNotBlank() && cleanLine.contains("/") && !cleanLine.contains(" ")) {"""

replacement = """            val cleanLine = line.replace("*", "").replace("`", "").replace("#", "").trim(' ', ':')
            val fileRegex = Regex("(?i)(?:file|path):\\\\s*([\\\\w./-]+)")
            val match = fileRegex.find(cleanLine)
            if (match != null) {
                return match.groupValues[1]
            }
            if (cleanLine.isNotBlank() && cleanLine.contains("/") && !cleanLine.contains(" ")) {"""

if target in content:
    content = content.replace(target, replacement)
    print("Patched parser again")
else:
    print("Target parser again not found")

with open('app/src/main/java/com/example/utils/AppyworkParser.kt', 'w') as f:
    f.write(content)

