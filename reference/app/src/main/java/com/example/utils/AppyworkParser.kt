package com.example.utils

import java.util.UUID

data class ParsedCodeBlock(
    val id: String = UUID.randomUUID().toString(),
    var filePath: String?, // Null if quarantined
    val code: String,
    var isQuarantined: Boolean = filePath == null
)

object AppyworkParser {
    fun parseText(rawText: String): List<ParsedCodeBlock> {
        val blocks = mutableListOf<ParsedCodeBlock>()
        
        val blockRegex = Regex("```[a-zA-Z]*\\n([\\s\\S]*?)```")
        val matches = blockRegex.findAll(rawText)
        
        var searchStartIndex = 0
        
        for (match in matches) {
            val code = match.groupValues[1].trim()
            val beforeBlock = rawText.substring(searchStartIndex, match.range.first)
            searchStartIndex = match.range.last + 1
            
            val path = extractFilePath(beforeBlock, code)
            blocks.add(ParsedCodeBlock(filePath = path, code = code))
        }
        
        return blocks
    }
    
    private fun extractFilePath(textBefore: String, code: String): String? {
        // Look at the last few lines before the code block
        val linesBefore = textBefore.lines().reversed().take(5)
        for (line in linesBefore) {
            val cleanLine = line.replace("*", "").replace("`", "").replace("#", "").trim(' ', ':')
            val fileRegex = Regex("(?i)(?:file|path):\\s*([\\w./-]+)")
            val match = fileRegex.find(cleanLine)
            if (match != null) {
                return match.groupValues[1]
            }
            if (cleanLine.isNotBlank() && cleanLine.contains("/") && !cleanLine.contains(" ")) {
                return cleanLine
            }
            // Catch simple filenames like "index.html" if they are clearly filenames
            if (cleanLine.matches(Regex("^[\\w-]+\\.[a-zA-Z0-9]+$"))) {
                return cleanLine
            }
        }
        
        // Fallback: Check first line of the code block itself (e.g. // File: path/to/file)
        val firstCodeLine = code.lines().firstOrNull()?.trim() ?: ""
        val pathRegex = Regex("(?i)(?:file|path):\\s*([\\w./-]+)")
        val match = pathRegex.find(firstCodeLine)
        if (match != null) {
            return match.groupValues[1]
        }
        
        return null
    }
}
