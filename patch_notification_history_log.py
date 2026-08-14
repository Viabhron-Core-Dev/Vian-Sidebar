filepath = 'app/src/main/java/com/example/NotificationHistoryActivity.kt'
with open(filepath, 'r') as f:
    content = f.read()

content = content.replace("com.example.com.example.core.LogKeeper", "com.example.core.LogKeeper")

with open(filepath, 'w') as f:
    f.write(content)
print("NotificationHistoryActivity patched")
