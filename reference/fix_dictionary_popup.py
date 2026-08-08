import re

with open("app/src/main/java/com/example/service/DictionaryPopupActivity.kt", "r") as f:
    content = f.read()

content = content.replace("import androidx.compose.material.icons.filled.PlayArrow", "import androidx.compose.material.icons.filled.PlayArrow\nimport androidx.compose.material.icons.filled.OpenInNew")

old_row = """                                if (query.isNotEmpty()) {
                                    IconButton(
                                        onClick = {
                                            tts?.speak(query, TextToSpeech.QUEUE_FLUSH, null, null)
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Read Aloud",
                                            tint = Color.White
                                        )
                                    }
                                }"""

new_row = """                                if (query.isNotEmpty()) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = {
                                                tts?.speak(query, TextToSpeech.QUEUE_FLUSH, null, null)
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = "Read Aloud",
                                                tint = Color.White
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                val i = Intent(this@DictionaryPopupActivity, SidebarService::class.java)
                                                i.action = "OPEN_DICTIONARY"
                                                i.putExtra("QUERY", query)
                                                startService(i)
                                                finish()
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.OpenInNew,
                                                contentDescription = "Open in Floating Dictionary",
                                                tint = Color.White
                                            )
                                        }
                                    }
                                }"""

content = content.replace(old_row, new_row)

with open("app/src/main/java/com/example/service/DictionaryPopupActivity.kt", "w") as f:
    f.write(content)
