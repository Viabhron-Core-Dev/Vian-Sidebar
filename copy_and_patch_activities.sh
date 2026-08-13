cp reference/app/src/main/java/com/example/HybridGridEditActivity.kt app/src/main/java/com/example/HybridGridEditActivity.kt
cp reference/app/src/main/java/com/example/WidgetsGridEditActivity.kt app/src/main/java/com/example/WidgetsGridEditActivity.kt
cp reference/app/src/main/java/com/example/SidebarEditActivity.kt app/src/main/java/com/example/SidebarEditActivity.kt

sed -i 's/import com.example.service.GridWidgetItem/import com.example.feature.sidebar.GridWidgetItem/g' app/src/main/java/com/example/HybridGridEditActivity.kt
sed -i 's/import com.example.service.GridWidgetItem/import com.example.feature.sidebar.GridWidgetItem/g' app/src/main/java/com/example/WidgetsGridEditActivity.kt
sed -i 's/import com.example.service.SidebarAppsManager/import com.example.feature.sidebar.SidebarAppsManager/g' app/src/main/java/com/example/SidebarEditActivity.kt
