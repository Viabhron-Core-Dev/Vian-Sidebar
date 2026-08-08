with open('app/src/main/AndroidManifest.xml', 'r') as f:
    content = f.read()

import re
content = re.sub(r'<service android:name="\.service\.FileExplorerFloatingService" />\n\s*', '', content)
content = re.sub(r'<activity\s+android:name="\.FileExplorerLauncherActivity"\s+android:theme="@style/Theme\.Appywork"\s+android:exported="true">\s*<intent-filter>\s*<action android:name="android\.intent\.action\.MAIN" />\s*<category android:name="android\.intent\.category\.LAUNCHER" />\s*</intent-filter>\s*</activity>\n\s*', '', content)

with open('app/src/main/AndroidManifest.xml', 'w') as f:
    f.write(content)
