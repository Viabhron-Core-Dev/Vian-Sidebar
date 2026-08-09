import os
import glob

def prepend_imports(filepath, imports):
    with open(filepath, 'r') as f:
        content = f.read()
    
    # Insert right after the package declaration
    lines = content.split('\n')
    for i, line in enumerate(lines):
        if line.startswith('package '):
            for imp in imports:
                lines.insert(i+1, imp)
            break
    
    with open(filepath, 'w') as f:
        f.write('\n'.join(lines))

prepend_imports('app/src/main/java/com/example/AppTrackerOpenerActivity.kt', [
    'import com.example.service.VianSideAccessibilityService'
])

for f in ['AppsPageView.kt', 'HybridGridPageView.kt', 'WidgetsGridPageView.kt', 'AppTrackerPageView.kt']:
    filepath = f"app/src/main/java/com/example/feature/sidebar/{f}"
    if os.path.exists(filepath):
        prepend_imports(filepath, [
            'import com.example.service.VianSideAccessibilityService',
            'import com.example.service.FloatingReaderService'
        ])
        
