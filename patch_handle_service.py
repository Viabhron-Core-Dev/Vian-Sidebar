filepath = 'app/src/main/java/com/example/core/HandleService.kt'
with open(filepath, 'r') as f:
    content = f.read()

old_destroy = """    override fun onDestroy() {
        super.onDestroy()
        AppLogger.d("HandleService", "onDestroy")
        prefs.unregisterOnSharedPreferenceChangeListener(this)
        unregisterReceiver(reloadReceiver)
        triggerHandleViews.forEach { it.detach() }
        triggerHandleViews.clear()
        readerHandleView?.detach()
        readerHandleView = null
    }"""

new_destroy = """    override fun onDestroy() {
        super.onDestroy()
        AppLogger.d("HandleService", "onDestroy")
        prefs.unregisterOnSharedPreferenceChangeListener(this)
        unregisterReceiver(reloadReceiver)
        screenStateReceiver?.let { unregisterReceiver(it) }
        triggerHandleViews.forEach { it.detach() }
        triggerHandleViews.clear()
        readerHandleView?.detach()
        readerHandleView = null
    }"""

content = content.replace(old_destroy, new_destroy)
with open(filepath, 'w') as f:
    f.write(content)
print("Updated HandleService.kt")
