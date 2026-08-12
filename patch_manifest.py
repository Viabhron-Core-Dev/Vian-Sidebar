import re

with open('app/src/main/AndroidManifest.xml', 'r') as f:
    manifest = f.read()

activities = """        <activity android:name=".feature.settings.AddElementActivity" android:exported="false" android:theme="@style/Theme.LiteReader" />
        <activity android:name=".feature.settings.ActionPickerActivity" android:exported="false" android:theme="@style/Theme.LiteReader" />
"""

if "AddElementActivity" not in manifest:
    manifest = manifest.replace("</application>", activities + "    </application>")
    with open('app/src/main/AndroidManifest.xml', 'w') as f:
        f.write(manifest)
    print("Manifest updated")
else:
    print("Manifest already updated")
