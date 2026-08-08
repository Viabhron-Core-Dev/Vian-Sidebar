import re

with open('app/src/main/AndroidManifest.xml', 'r') as f:
    content = f.read()

target = '<activity android:name=".AppyworkSettingsActivity" android:exported="false" />'
replacement = """<activity android:name=".AppyworkSettingsActivity" android:exported="false" />
        <activity android:name=".AppyworkReceiverActivity" android:label="Push via Appywork" android:theme="@android:style/Theme.Translucent.NoTitleBar" android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.PROCESS_TEXT" />
                <category android:name="android.intent.category.DEFAULT" />
                <data android:mimeType="text/plain" />
            </intent-filter>
        </activity>
        <service android:name=".service.AppyworkFloatingService" android:exported="false" />"""

if 'AppyworkReceiverActivity' not in content:
    content = content.replace(target, replacement)
    with open('app/src/main/AndroidManifest.xml', 'w') as f:
        f.write(content)
