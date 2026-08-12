import os
import glob

def replace_package(file_path, new_pkg):
    with open(file_path, 'r') as f:
        content = f.read()
    
    # replace package com.example.* with new_pkg
    import re
    content = re.sub(r'package com\.example.*?(\n|$)', f'package {new_pkg}\n\n', content)
    
    # fix imports
    content = content.replace('import com.example.service.SidebarService', 'import com.example.service.SidebarService')
    content = content.replace('import com.example.service.', 'import com.example.feature.miniapps.')
    content = content.replace('import com.example.feature.miniapps.SidebarService', 'import com.example.service.SidebarService')
    
    with open(file_path, 'w') as f:
        f.write(content)

for f in glob.glob("app/src/main/java/com/example/feature/miniapps/translation/*.kt"):
    replace_package(f, "com.example.feature.miniapps.translation")

replace_package("app/src/main/java/com/example/feature/miniapps/DictionaryPopupActivity.kt", "com.example.feature.miniapps")

for f in glob.glob("app/src/main/java/com/example/feature/miniapps/reader/*.kt"):
    replace_package(f, "com.example.feature.miniapps.reader")

for f in glob.glob("app/src/main/java/com/example/feature/miniapps/browser/*.kt"):
    replace_package(f, "com.example.feature.miniapps.browser")
