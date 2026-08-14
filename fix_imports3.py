import os
import glob

def replace_in_file(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    replacements = {
        'import com.example.service.AppWidgetHelper': 'import com.example.core.AppWidgetHelper',
        'import com.example.service.QuickTileHandler': 'import com.example.feature.system_hub.QuickTileHandler',
        'import com.example.service.MediaVolumeHandler': 'import com.example.feature.system_hub.MediaVolumeHandler',
        'import com.example.service.DisplayHandler': 'import com.example.feature.system_hub.DisplayHandler',
        'import com.example.service.VianSideAccessibilityService': 'import com.example.feature.system_hub.VianSideAccessibilityService',
        'import com.example.service.FloatingReaderService': 'import com.example.feature.miniapps.reader.FloatingReaderService',
        'com.example.service.AppWidgetHelper': 'com.example.core.AppWidgetHelper',
        'com.example.service.VianSideAccessibilityService': 'com.example.feature.system_hub.VianSideAccessibilityService'
    }

    new_content = content
    for old, new in replacements.items():
        new_content = new_content.replace(old, new)

    if new_content != content:
        with open(filepath, 'w') as f:
            f.write(new_content)
        print(f"Updated {filepath}")

for root, _, files in os.walk('app/src/main/java'):
    for file in files:
        if file.endswith('.kt'):
            replace_in_file(os.path.join(root, file))

