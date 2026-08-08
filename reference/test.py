import re
with open("app/src/main/java/com/example/HandlesListSettingsScreen.kt", "r") as f:
    print(f.read().find("LocalLifecycleOwner.current"))
