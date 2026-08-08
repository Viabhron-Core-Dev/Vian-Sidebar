package com.example

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.os.Bundle
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min

class IconPickerActivity : Activity() {
    private val PICK_IMAGE_REQ = 1001
    private var itemId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        itemId = intent.getStringExtra("item_id") ?: ""
        if (itemId.isEmpty()) {
            finish()
            return
        }
        val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        try {
            startActivityForResult(pickIntent, PICK_IMAGE_REQ)
        } catch (e: Exception) {
            e.printStackTrace()
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQ && resultCode == RESULT_OK && data != null) {
            val uri = data.data
            if (uri != null) {
                try {
                    val inputStream = contentResolver.openInputStream(uri)
                    val originalBitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()
                    
                    if (originalBitmap != null) {
                        val size = min(originalBitmap.width, originalBitmap.height)
                        val x = (originalBitmap.width - size) / 2
                        val y = (originalBitmap.height - size) / 2
                        val squareBitmap = Bitmap.createBitmap(originalBitmap, x, y, size, size)
                        
                        val roundedBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
                        val canvas = Canvas(roundedBitmap)
                        val paint = Paint()
                        val rect = Rect(0, 0, size, size)
                        val rectF = RectF(rect)
                        val roundPx = size * 0.25f
                        
                        paint.isAntiAlias = true
                        canvas.drawARGB(0, 0, 0, 0)
                        paint.color = -0x1000000
                        canvas.drawRoundRect(rectF, roundPx, roundPx, paint)
                        
                        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
                        canvas.drawBitmap(squareBitmap, rect, rect, paint)
                        
                        val finalBitmap = Bitmap.createScaledBitmap(roundedBitmap, 144, 144, true)
                        
                        val dir = File(filesDir, "custom_icons")
                        if (!dir.exists()) dir.mkdirs()
                        val safeId = itemId.replace(Regex("[^a-zA-Z0-9.-]"), "_")
                        val file = File(dir, "$safeId.webp")
                        val out = FileOutputStream(file)
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                            finalBitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, 90, out)
                        } else {
                            @Suppress("DEPRECATION")
                            finalBitmap.compress(Bitmap.CompressFormat.WEBP, 90, out)
                        }
                        out.flush()
                        out.close()
                        
                        sendBroadcast(Intent("com.example.UPDATE_SIDEBAR_ICONS").apply {
                            putExtra("item_id", itemId)
                        })
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        finish()
    }
}
