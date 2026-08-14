filepath = 'app/src/main/java/com/example/feature/settings/IntentPickerActivity.kt'
with open(filepath, 'r') as f:
    content = f.read()

old_block = """                            val resultIntent = Intent().apply {
                                putExtra("LABEL", label)
                                putExtra("URI", uri)
                            }
                            setResult(RESULT_OK, resultIntent)"""

new_block = """                            import java.net.URLEncoder
                            val encodedLabel = URLEncoder.encode(label, "UTF-8")
                            val encodedUri = URLEncoder.encode(uri, "UTF-8")
                            val id = "intent:$encodedLabel:$encodedUri"
                            val resultIntent = Intent().apply {
                                putExtra("ELEMENT_ID", id)
                            }
                            setResult(RESULT_OK, resultIntent)"""

content = content.replace(old_block, new_block)

with open(filepath, 'w') as f:
    f.write(content)
print("IntentPickerActivity patched")
