filepath = 'app/src/main/java/com/example/App.kt'
with open(filepath, 'r') as f:
    content = f.read()

old_app = """    override fun onCreate() {"""

new_app = """    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        com.example.core.FloatingWindowManager.onTrimMemory(level)
    }

    override fun onCreate() {"""

content = content.replace(old_app, new_app)
with open(filepath, 'w') as f:
    f.write(content)
print("Updated App.kt")
