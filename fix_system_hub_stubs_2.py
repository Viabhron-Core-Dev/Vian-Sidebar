import re

# VianSideAccessibilityService.kt
with open('app/src/main/java/com/example/feature/system_hub/VianSideAccessibilityService.kt', 'r') as f:
    text = f.read()

text = re.sub(r'private var autoScrollManager.*', 'private var autoScrollManager: Any? = null', text)
text = re.sub(r'private var cursorManager.*', 'private var cursorManager: Any? = null', text)
text = re.sub(r'private var longScreenshotManager.*', 'private var longScreenshotManager: Any? = null', text)

text = re.sub(r'autoScrollManager = .*', 'autoScrollManager = null', text)
text = re.sub(r'cursorManager = .*', 'cursorManager = null', text)
text = re.sub(r'longScreenshotManager = .*', 'longScreenshotManager = null', text)

text = text.replace("autoScrollManager?.isRunning()", "false")
text = text.replace("autoScrollManager?.stop()", "")
text = text.replace("autoScrollManager?.start(", "// autoScrollManager?.start(")

text = text.replace("cursorManager?.isRunning()", "false")
text = text.replace("cursorManager?.stop()", "")
text = text.replace("cursorManager?.start(", "// cursorManager?.start(")

text = text.replace("longScreenshotManager?.start(", "// longScreenshotManager?.start(")

p_barcode = r"val intent = Intent\(this,\s*com\.example\.BarcodeScannerActivity::class\.java\)\.apply\s*\{[^\}]*\}\s*startActivity\(intent\)"
text = re.sub(p_barcode, 'android.widget.Toast.makeText(this, "Barcode Scanner Not Migrated", android.widget.Toast.LENGTH_SHORT).show()', text, flags=re.DOTALL)

p_qr = r"val cropIntent = Intent\(this,\s*com\.example\.QRCropActivity::class\.java\)\.apply\s*\{[^\}]*\}\s*startActivity\(cropIntent\)"
text = re.sub(p_qr, 'android.widget.Toast.makeText(this, "QR Crop Not Migrated", android.widget.Toast.LENGTH_SHORT).show()', text, flags=re.DOTALL)

with open('app/src/main/java/com/example/feature/system_hub/VianSideAccessibilityService.kt', 'w') as f:
    f.write(text)

