import re
with open('app/src/main/java/com/example/feature/system_hub/VianSideAccessibilityService.kt', 'r') as f:
    text = f.read()

# Completely remove the Intent(...) start activities
text = re.sub(r'val intent = Intent\(this, com\.example\.BarcodeScannerActivity::class\.java\)\.apply \{.*?startActivity\(intent\)', 'android.widget.Toast.makeText(this, "Not Migrated", 0).show()', text, flags=re.DOTALL)
text = re.sub(r'val cropIntent = Intent\(this, com\.example\.QRCropActivity::class\.java\)\.apply \{.*?startActivity\(cropIntent\)', 'android.widget.Toast.makeText(this, "Not Migrated", 0).show()', text, flags=re.DOTALL)

with open('app/src/main/java/com/example/feature/system_hub/VianSideAccessibilityService.kt', 'w') as f:
    f.write(text)
