import os

def migrate_file(src, dest, package_name):
    if not os.path.exists(src):
        print(f"Source not found: {src}")
        return
        
    with open(src, 'r') as f:
        content = f.read()
        
    # Replace package
    content = content.replace("package com.example", f"package {package_name}")
    
    # Fix LogKeeper import and usage
    content = content.replace("import com.example.LogKeeper", "import com.example.core.LogKeeper")
    content = content.replace("com.example.LogKeeper", "com.example.core.LogKeeper")
    
    # Write destination
    os.makedirs(os.path.dirname(dest), exist_ok=True)
    with open(dest, 'w') as f:
        f.write(content)
    print(f"Migrated {src} to {dest}")

migrate_file('reference/app/src/main/java/com/example/SettingsActivity.kt', 'app/src/main/java/com/example/feature/settings/SettingsActivity.kt', 'com.example.feature.settings')
migrate_file('reference/app/src/main/java/com/example/AddElementActivity.kt', 'app/src/main/java/com/example/feature/settings/AddElementActivity.kt', 'com.example.feature.settings')
migrate_file('reference/app/src/main/java/com/example/ActionPickerActivity.kt', 'app/src/main/java/com/example/feature/settings/ActionPickerActivity.kt', 'com.example.feature.settings')
