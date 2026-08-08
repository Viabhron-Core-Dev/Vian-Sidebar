import re

with open("app/src/main/java/com/example/service/SidebarAppsManager.kt", "r") as f:
    content = f.read()

# Remove the bindIcon method
start = content.find("fun bindIcon(id: String")
if start != -1:
    end = content.find("fun getIconBitmap(id: String)")
    content = content[:start] + content[end:]
    
with open("app/src/main/java/com/example/service/SidebarAppsManager.kt", "w") as f:
    f.write(content)
