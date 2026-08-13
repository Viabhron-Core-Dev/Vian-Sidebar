#!/bin/bash
cp reference/app/src/main/java/com/example/service/TagManagementActivity.kt app/src/main/java/com/example/feature/settings/TagManagementActivity.kt
sed -i 's/package com.example.service/package com.example.feature.settings/' app/src/main/java/com/example/feature/settings/TagManagementActivity.kt
sed -i '/<\/application>/i \        <activity android:name=".feature.settings.TagManagementActivity" android:exported="false" android:theme="@style/Theme.LiteReader" />' app/src/main/AndroidManifest.xml
