#!/bin/bash
sed -i '/Text("Scan QR")/!b;n;a\
            Button(onClick = {\
                val imgRatio = bitmap.width.toFloat() / bitmap.height.toFloat()\
                val viewRatio = viewSize.width.toFloat() / viewSize.height.toFloat()\
                var renderedW = viewSize.width.toFloat()\
                var renderedH = viewSize.height.toFloat()\
                var offsetX = 0f\
                var offsetY = 0f\
                if (imgRatio > viewRatio) {\
                    renderedH = viewSize.width / imgRatio\
                    offsetY = (viewSize.height - renderedH) / 2f\
                } else {\
                    renderedW = viewSize.height * imgRatio\
                    offsetX = (viewSize.width - renderedW) / 2f\
                }\
                val scale = bitmap.width / renderedW\
                val minX = polygonPoints.minOfOrNull { it.x } ?: 0f\
                val maxX = polygonPoints.maxOfOrNull { it.x } ?: 0f\
                val minY = polygonPoints.minOfOrNull { it.y } ?: 0f\
                val maxY = polygonPoints.maxOfOrNull { it.y } ?: 0f\
                val rectToUse = if (cropShape == "polygon" && polygonPoints.isNotEmpty()) Rect(minX, minY, maxX, maxY) else cropRect\
                val realX = (rectToUse.left - offsetX) * scale\
                val realY = (rectToUse.top - offsetY) * scale\
                val realW = rectToUse.width * scale\
                val realH = rectToUse.height * scale\
                val mappedPoints = polygonPoints.map { Offset((it.x - offsetX) * scale, (it.y - offsetY) * scale) }\
                onAction("ocr", realX, realY, realW, realH, cropShape, mappedPoints)\
            }) {\
                Text("OCR")\
            }
' app/src/main/java/com/example/service/QRCropActivity.kt
