import re

files_to_fix = [
    'app/src/main/java/com/example/feature/sidebar/NotificationPageView.kt',
    'app/src/main/java/com/example/feature/sidebar/SchedulerPageView.kt',
]

for file_path in files_to_fix:
    with open(file_path, 'r') as f:
        text = f.read()
    
    text = text.replace('import com.example.R', 'import com.example.R\nimport com.example.core.LogKeeper\nimport com.example.feature.settings.TagManagementActivity')
    
    with open(file_path, 'w') as f:
        f.write(text)

