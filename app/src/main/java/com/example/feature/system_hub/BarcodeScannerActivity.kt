package com.example.feature.system_hub

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
class BarcodeScannerActivity : ComponentActivity() {

    private lateinit var cameraExecutor: ExecutorService
    private var imageCapture: ImageCapture? = null
    private var cameraControl: CameraControl? = null
    private var isTorchEnabled = false

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(this, "Camera permission required for Secure Camera Scanner", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        setContent {
            var isTextMode by remember { mutableStateOf(false) }
            var scannedText by remember { mutableStateOf("") }
            var capturedPhoto by remember { mutableStateOf<Bitmap?>(null) }
            var isFlashOn by remember { mutableStateOf(false) }

            MaterialTheme(colorScheme = darkColorScheme()) {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(if (capturedPhoto != null) "Tap-to-Tap Crop & OCR" else "Secure Camera Scanner") },
                            navigationIcon = {
                                IconButton(onClick = {
                                    if (capturedPhoto != null) {
                                        capturedPhoto = null
                                    } else {
                                        finish()
                                    }
                                }) {
                                    Icon(Icons.Filled.ArrowBack, "Back")
                                }
                            },
                            actions = {
                                if (capturedPhoto == null) {
                                    IconButton(onClick = {
                                        isFlashOn = !isFlashOn
                                        isTorchEnabled = isFlashOn
                                        cameraControl?.enableTorch(isFlashOn)
                                    }) {
                                        Icon(
                                            if (isFlashOn) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                                            contentDescription = "Flash"
                                        )
                                    }
                                    FilterChip(
                                        selected = isTextMode,
                                        onClick = { isTextMode = !isTextMode },
                                        label = { Text(if (isTextMode) "Text OCR" else "Barcode") },
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                }
                            }
                        )
                    }
                ) { padding ->
                    Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                        if (capturedPhoto != null) {
                            CameraPhotoCropScreen(
                                bitmap = capturedPhoto!!,
                                onAction = { action, x, y, width, height, shape, points ->
                                    when (action) {
                                        "ocr" -> ocrCroppedArea(capturedPhoto!!, x, y, width, height, shape, points)
                                        "scan" -> scanCroppedArea(capturedPhoto!!, x, y, width, height, shape, points)
                                        "share" -> shareCroppedArea(capturedPhoto!!, x, y, width, height, shape, points)
                                    }
                                },
                                onRetake = { capturedPhoto = null }
                            )
                        } else {
                            CameraLiveView(
                                modifier = Modifier.fillMaxSize(),
                                isTextMode = isTextMode,
                                onResult = { result ->
                                    scannedText = result
                                },
                                onCameraReady = { control, capture ->
                                    cameraControl = control
                                    imageCapture = capture
                                }
                            )

                            // Shutter button for Tap-to-Tap Photo OCR
                            FloatingActionButton(
                                onClick = {
                                    capturePhoto { bmp ->
                                        capturedPhoto = bmp
                                    }
                                },
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 32.dp)
                                    .size(72.dp),
                                shape = CircleShape,
                                containerColor = MaterialTheme.colorScheme.primary
                            ) {
                                Icon(
                                    Icons.Filled.CameraAlt,
                                    contentDescription = "Capture for Tap-to-Tap OCR",
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            if (scannedText.isNotEmpty()) {
                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.TopCenter)
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    color = Color.Black.copy(alpha = 0.85f),
                                    shape = MaterialTheme.shapes.medium
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = scannedText,
                                            color = Color.White,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            TextButton(onClick = { scannedText = "" }) {
                                                Text("Dismiss")
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Button(
                                                onClick = {
                                                    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                    clipboard.setPrimaryClip(ClipData.newPlainText("Scanned", scannedText))
                                                    Toast.makeText(this@BarcodeScannerActivity, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                                }
                                            ) {
                                                Text("Copy")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun capturePhoto(onCaptured: (Bitmap) -> Unit) {
        val capture = imageCapture ?: run {
            Toast.makeText(this, "Camera not ready", Toast.LENGTH_SHORT).show()
            return
        }

        val photoFile = File(cacheDir, "camera_capture_${System.currentTimeMillis()}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        capture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                    if (bitmap != null) {
                        onCaptured(bitmap)
                    } else {
                        Toast.makeText(this@BarcodeScannerActivity, "Failed to decode photo", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("SecureScanner", "Photo capture failed: ${exception.message}", exception)
                    Toast.makeText(this@BarcodeScannerActivity, "Capture failed", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    @Composable
    fun CameraLiveView(
        modifier: Modifier = Modifier,
        isTextMode: Boolean,
        onResult: (String) -> Unit,
        onCameraReady: (CameraControl, ImageCapture) -> Unit
    ) {
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current

        AndroidView(
            modifier = modifier,
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val capture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build()

                    val imageAnalyzer = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                            it.setAnalyzer(cameraExecutor) { imageProxy ->
                                processLiveFrame(imageProxy, isTextMode, onResult)
                            }
                        }

                    try {
                        cameraProvider.unbindAll()
                        val camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            capture,
                            imageAnalyzer
                        )
                        onCameraReady(camera.cameraControl, capture)

                        previewView.setOnTouchListener { _, event ->
                            if (event.action == MotionEvent.ACTION_UP) {
                                val factory = previewView.meteringPointFactory
                                val point = factory.createPoint(event.x, event.y)
                                val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE)
                                    .setAutoCancelDuration(3, java.util.concurrent.TimeUnit.SECONDS)
                                    .build()
                                camera.cameraControl.startFocusAndMetering(action)
                            }
                            true
                        }
                    } catch (e: Exception) {
                        Log.e("SecureCameraScanner", "Camera binding failed", e)
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            }
        )
    }

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    private fun processLiveFrame(imageProxy: ImageProxy, isTextMode: Boolean, onResult: (String) -> Unit) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            if (isTextMode) {
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        if (visionText.text.isNotBlank()) {
                            onResult(visionText.text)
                        }
                    }
                    .addOnCompleteListener {
                        imageProxy.close()
                    }
            } else {
                val scanner = BarcodeScanning.getClient()
                scanner.process(image)
                    .addOnSuccessListener { barcodes ->
                        if (barcodes.isNotEmpty()) {
                            val value = barcodes.first().displayValue ?: ""
                            if (value.isNotEmpty()) {
                                onResult(value)
                            }
                        }
                    }
                    .addOnCompleteListener {
                        imageProxy.close()
                    }
            }
        } else {
            imageProxy.close()
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
            val image = InputImage.fromBitmap(croppedBitmap, 0)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val text = visionText.text
                    if (text.isNotEmpty()) {
                        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("OCR Text", text))
                        Toast.makeText(this, "Copied OCR text: $text", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, "No text found in selected area", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    val msg = if (e.message?.contains("optional module") == true || e.message?.contains("Waiting for") == true || e.message?.contains("unavailable") == true) {
                        "OCR engine not downloaded. Please download it from Record Settings."
                    } else {
                        "OCR failed: ${e.message}"
                    }
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Crop OCR failed", Toast.LENGTH_SHORT).show()
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
                    if (!text.isNullOrEmpty()) {
                        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Scanned Code", text))
                        Toast.makeText(this, "Scanned: $text (Copied)", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, "No code found in selected area", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "No barcode/QR code found in selection", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun shareCroppedArea(bitmap: Bitmap, x: Float, y: Float, w: Float, h: Float, shape: String, points: List<Offset>) {
        val cropX = maxOf(0, x.toInt())
        val cropY = maxOf(0, y.toInt())
        val cropW = minOf(bitmap.width - cropX, w.toInt())
        val cropH = minOf(bitmap.height - cropY, h.toInt())
        if (cropW <= 0 || cropH <= 0) return

        try {
            val croppedBitmap = Bitmap.createBitmap(bitmap, cropX, cropY, cropW, cropH)
            val file = File(cacheDir, "shared_crop_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { out ->
                croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "Share Cropped Area"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error sharing cropped area", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}

@Composable
fun CameraPhotoCropScreen(
    bitmap: Bitmap,
    onAction: (String, Float, Float, Float, Float, String, List<Offset>) -> Unit,
    onRetake: () -> Unit
) {
    var viewSize by remember { mutableStateOf(IntSize.Zero) }
    var cropRect by remember { mutableStateOf(Rect.Zero) }
    var cropShape by remember { mutableStateOf("square") }
    val polygonPoints = remember { mutableStateListOf<Offset>() }

    Box(modifier = Modifier.fillMaxSize().onSizeChanged { size ->
        viewSize = size
        if (cropRect == Rect.Zero && size.width > 0 && size.height > 0) {
            val boxSize = size.width * 0.65f
            cropRect = Rect(
                offset = Offset((size.width - boxSize) / 2f, (size.height - boxSize) / 2f),
                size = Size(boxSize, boxSize)
            )
        }
    }) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Captured Photo",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        if (viewSize.width > 0) {
            val handleSize = 36.dp

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
                                val touchRadius = 50.dp.toPx()
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
                                if (cropRect.width < 50f) cropRect = Rect(cropRect.left, cropRect.top, cropRect.left + 50f, cropRect.bottom)
                                if (cropRect.height < 50f) cropRect = Rect(cropRect.left, cropRect.top, cropRect.right, cropRect.top + 50f)
                            }
                        )
                    }
                }
            ) {
                val dimColor = Color.Black.copy(alpha = 0.55f)
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
                    drawOval(Color.Cyan, cropRect.topLeft, cropRect.size, style = Stroke(width = 3.dp.toPx()))
                } else if (cropShape == "polygon") {
                    if (polygonPoints.isNotEmpty()) {
                        val polyPath = androidx.compose.ui.graphics.Path().apply {
                            moveTo(polygonPoints.first().x, polygonPoints.first().y)
                            for (i in 1 until polygonPoints.size) {
                                lineTo(polygonPoints[i].x, polygonPoints[i].y)
                            }
                            close()
                        }
                        drawPath(polyPath, Color.Cyan, style = Stroke(width = 3.dp.toPx()))
                        polygonPoints.forEach { pt ->
                            drawCircle(Color.Cyan, radius = hs, center = pt)
                        }
                    }
                } else {
                    drawRect(Color.Cyan, cropRect.topLeft, cropRect.size, style = Stroke(width = 3.dp.toPx()))
                }

                if (cropShape != "polygon") {
                    val corners = listOf(cropRect.topLeft, cropRect.topRight, cropRect.bottomLeft, cropRect.bottomRight)
                    corners.forEach { corner ->
                        drawCircle(Color.Cyan, radius = hs, center = corner)
                    }
                }
            }

            // Bottom toolbar
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                color = Color.Black.copy(alpha = 0.85f),
                shape = MaterialTheme.shapes.large
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        FilterChip(
                            selected = cropShape == "square",
                            onClick = { cropShape = "square" },
                            label = { Text("Box") }
                        )
                        FilterChip(
                            selected = cropShape == "circle",
                            onClick = { cropShape = "circle" },
                            label = { Text("Oval") }
                        )
                        FilterChip(
                            selected = cropShape == "polygon",
                            onClick = {
                                cropShape = "polygon"
                                polygonPoints.clear()
                            },
                            label = { Text("Tap-to-Tap") }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = {
                                val (x, y, w, h) = calculateBitmapCrop(bitmap, viewSize, cropRect, cropShape, polygonPoints)
                                onAction("ocr", x, y, w, h, cropShape, polygonPoints)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("OCR Text")
                        }
                        Button(
                            onClick = {
                                val (x, y, w, h) = calculateBitmapCrop(bitmap, viewSize, cropRect, cropShape, polygonPoints)
                                onAction("scan", x, y, w, h, cropShape, polygonPoints)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text("Scan Code")
                        }
                        OutlinedButton(
                            onClick = {
                                val (x, y, w, h) = calculateBitmapCrop(bitmap, viewSize, cropRect, cropShape, polygonPoints)
                                onAction("share", x, y, w, h, cropShape, polygonPoints)
                            }
                        ) {
                            Text("Share")
                        }
                    }
                }
            }
        }
    }
}

private fun calculateBitmapCrop(
    bitmap: Bitmap,
    viewSize: IntSize,
    cropRect: Rect,
    cropShape: String,
    polygonPoints: List<Offset>
): FloatArray {
    val scaleX = bitmap.width.toFloat() / viewSize.width.toFloat()
    val scaleY = bitmap.height.toFloat() / viewSize.height.toFloat()
    val scale = maxOf(scaleX, scaleY)

    val imageRenderedWidth = bitmap.width / scale
    val imageRenderedHeight = bitmap.height / scale

    val offsetX = (viewSize.width - imageRenderedWidth) / 2f
    val offsetY = (viewSize.height - imageRenderedHeight) / 2f

    val targetRect = if (cropShape == "polygon" && polygonPoints.isNotEmpty()) {
        val minX = polygonPoints.minOf { it.x }
        val maxX = polygonPoints.maxOf { it.x }
        val minY = polygonPoints.minOf { it.y }
        val maxY = polygonPoints.maxOf { it.y }
        Rect(minX, minY, maxX, maxY)
    } else {
        cropRect
    }

    val cropX = ((targetRect.left - offsetX) * scale).coerceAtLeast(0f)
    val cropY = ((targetRect.top - offsetY) * scale).coerceAtLeast(0f)
    val cropW = (targetRect.width * scale).coerceAtMost(bitmap.width - cropX)
    val cropH = (targetRect.height * scale).coerceAtMost(bitmap.height - cropY)

    return floatArrayOf(cropX, cropY, cropW, cropH)
}
