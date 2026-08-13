#!/bin/bash
sed -i 's/: LinearLayout(context) {/: FrameLayout(context) {/g' app/src/main/java/com/example/feature/sidebar/SidebarView.kt
sed -i 's/orientation = VERTICAL//g' app/src/main/java/com/example/feature/sidebar/SidebarView.kt
