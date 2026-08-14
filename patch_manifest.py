filepath = 'app/src/main/AndroidManifest.xml'
with open(filepath, 'r') as f:
    content = f.read()

# Add NotificationHistoryActivity
activity_tag = '        <activity android:name=".NotificationHistoryActivity" android:exported="false" android:theme="@style/Theme.LiteReader" />'

if "NotificationHistoryActivity" not in content:
    content = content.replace("</application>", activity_tag + "\n    </application>")

with open(filepath, 'w') as f:
    f.write(content)
print("AndroidManifest updated")
