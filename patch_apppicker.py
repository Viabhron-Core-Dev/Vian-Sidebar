filepath = 'app/src/main/java/com/example/feature/settings/AppPickerActivity.kt'
with open(filepath, 'r') as f:
    content = f.read()

content = content.replace("import com.example.service.SidebarAppsManager", "import com.example.feature.sidebar.SidebarAppsManager\nimport com.example.feature.sidebar.AppInfo")
content = content.replace("com.example.service.AppInfo", "AppInfo")

with open(filepath, 'w') as f:
    f.write(content)
