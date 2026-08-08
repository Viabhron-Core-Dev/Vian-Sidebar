with open("app/src/main/java/com/example/service/ReaderHandleView.kt", "r") as f:
    content = f.read()

content = content.replace(
    'val isRight = prefs.getString("sidebar_position", "right") == "right"',
    'val isRight = prefs.getString("${prefix}edge", "right") == "right"'
)

with open("app/src/main/java/com/example/service/ReaderHandleView.kt", "w") as f:
    f.write(content)
