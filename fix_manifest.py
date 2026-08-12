import re
with open('app/src/main/AndroidManifest.xml', 'r') as f:
    text = f.read()

service_tag = """
        <service
            android:name=".feature.system_hub.VianSideAccessibilityService"
            android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE"
            android:exported="true">
            <intent-filter>
                <action android:name="android.accessibilityservice.AccessibilityService" />
            </intent-filter>
            <meta-data
                android:name="android.accessibilityservice"
                android:resource="@xml/accessibility_service_config" />
        </service>
"""

# Insert before </application>
text = text.replace("</application>", service_tag + "</application>")

with open('app/src/main/AndroidManifest.xml', 'w') as f:
    f.write(text)
