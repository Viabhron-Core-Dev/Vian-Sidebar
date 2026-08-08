with open("app/src/main/java/com/example/HandleEditScreen.kt", "r") as f:
    content = f.read()

import re

old_state = """    var colorHex by remember { mutableStateOf(prefs.getString("${prefix}color", if (handleId == "reader") "#44102d42" else "#3318304A") ?: "#3318304A") }
    var shape by remember { mutableStateOf(prefs.getString("${prefix}shape", if (handleId == "reader") "half_oval" else "triangle") ?: "triangle") }"""

new_state = """    var colorHex by remember { mutableStateOf(prefs.getString("${prefix}color", if (handleId == "reader") "#44102d42" else "#3318304A") ?: "#3318304A") }
    var shape by remember { mutableStateOf(prefs.getString("${prefix}shape", if (handleId == "reader") "half_oval" else "triangle") ?: "triangle") }
    var edge by remember { mutableStateOf(prefs.getString("${prefix}edge", "right") ?: "right") }"""

content = content.replace(old_state, new_state)

old_ui = """            Text("Y Position: ${yPos.toInt()}")
            Slider(value = yPos, onValueChange = {"""

new_ui = """            Text("Edge Position:")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("left", "right").forEach { s ->
                    FilterChip(
                        selected = edge == s,
                        onClick = { 
                            edge = s
                            prefs.edit().putString("${prefix}edge", s).apply()
                        },
                        label = { Text(s.replaceFirstChar { it.uppercase() }) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            Text("Y Position: ${yPos.toInt()}")
            Slider(value = yPos, onValueChange = {"""

content = content.replace(old_ui, new_ui)

with open("app/src/main/java/com/example/HandleEditScreen.kt", "w") as f:
    f.write(content)
