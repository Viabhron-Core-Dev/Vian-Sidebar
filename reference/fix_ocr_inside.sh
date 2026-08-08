#!/bin/bash
sed -i '/private fun shareCroppedArea/i\
    private fun ocrCroppedArea(bitmap: Bitmap, x: Float, y: Float, w: Float, h: Float, shape: String, points: List<Offset>) {\
        val cropX = maxOf(0, x.toInt())\
        val cropY = maxOf(0, y.toInt())\
        val cropW = minOf(bitmap.width - cropX, w.toInt())\
        val cropH = minOf(bitmap.height - cropY, h.toInt())\
        if (cropW <= 0 || cropH <= 0) {\
            Toast.makeText(this, "Invalid crop area", Toast.LENGTH_SHORT).show()\
            return\
        }\
        try {\
            var croppedBitmap = Bitmap.createBitmap(bitmap, cropX, cropY, cropW, cropH)\
            \
            if (shape == "circle") {\
                val output = Bitmap.createBitmap(cropW, cropH, Bitmap.Config.ARGB_8888)\
                val canvas = android.graphics.Canvas(output)\
                canvas.drawColor(android.graphics.Color.WHITE)\
                val path = android.graphics.Path()\
                path.addOval(android.graphics.RectF(0f, 0f, cropW.toFloat(), cropH.toFloat()), android.graphics.Path.Direction.CW)\
                canvas.clipPath(path)\
                canvas.drawBitmap(croppedBitmap, 0f, 0f, null)\
                croppedBitmap = output\
            } else if (shape == "polygon") {\
                val output = Bitmap.createBitmap(cropW, cropH, Bitmap.Config.ARGB_8888)\
                val canvas = android.graphics.Canvas(output)\
                canvas.drawColor(android.graphics.Color.WHITE)\
                if (points.isNotEmpty()) {\
                    val path = android.graphics.Path()\
                    path.moveTo(points.first().x - cropX, points.first().y - cropY)\
                    for (i in 1 until points.size) {\
                        path.lineTo(points[i].x - cropX, points[i].y - cropY)\
                    }\
                    path.close()\
                    canvas.clipPath(path)\
                }\
                canvas.drawBitmap(croppedBitmap, 0f, 0f, null)\
                croppedBitmap = output\
            }\
            \
            val image = InputImage.fromBitmap(croppedBitmap, 0)\
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)\
            recognizer.process(image)\
                .addOnSuccessListener { visionText ->\
                    val text = visionText.text\
                    if (text.isNotEmpty()) {\
                        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager\
                        clipboard.setPrimaryClip(ClipData.newPlainText("OCR Text", text))\
                        Toast.makeText(this, "Copied: $text", Toast.LENGTH_LONG).show()\
                    } else {\
                        Toast.makeText(this, "No text found", Toast.LENGTH_SHORT).show()\
                    }\
                }\
                .addOnFailureListener { e ->\
                    Toast.makeText(this, "OCR failed: ${e.message}", Toast.LENGTH_SHORT).show()\
                }\
        } catch (e: Exception) {\
            e.printStackTrace()\
            Toast.makeText(this, "Failed to crop for OCR", Toast.LENGTH_SHORT).show()\
        }\
    }\
' app/src/main/java/com/example/service/QRCropActivity.kt
