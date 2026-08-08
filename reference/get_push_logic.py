import re

with open('app/src/main/java/com/example/service/AppyworkWindowManager.kt', 'r') as f:
    content = f.read()

match = re.search(r'pushStatus = "Uploading \$\{unsyncedFiles.size\} files..."(.*?)pushStatus = "Creating tree..."', content, re.DOTALL)
if match:
    print(match.group(0))
