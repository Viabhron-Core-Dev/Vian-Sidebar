import re

with open('app/src/main/java/com/example/feature/miniapps/DictionaryPageView.kt', 'r') as f:
    content = f.read()

content = content.replace('val results = withContext(Dispatchers.IO) {', 'val results: List<DictionaryEntry> = withContext(Dispatchers.IO) {')
with open('app/src/main/java/com/example/feature/miniapps/DictionaryPageView.kt', 'w') as f:
    f.write(content)

with open('app/src/main/java/com/example/feature/miniapps/CompassPageView.kt', 'r') as f:
    content = f.read()

content = content.replace('import com.example.service.LogKeeper', 'import com.example.core.LogKeeper')
if 'import com.example.core.LogKeeper' not in content:
    content = content.replace('import android.hardware.SensorManager', 'import android.hardware.SensorManager\nimport com.example.core.LogKeeper')

with open('app/src/main/java/com/example/feature/miniapps/CompassPageView.kt', 'w') as f:
    f.write(content)

