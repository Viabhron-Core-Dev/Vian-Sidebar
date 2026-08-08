import re

with open('app/src/main/java/com/example/service/AppyworkWindowManager.kt', 'r') as f:
    content = f.read()

target_ui = """    Column(modifier = Modifier.fillMaxSize()) {
        Text("Select Project to Apply Changes:", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 8.dp))"""

replacement_ui = """    Column(modifier = Modifier.fillMaxSize()) {
        Text("Parsed Blocks: ${parsedBlocks.size}", style = MaterialTheme.typography.titleMedium)
        val validBlocks = parsedBlocks.count { !it.isQuarantined && it.filePath != null }
        Text("Valid: $validBlocks | Quarantined: ${parsedBlocks.size - validBlocks}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 16.dp))

        Text("Select Project to Apply Changes:", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 8.dp))"""

if target_ui in content:
    content = content.replace(target_ui, replacement_ui)
    
    # Also add import kotlinx.coroutines.launch
    if "import kotlinx.coroutines.launch" not in content:
        content = content.replace("import kotlin.math.max", "import kotlin.math.max\nimport kotlinx.coroutines.launch")
        
    with open('app/src/main/java/com/example/service/AppyworkWindowManager.kt', 'w') as f:
        f.write(content)
    print("Patched UI successfully")
else:
    print("Target UI not found")
