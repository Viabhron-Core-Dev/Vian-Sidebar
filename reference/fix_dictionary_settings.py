import re

with open("app/src/main/java/com/example/DictionarySettingsScreen.kt", "r") as f:
    content = f.read()

old_vars = """    var activeDict by remember { mutableStateOf(prefs.getString("active_dict", "English") ?: "English") }
    val scope = rememberCoroutineScope()"""

new_vars = """    var activeDict by remember { mutableStateOf(prefs.getString("active_dict", "English") ?: "English") }
    var fontScale by remember { mutableStateOf(prefs.getFloat("dict_font_size_scale", 1.0f)) }
    val scope = rememberCoroutineScope()"""

content = content.replace(old_vars, new_vars)

old_ui = """                        Divider()
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                "You can import StarDict dictionaries (.idx and .dict/.dict.dz wrapped in a .zip). Search GitHub for 'StarDict dictionaries' to find compatible files.",
                modifier = Modifier.padding(16.dp),"""

new_ui = """                        Divider()
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            ListItem(
                headlineContent = { Text("Font Size Scale") },
                supportingContent = {
                    Slider(
                        value = fontScale,
                        onValueChange = { 
                            fontScale = it
                            prefs.edit().putFloat("dict_font_size_scale", it).apply()
                        },
                        valueRange = 0.5f..2.5f,
                        steps = 19
                    )
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "You can import StarDict dictionaries (.idx and .dict/.dict.dz wrapped in a .zip). Search GitHub for 'StarDict dictionaries' to find compatible files.",
                modifier = Modifier.padding(16.dp),"""

content = content.replace(old_ui, new_ui)

with open("app/src/main/java/com/example/DictionarySettingsScreen.kt", "w") as f:
    f.write(content)
