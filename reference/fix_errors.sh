#!/bin/bash
# Move ocrCroppedArea inside QRCropActivity
sed -i '/private fun ocrCroppedArea/,$d' app/src/main/java/com/example/service/QRCropActivity.kt

# Inject it before the last two braces (which close QRCropScreen)
# Wait, let's inject it into QRCropActivity by finding where shareCroppedArea is, and putting it right after that function.
