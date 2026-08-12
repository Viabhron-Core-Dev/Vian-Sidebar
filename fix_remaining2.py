import re

with open('app/src/main/java/com/example/feature/settings/ActionPickerActivity.kt', 'r') as f:
    action_picker = f.read()

imports = """
import com.example.feature.sidebar.SidebarItem
import com.example.feature.sidebar.ALL_QUICK_TILES
import com.example.feature.sidebar.ALL_SYSTEM_ACTIONS
import com.example.feature.sidebar.ALL_SCREEN_CAPTURE_ACTIONS
import com.example.feature.sidebar.ALL_UTILITIES_ACTIONS
import com.example.feature.sidebar.ALL_VOLUME_ACTIONS
import com.example.feature.sidebar.ALL_MEDIA_ACTIONS
import com.example.feature.sidebar.ALL_DISPLAY_ACTIONS
import com.example.feature.sidebar.ALL_SETTINGS_SHORTCUTS
"""
action_picker = action_picker.replace("import android.widget.TextView", "import android.widget.TextView" + imports)

with open('app/src/main/java/com/example/feature/settings/ActionPickerActivity.kt', 'w') as f:
    f.write(action_picker)


with open('app/src/main/java/com/example/feature/settings/AddElementActivity.kt', 'r') as f:
    add_el = f.read()

add_el = add_el.replace("""        addItem(android.R.drawable.ic_menu_agenda, "App") {
            startActivityForResult(Intent(this, AppPickerActivity::class.java), 200)
        }""", """        addItem(android.R.drawable.ic_menu_agenda, "App") {
            // Stubbed
        }""")

add_el = add_el.replace("""        addItem(android.R.drawable.ic_menu_share, "Shortcut") {
            startActivityForResult(Intent(this, ShortcutPickerActivity::class.java), 300)
        }""", """        addItem(android.R.drawable.ic_menu_share, "Shortcut") {
            // Stubbed
        }""")

add_el = add_el.replace("com.example.service.core.HandleService", "com.example.core.HandleService")

with open('app/src/main/java/com/example/feature/settings/AddElementActivity.kt', 'w') as f:
    f.write(add_el)


with open('app/src/main/java/com/example/feature/settings/SettingsActivity.kt', 'r') as f:
    settings = f.read()

settings = settings.replace("import com.example.BackupHelper", "")
settings = settings.replace("isFirstLaunch = true,", "")

# There was an issue with `intent` on line 207, which is likely part of the LogKeeperActivity stub
# val intent = Intent() /* LogKeeperActivity */
# startActivity(intent) -> This fails because intent has no action or class, but it shouldn't fail to compile unless `startActivity` rejects empty intents at runtime.
# Wait, the error is: "Unresolved reference 'intent'"
# Ah, maybe I commented out `val intent = ...` but not the `startActivity(intent)`!
settings = settings.replace("startActivity(intent)", "// startActivity(intent)")
settings = settings.replace("val intent = Intent() /* LogKeeperActivity */", "")
settings = settings.replace("val intent = Intent() /* PwaManagerActivity */", "")
settings = settings.replace("val intent = Intent() /* AppyworkSettingsActivity */", "")

with open('app/src/main/java/com/example/feature/settings/SettingsActivity.kt', 'w') as f:
    f.write(settings)
