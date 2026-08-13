package com.example.feature.system_hub

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.core.LogKeeper
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class QRCropActivity : ComponentActivity() {
    private var tempImagePath: String? = null

    override fun onDestroy() {
        super.onDestroy()
        tempImagePath?.let { path ->
            try {
                val file = File(path)
                if (file.exists()) {
                    file.delete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        try {
            val cacheFiles = cacheDir.listFiles { _, name -> name.startsWith("shared_crop_") }
            cacheFiles?.forEach { file ->
                if (file.lastModified() < System.currentTimeMillis() - 60 * 60 * 1000) { // 1 hour old
                    file.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Cleanup old shared crop images
        Thread {
            try {
                cacheDir.listFiles()?.forEach { file ->
                    if (file.name.startsWith("shared_crop_") && file.name.endsWith(".jpg")) {
                        file.delete()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()

        super.onCreate(savedInstanceState)
        
        val imagePath = intent.getStringExtra("IMAGE_PATH")
        tempImagePath = imagePath
        if (imagePath == null) {
            Toast.makeText(this, "No image provided", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val file = File(imagePath)
        if (!file.exists()) {
            Toast.makeText(this, "Image file not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
        if (bitmap == null) {
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
                    QRCropScreen(
                        bitmap = bitmap,
                        onAction = { action, x, y, width, height, shape, points ->
                            if (action == "scan") {
                                scanCroppedArea(bitmap, x, y, width, height, shape, points)
                            } else if (action == "ocr") {
                                ocrCroppedArea(bitmap, x, y, width, height, shape, points)
                            } else if (action == "share") {
                                shareCroppedArea(bitmap, x, y, width, height, shape, points)
                            }
                        },
                        onClose = { finish() }
                    )
                }
            }
        }
    }

    private fun ocrCroppedArea(bitmap: Bitmap, x: Float, y: Float, w: Float, h: Float, shape: String, points: List<Offset>) {
        val cropX = maxOf(0, x.toInt())
        val cropY = maxOf(0, y.toInt())
        val cropW = minOf(bitmap.width - cropX, w.toInt())
        val cropH = minOf(bitmap.height - cropY, h.toInt())
        if (cropW <= 0 || cropH <= 0) {
            Toast.makeText(this, "Invalid crop area", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            var croppedBitmap = Bitmap.createBitmap(bitmap, cropX, cropY, cropW, cropH)
            
            if (shape == "circle") {
                val output = Bitmap.createBitmap(cropW, cropH, Bitmap.Config.ARGB_8888)
                val canvas = android.graphics.Canvas(output)
                canvas.drawColor(android.graphics.Color.WHITE)
                val path = android.graphics.Path()
                path.addOval(android.graphics.RectF(0f, 0f, cropW.toFloat(), cropH.toFloat()), android.graphics.Path.Direction.CW)
                canvas.clipPath(path)
                canvas.drawBitmap(croppedBitmap, 0f, 0f, null)
                croppedBitmap = output
            } else if (shape == "polygon") {
                val output = Bitmap.createBitmap(cropW, cropH, Bitmap.Config.ARGB_8888)
                val canvas = android.graphics.Canvas(output)
                canvas.drawColor(android.graphics.Color.WHITE)
                if (points.isNotEmpty()) {
                    val path = android.graphics.Path()
                    path.moveTo(points.first().x - cropX, points.first().y - cropY)
                    for (i in 1 until points.size) {
                        path.lineTo(points[i].x - cropX, points[i].y - cropY)
                    }
                    path.close()
                    canvas.clipPath(path)
                }
                canvas.drawBitmap(croppedBitmap, 0f, 0f, null)
                croppedBitmap = output
            }
            
            val image = InputImage.fromBitmap(croppedBitmap, 0)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val text = visionText.text
                    if (text.isNotEmpty()) {
                        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("OCR Text", text))
                        Toast.makeText(this, "Copied: $text", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, "No text found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "OCR failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to crop for OCR", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareCroppedArea(bitmap: Bitmap, x: Float, y: Float, w: Float, h: Float, shape: String, points: List<Offset>) {
        val cropX = maxOf(0, x.toInt())
        val cropY = maxOf(0, y.toInt())
        val cropW = minOf(bitmap.width - cropX, w.toInt())
        val cropH = minOf(bitmap.height - cropY, h.toInt())
        if (cropW <= 0 || cropH <= 0) {
            Toast.makeText(this, "Invalid crop area", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            var croppedBitmap = Bitmap.createBitmap(bitmap, cropX, cropY, cropW, cropH)
            
            if (shape == "circle") {
                val output = Bitmap.createBitmap(cropW, cropH, Bitmap.Config.ARGB_8888)
                val canvas = android.graphics.Canvas(output)
                canvas.drawColor(android.graphics.Color.WHITE)
                val path = android.graphics.Path()
                path.addOval(android.graphics.RectF(0f, 0f, cropW.toFloat(), cropH.toFloat()), android.graphics.Path.Direction.CW)
                canvas.clipPath(path)
                canvas.drawBitmap(croppedBitmap, 0f, 0f, null)
                croppedBitmap = output
            } else if (shape == "polygon") {
                val output = Bitmap.createBitmap(cropW, cropH, Bitmap.Config.ARGB_8888)
                val canvas = android.graphics.Canvas(output)
                canvas.drawColor(android.graphics.Color.WHITE)
                if (points.isNotEmpty()) {
                    val path = android.graphics.Path()
                    path.moveTo(points.first().x - cropX, points.first().y - cropY)
                    for (i in 1 until points.size) {
                        path.lineTo(points[i].x - cropX, points[i].y - cropY)
                    }
                    path.close()
                    canvas.clipPath(path)
                }
                canvas.drawBitmap(croppedBitmap, 0f, 0f, null)
                croppedBitmap = output
            }

            val cacheFile = java.io.File(cacheDir, "shared_crop_${System.currentTimeMillis()}.jpg")
            java.io.FileOutputStream(cacheFile).use { out ->
                croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }
            val uri = androidx.core.content.FileProvider.getUriForFile(this, "${packageName}.provider", cacheFile)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, uri)
                clipData = android.content.ClipData.newRawUri("", uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(shareIntent, "Share Image")
            chooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(chooser)
            finish()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error sharing image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun scanCroppedArea(bitmap: Bitmap, x: Float, y: Float, w: Float, h: Float, shape: String, points: List<Offset>) {
        val cropX = maxOf(0, x.toInt())
        val cropY = maxOf(0, y.toInt())
        val cropW = minOf(bitmap.width - cropX, w.toInt())
        val cropH = minOf(bitmap.height - cropY, h.toInt())

        if (cropW <= 0 || cropH <= 0) {
            Toast.makeText(this, "Invalid crop area", Toast.LENGTH_SHORT).show()
            return
        }

        val croppedBitmap = Bitmap.createBitmap(bitmap, cropX, cropY, cropW, cropH)
        
        Thread {
            try {
                val pixels = IntArray(cropW * cropH)
                croppedBitmap.getPixels(pixels, 0, cropW, 0, 0, cropW, cropH)
                val source = RGBLuminanceSource(cropW, cropH, pixels)
                val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
                val reader = MultiFormatReader()
                val result = reader.decode(binaryBitmap)
                val text = result.text
                
                runOnUiThread {
                    if (text.isNullOrEmpty()) {
                        Toast.makeText(this, "No QR Code found in this area", Toast.LENGTH_SHORT).show()
                    } else {
                        com.example.core.LogKeeper.writeLog("QRCropActivity", "Successfully scanned QR Code: ${text.take(20)}...")
                        showResultDialog(text)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "No QR Code found", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun showResultDialog(text: String) {
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                AlertDialog(
                    onDismissRequest = { finish() },
                    title = { Text("Scanned QR Code (Secure)") },
                    text = { 
                        Column {
                            Text("Data treated with caution. Do not open unknown links.", style = MaterialTheme.typography.labelSmall, color = Color.Yellow)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text) 
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("QR Code", text)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(this@QRCropActivity, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                        }) {
                            Text("Copy Data")
                        }
                    },
                    dismissButton = {
                        if (text.startsWith("http://") || text.startsWith("https://")) {
                            TextButton(onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(text))
                                startActivity(intent)
                                finish()
                            }) {
                                Text("Open Link", color = Color.Red)
                            }
                        } else {
                            TextButton(onClick = { finish() }) {
                                Text("Close")
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun QRCropScreen(bitmap: Bitmap, onAction: (String, Float, Float, Float, Float, String, List<Offset>) -> Unit, onClose: () -> Unit) {
    var viewSize by remember { mutableStateOf(IntSize.Zero) }
    var cropRect by remember { mutableStateOf(Rect.Zero) }
    var cropShape by remember { mutableStateOf("square") }
    val polygonPoints = remember { mutableStateListOf<Offset>() }
    
    Box(modifier = Modifier.fillMaxSize().onSizeChanged { size ->
        viewSize = size
        if (cropRect == Rect.Zero && size.width > 0 && size.height > 0) {
            val boxSize = size.width * 0.6f
            cropRect = Rect(
                offset = Offset((size.width - boxSize) / 2f, (size.height - boxSize) / 2f),
                size = Size(boxSize, boxSize)
            )
        }
    }) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Screenshot",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
        
        if (viewSize.width > 0) {
            val handleSize = 40.dp
            
            Canvas(modifier = Modifier
                .graphicsLayer { alpha = 0.99f }
                .fillMaxSize()
                .pointerInput(cropShape) {
                    if (cropShape == "polygon") {
                        detectTapGestures(
                            onTap = { offset ->
                                polygonPoints.add(offset)
                            }
                        )
                    } else {
                        var dragHandle: String? = null
                        detectDragGestures(
                            onDragStart = { offset ->
                                val touchRadius = 60.dp.toPx()
                                val left = cropRect.left
                                val right = cropRect.right
                                val top = cropRect.top
                                val bottom = cropRect.bottom
                                
                                dragHandle = when {
                                    offset.x in (left - touchRadius)..(left + touchRadius) && offset.y in (top - touchRadius)..(top + touchRadius) -> "topLeft"
                                    offset.x in (right - touchRadius)..(right + touchRadius) && offset.y in (top - touchRadius)..(top + touchRadius) -> "topRight"
                                    offset.x in (left - touchRadius)..(left + touchRadius) && offset.y in (bottom - touchRadius)..(bottom + touchRadius) -> "bottomLeft"
                                    offset.x in (right - touchRadius)..(right + touchRadius) && offset.y in (bottom - touchRadius)..(bottom + touchRadius) -> "bottomRight"
                                    offset.x in left..right && offset.y in top..bottom -> "center"
                                    else -> null
                                }
                            },
                            onDragEnd = { dragHandle = null },
                            onDragCancel = { dragHandle = null },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                when (dragHandle) {
                                    "topLeft" -> cropRect = Rect(cropRect.left + dragAmount.x, cropRect.top + dragAmount.y, cropRect.right, cropRect.bottom)
                                    "topRight" -> cropRect = Rect(cropRect.left, cropRect.top + dragAmount.y, cropRect.right + dragAmount.x, cropRect.bottom)
                                    "bottomLeft" -> cropRect = Rect(cropRect.left + dragAmount.x, cropRect.top, cropRect.right, cropRect.bottom + dragAmount.y)
                                    "bottomRight" -> cropRect = Rect(cropRect.left, cropRect.top, cropRect.right + dragAmount.x, cropRect.bottom + dragAmount.y)
                                    "center" -> cropRect = cropRect.translate(dragAmount.x, dragAmount.y)
                                }
                                
                                // Enforce minimum size and constraints
                                if (cropRect.width < 50f) cropRect = Rect(cropRect.left, cropRect.top, cropRect.left + 50f, cropRect.bottom)
                                if (cropRect.height < 50f) cropRect = Rect(cropRect.left, cropRect.top, cropRect.right, cropRect.top + 50f)
                            }
                        )
                    }
                }
            ) {
                val dimColor = Color.Black.copy(alpha = 0.5f)
                val path = androidx.compose.ui.graphics.Path().apply {
                    addRect(androidx.compose.ui.geometry.Rect(0f, 0f, size.width, size.height))
                    if (cropShape == "circle") {
                        addOval(cropRect)
                    } else if (cropShape == "polygon") {
                        if (polygonPoints.isNotEmpty()) {
                            moveTo(polygonPoints.first().x, polygonPoints.first().y)
                            for (i in 1 until polygonPoints.size) {
                                lineTo(polygonPoints[i].x, polygonPoints[i].y)
                            }
                            close()
                        }
                    } else {
                        addRect(cropRect)
                    }
                    fillType = androidx.compose.ui.graphics.PathFillType.EvenOdd
                }
                drawPath(path, dimColor)
                
                val hs = handleSize.toPx() / 2
                if (cropShape == "circle") {
                    drawOval(Color.Green, cropRect.topLeft, cropRect.size, style = Stroke(width = 4.dp.toPx()))
                } else if (cropShape == "polygon") {
                    if (polygonPoints.isNotEmpty()) {
                        val polyPath = androidx.compose.ui.graphics.Path().apply {
                            moveTo(polygonPoints.first().x, polygonPoints.first().y)
                            for (i in 1 until polygonPoints.size) {
                                lineTo(polygonPoints[i].x, polygonPoints[i].y)
                            }
                            close()
                        }
                        drawPath(polyPath, Color.Green, style = Stroke(width = 4.dp.toPx()))
                        polygonPoints.forEach { pt ->
                            drawCircle(Color.Green, radius = hs, center = pt)
                        }
                    }
                } else {
                    drawRect(Color.Green, cropRect.topLeft, cropRect.size, style = Stroke(width = 4.dp.toPx()))
                }
                
                // Draw handles for square and circle
                if (cropShape != "polygon") {
                    drawCircle(Color.Green, radius = hs, center = cropRect.topLeft)
                    drawCircle(Color.Green, radius = hs, center = cropRect.topRight)
                    drawCircle(Color.Green, radius = hs, center = cropRect.bottomLeft)
                    drawCircle(Color.Green, radius = hs, center = cropRect.bottomRight)
                }
            }
        }
        
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FloatingActionButton(onClick = { cropShape = "square" }, containerColor = if (cropShape == "square") Color.Green else Color.Gray) {
                Text("Square", modifier = Modifier.padding(8.dp))
            }
            FloatingActionButton(onClick = { cropShape = "circle" }, containerColor = if (cropShape == "circle") Color.Green else Color.Gray) {
                Text("Circle", modifier = Modifier.padding(8.dp))
            }
            FloatingActionButton(onClick = { cropShape = "polygon" }, containerColor = if (cropShape == "polygon") Color.Green else Color.Gray) {
                Text("Polygon", modifier = Modifier.padding(8.dp))
            }
            if (cropShape == "polygon" && polygonPoints.isNotEmpty()) {
                FloatingActionButton(onClick = { polygonPoints.clear() }, containerColor = Color.Red) {
                    Text("Clear", modifier = Modifier.padding(8.dp), color = Color.White)
                }
            }
        }
        
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.7f))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = onClose, colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)) {
                Text("Cancel")
            }
            Button(onClick = {
                val imgRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
                val viewRatio = viewSize.width.toFloat() / viewSize.height.toFloat()
                var renderedW = viewSize.width.toFloat()
                var renderedH = viewSize.height.toFloat()
                var offsetX = 0f
                var offsetY = 0f
                if (imgRatio > viewRatio) {
                    renderedH = viewSize.width / imgRatio
                    offsetY = (viewSize.height - renderedH) / 2f
                } else {
                    renderedW = viewSize.height * imgRatio
                    offsetX = (viewSize.width - renderedW) / 2f
                }
                val scale = bitmap.width / renderedW
                val minX = polygonPoints.minOfOrNull { it.x } ?: 0f
                val maxX = polygonPoints.maxOfOrNull { it.x } ?: 0f
                val minY = polygonPoints.minOfOrNull { it.y } ?: 0f
                val maxY = polygonPoints.maxOfOrNull { it.y } ?: 0f
                val rectToUse = if (cropShape == "polygon" && polygonPoints.isNotEmpty()) Rect(minX, minY, maxX, maxY) else cropRect
                
                val realX = (rectToUse.left - offsetX) * scale
                val realY = (rectToUse.top - offsetY) * scale
                val realW = rectToUse.width * scale
                val realH = rectToUse.height * scale
                val mappedPoints = polygonPoints.map { Offset((it.x - offsetX) * scale, (it.y - offsetY) * scale) }
                onAction("share", realX, realY, realW, realH, cropShape, mappedPoints)
            }) {
                Text("Share")
            }
            
            Button(onClick = {
                val imgRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
                val viewRatio = viewSize.width.toFloat() / viewSize.height.toFloat()
                var renderedW = viewSize.width.toFloat()
                var renderedH = viewSize.height.toFloat()
                var offsetX = 0f
                var offsetY = 0f
                if (imgRatio > viewRatio) {
                    renderedH = viewSize.width / imgRatio
                    offsetY = (viewSize.height - renderedH) / 2f
                } else {
                    renderedW = viewSize.height * imgRatio
                    offsetX = (viewSize.width - renderedW) / 2f
                }
                val scale = bitmap.width / renderedW
                val minX = polygonPoints.minOfOrNull { it.x } ?: 0f
                val maxX = polygonPoints.maxOfOrNull { it.x } ?: 0f
                val minY = polygonPoints.minOfOrNull { it.y } ?: 0f
                val maxY = polygonPoints.maxOfOrNull { it.y } ?: 0f
                val rectToUse = if (cropShape == "polygon" && polygonPoints.isNotEmpty()) Rect(minX, minY, maxX, maxY) else cropRect
                
                val realX = (rectToUse.left - offsetX) * scale
                val realY = (rectToUse.top - offsetY) * scale
                val realW = rectToUse.width * scale
                val realH = rectToUse.height * scale
                val mappedPoints = polygonPoints.map { Offset((it.x - offsetX) * scale, (it.y - offsetY) * scale) }
                onAction("scan", realX, realY, realW, realH, cropShape, mappedPoints)
            }) {
                Text("Scan QR")
            }
            Button(onClick = {
                val imgRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
                val viewRatio = viewSize.width.toFloat() / viewSize.height.toFloat()
                var renderedW = viewSize.width.toFloat()
                var renderedH = viewSize.height.toFloat()
                var offsetX = 0f
                var offsetY = 0f
                if (imgRatio > viewRatio) {
                    renderedH = viewSize.width / imgRatio
                    offsetY = (viewSize.height - renderedH) / 2f
                } else {
                    renderedW = viewSize.height * imgRatio
                    offsetX = (viewSize.width - renderedW) / 2f
                }
                val scale = bitmap.width / renderedW
                val minX = polygonPoints.minOfOrNull { it.x } ?: 0f
                val maxX = polygonPoints.maxOfOrNull { it.x } ?: 0f
                val minY = polygonPoints.minOfOrNull { it.y } ?: 0f
                val maxY = polygonPoints.maxOfOrNull { it.y } ?: 0f
                val rectToUse = if (cropShape == "polygon" && polygonPoints.isNotEmpty()) Rect(minX, minY, maxX, maxY) else cropRect
                val realX = (rectToUse.left - offsetX) * scale
                val realY = (rectToUse.top - offsetY) * scale
                val realW = rectToUse.width * scale
                val realH = rectToUse.height * scale
                val mappedPoints = polygonPoints.map { Offset((it.x - offsetX) * scale, (it.y - offsetY) * scale) }
                onAction("ocr", realX, realY, realW, realH, cropShape, mappedPoints)
            }) {
                Text("OCR")
            }
        }
    }
}

