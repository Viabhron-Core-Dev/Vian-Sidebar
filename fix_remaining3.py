import re

with open('app/src/main/java/com/example/feature/settings/SettingsActivity.kt', 'r') as f:
    settings = f.read()

settings = re.sub(r'// startActivity\(intent\)', '', settings)
settings = re.sub(r'// // startActivity\(intent\)', '', settings)

# Make sure any BackupHelper calls are gone
settings = settings.replace("/* import com.example.BackupHelper */", "")
settings = settings.replace("isFirstLaunch = true", "")

with open('app/src/main/java/com/example/feature/settings/SettingsActivity.kt', 'w') as f:
    f.write(settings)
