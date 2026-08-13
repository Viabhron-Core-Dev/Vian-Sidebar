import re
with open('app/src/main/java/com/example/feature/sidebar/SchedulerPageView.kt', 'r') as f:
    text = f.read()

text = text.replace('import kotlinx.coroutines.Dispatchers', 'import kotlinx.coroutines.Dispatchers\nimport kotlinx.coroutines.Job')

with open('app/src/main/java/com/example/feature/sidebar/SchedulerPageView.kt', 'w') as f:
    f.write(text)
