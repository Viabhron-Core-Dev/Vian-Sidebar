with open("app/src/main/java/com/example/HandleManager.kt", "r") as f:
    content = f.read()

content = content.replace('list.add(HandleConfig(id = "sidebar", name = "Handle 1 | Right (Bottom)", enabled = true))\n            return list', 'list.add(HandleConfig(id = "sidebar", name = "Handle 1 | Right (Bottom)", enabled = true))\n            prefs.edit().putString("handle_sidebar_tap", "toggle_sidebar").apply()\n            return list')

with open("app/src/main/java/com/example/HandleManager.kt", "w") as f:
    f.write(content)
