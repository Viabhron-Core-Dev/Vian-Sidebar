import os

dir_path = 'app/src/main/java/com/example/feature/system_hub/'
for filename in os.listdir(dir_path):
    if not filename.endswith('.kt'): continue
    
    filepath = os.path.join(dir_path, filename)
    with open(filepath, 'r') as f:
        content = f.read()
    
    content = content.replace("package com.example.service", "package com.example.feature.system_hub")
    content = content.replace("import com.example.LogKeeper", "import com.example.core.LogKeeper")
    content = content.replace("com.example.LogKeeper", "com.example.core.LogKeeper")
    
    # Fix usages of other classes that might have moved
    # e.g., FloatingReaderService -> com.example.core.HandleService
    content = content.replace("FloatingReaderService", "com.example.core.HandleService")
    
    with open(filepath, 'w') as f:
        f.write(content)
