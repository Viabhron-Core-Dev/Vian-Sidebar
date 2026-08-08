import re

with open('app/src/main/java/com/example/service/AppsPageView.kt', 'r') as f:
    content = f.read()

content = content.replace('onCloseSidebar()', 'currentFolderPopup?.dismiss()\n                        onCloseSidebar()')

with open('app/src/main/java/com/example/service/AppsPageView.kt', 'w') as f:
    f.write(content)

print("Added dismiss to onCloseSidebar calls.")
