import subprocess
print(subprocess.run(["./gradlew", "assembleDebug"], capture_output=True, text=True).stdout)
