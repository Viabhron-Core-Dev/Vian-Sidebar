import re

with open('app/src/main/java/com/example/utils/AppyworkParser.kt', 'r') as f:
    content = f.read()

target = """    private fun extractFilePath(textBefore: String, code: String): String? {
        // Look at the last few lines before the code block
        val linesBefore = textBefore.lines().reversed().take(5)
        for (line in linesBefore) {
            val cleanLine = line.replace("*", "").replace("`", "").replace("#", "").trim(' ', ':')
            if (cleanLine.isNotBlank() && cleanLine.contains("/") && !cleanLine.contains(" ")) {
                return cleanLine
            }
            // Catch simple filenames like "index.html" if they are clearly filenames
            if (cleanLine.matches(Regex("^[\\w-]+\\.[a-zA-Z0-9]+$"))) {
                return cleanLine
            }
        }"""

replacement = """    private fun extractFilePath(textBefore: String, code: String): String? {
        // Look at the last few lines before the code block
        val linesBefore = textBefore.lines().reversed().take(5)
        for (line in linesBefore) {
            val cleanLine = line.replace("*", "").replace("`", "").replace("#", "").trim(' ', ':')
            val fileRegex = Regex("(?i)(?:file|path):\\\\s*([\\\\w./-]+)")
            val match = fileRegex.find(cleanLine)
            if (match != null) {
                return match.groupValues[1]
            }
            if (cleanLine.isNotBlank() && cleanLine.contains("/") && !cleanLine.contains(" ")) {
                return cleanLine
            }
            // Catch simple filenames like "index.html" if they are clearly filenames
            if (cleanLine.matches(Regex("^[\\\\w-]+\\\\.[a-zA-Z0-9]+$"))) {
                return cleanLine
            }
        }"""

if target in content:
    content = content.replace(target, replacement)
    print("Patched parser")
else:
    print("Target parser not found")

with open('app/src/main/java/com/example/utils/AppyworkParser.kt', 'w') as f:
    f.write(content)

