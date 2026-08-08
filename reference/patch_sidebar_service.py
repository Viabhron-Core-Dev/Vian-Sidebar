import re

with open('app/src/main/java/com/example/service/SidebarService.kt', 'r') as f:
    content = f.read()

# Add screen_record and close()
new_logic = """            } else if (action == "barcode_scanner") {
                val intent = Intent(this, com.example.service.BarcodeScannerActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } else if (action == "screen_record") {
                val intent = Intent(this, com.example.service.ScreenRecordActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } else if (action == "settings") {"""

content = content.replace('            } else if (action == "barcode_scanner") {\n                val intent = Intent(this, com.example.service.BarcodeScannerActivity::class.java)\n                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)\n                startActivity(intent)\n            } else if (action == "settings") {', new_logic)

new_tail = """                startActivity(launchIntent)
            } catch (e: Exception) {}
        }
        sidebarView?.close()
        hybridGridWindowManager?.show(false)
    }"""

content = content.replace('                startActivity(launchIntent)\n            } catch (e: Exception) {}\n        }\n    }', new_tail)

with open('app/src/main/java/com/example/service/SidebarService.kt', 'w') as f:
    f.write(content)
