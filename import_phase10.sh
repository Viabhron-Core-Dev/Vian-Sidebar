#!/bin/bash
set -e

mkdir -p app/src/main/java/com/example/feature/system_hub
mkdir -p app/src/main/java/com/example/core

# System Hub / Accessibility
cp reference/app/src/main/java/com/example/service/VianSideAccessibilityService.kt app/src/main/java/com/example/feature/system_hub/
cp reference/app/src/main/java/com/example/service/AutoScrollManager.kt app/src/main/java/com/example/feature/system_hub/
cp reference/app/src/main/java/com/example/service/CursorManager.kt app/src/main/java/com/example/feature/system_hub/
cp reference/app/src/main/java/com/example/service/LongScreenshotManager.kt app/src/main/java/com/example/feature/system_hub/
cp reference/app/src/main/java/com/example/service/CallRecorderManager.kt app/src/main/java/com/example/feature/system_hub/
cp reference/app/src/main/java/com/example/service/FloatingRecordButtonView.kt app/src/main/java/com/example/feature/system_hub/
cp reference/app/src/main/java/com/example/service/QuickTileHandler.kt app/src/main/java/com/example/feature/system_hub/
cp reference/app/src/main/java/com/example/service/DisplayHandler.kt app/src/main/java/com/example/feature/system_hub/
cp reference/app/src/main/java/com/example/service/MediaVolumeHandler.kt app/src/main/java/com/example/feature/system_hub/

# Core
cp reference/app/src/main/java/com/example/service/NetSpeedManager.kt app/src/main/java/com/example/core/

# Settings
cp reference/app/src/main/java/com/example/NetSpeedSettingsScreen.kt app/src/main/java/com/example/feature/settings/
cp reference/app/src/main/java/com/example/CallRecorderSettingsScreen.kt app/src/main/java/com/example/feature/settings/

# Update packages
sed -i 's/package com.example.service/package com.example.feature.system_hub/' app/src/main/java/com/example/feature/system_hub/*.kt
sed -i 's/package com.example.service/package com.example.core/' app/src/main/java/com/example/core/NetSpeedManager.kt
sed -i 's/package com.example/package com.example.feature.settings/' app/src/main/java/com/example/feature/settings/NetSpeedSettingsScreen.kt
sed -i 's/package com.example/package com.example.feature.settings/' app/src/main/java/com/example/feature/settings/CallRecorderSettingsScreen.kt

echo "Phase 10 files imported."
