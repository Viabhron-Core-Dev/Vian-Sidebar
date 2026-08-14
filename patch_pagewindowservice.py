filepath = 'app/src/main/java/com/example/service/PageWindowService.kt'
with open(filepath, 'r') as f:
    content = f.read()

old_add = """                val title = getTitleForPageType(pageType)
                val window = PageWindow(this, pageType, title)
                windows[pageType] = window
                window.show()"""
new_add = """                val title = getTitleForPageType(pageType)
                val window = PageWindow(this, pageType, title)
                window.onClose = {
                    windows.remove(pageType)
                }
                windows[pageType] = window
                window.show()"""
content = content.replace(old_add, new_add)

with open(filepath, 'w') as f:
    f.write(content)
print("PageWindowService updated")
