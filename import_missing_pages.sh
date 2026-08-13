#!/bin/bash
set -e

# 1. Copy ActiveAppTracker
cp reference/app/src/main/java/com/example/utils/ActiveAppTracker.kt app/src/main/java/com/example/utils/ActiveAppTracker.kt

# 2. Copy and adapt Sidebar Pages
for page in SchedulerPageView NotificationPageView ResourcesTrackerPageView; do
    cp reference/app/src/main/java/com/example/service/${page}.kt app/src/main/java/com/example/feature/sidebar/${page}.kt
    # Update package
    sed -i 's/package com.example.service/package com.example.feature.sidebar/' app/src/main/java/com/example/feature/sidebar/${page}.kt
done

# 3. Copy PageManagementSettingsScreen
cp reference/app/src/main/java/com/example/PageManagementSettingsScreen.kt app/src/main/java/com/example/feature/settings/PageManagementSettingsScreen.kt
sed -i 's/package com.example/package com.example.feature.settings/' app/src/main/java/com/example/feature/settings/PageManagementSettingsScreen.kt

# 4. Update AndroidManifest.xml (Insert AppNotificationListener)
# Look for </application> and insert before it
sed -i '/<\/application>/i \        <service\n            android:name=".service.AppNotificationListener"\n            android:label="Vian Notification Listener"\n            android:permission="android.permission.BIND_NOTIFICATION_LISTENER_SERVICE"\n            android:exported="true">\n            <intent-filter>\n                <action android:name="android.service.notification.NotificationListenerService" />\n            </intent-filter>\n        </service>' app/src/main/AndroidManifest.xml

