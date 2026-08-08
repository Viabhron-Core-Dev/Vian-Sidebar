import os
with open("app/src/main/java/com/example/service/DictionaryWindowManager.kt", "r") as f:
    if "import androidx.compose.material.icons.filled.VolumeUp" not in f.read():
        print("Not there")
