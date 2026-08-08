import re

with open('app/src/main/java/com/example/service/FolderStyleDialog.kt', 'r') as f:
    content = f.read()

old_draw = """    override fun draw(canvas: Canvas) {
        val w = bounds.width().toFloat()
        val h = bounds.height().toFloat()
        val cx = bounds.centerX().toFloat()
        val cy = bounds.centerY().toFloat()
        
        // Draw circular background (like Lawnchair)
        paint.color = android.graphics.Color.parseColor("#33FFFFFF") // Translucent white/gray
        paint.style = Paint.Style.FILL
        val bgRadius = minOf(w, h) * 0.45f
        canvas.drawCircle(cx, cy, bgRadius, paint)
        
        // Reset paint
        paint.style = Paint.Style.FILL
        paint.alpha = 255

        val symbolSize = w * 0.75f
        val sx = cx - symbolSize / 2f
        val sy = cy - symbolSize / 2f"""
        
new_draw = """    override fun draw(canvas: Canvas) {
        val w = bounds.width().toFloat()
        val h = bounds.height().toFloat()
        val cx = bounds.centerX().toFloat()
        val cy = bounds.centerY().toFloat()
        
        // Reset paint
        paint.style = Paint.Style.FILL
        paint.alpha = 255

        val symbolSize = w * 0.9f
        val sx = cx - symbolSize / 2f
        val sy = cy - symbolSize / 2f"""
content = content.replace(old_draw, new_draw)

old_stack = """    private fun drawStack(canvas: Canvas, x: Float, y: Float, size: Float) {
        if (miniIcons.isEmpty()) {
            return
        }
        val count = minOf(3, miniIcons.size)
        val cardSize = size * 0.75f
        val gapX = if (count > 1) (size - cardSize) / (count - 1) else 0f
        val gapY = if (count > 1) (size - cardSize) / (count - 1) else 0f
        
        for (i in count - 1 downTo 0) {
            // i=2 (back) drawn first at top-left
            // i=0 (front) drawn last at bottom-right
            val tx = x + (count - 1 - i) * gapX
            val ty = y + (count - 1 - i) * gapY
            canvas.drawBitmap(miniIcons[i], null, RectF(tx, ty, tx + cardSize, ty + cardSize), iconPaint)
        }
    }"""
new_stack = """    private fun drawStack(canvas: Canvas, x: Float, y: Float, size: Float) {
        if (miniIcons.isEmpty()) {
            return
        }
        val count = minOf(3, miniIcons.size)
        val cardSize = size * 0.85f
        val gapX = if (count > 1) (size - cardSize) / (count - 1) else 0f
        val gapY = if (count > 1) (size - cardSize) / (count - 1) else 0f
        
        for (i in count - 1 downTo 0) {
            // i=2 (back) drawn first at top-left
            // i=0 (front) drawn last at bottom-right
            val tx = x + (count - 1 - i) * gapX
            val ty = y + (count - 1 - i) * gapY
            canvas.drawBitmap(miniIcons[i], null, RectF(tx, ty, tx + cardSize, ty + cardSize), iconPaint)
        }
    }"""
content = content.replace(old_stack, new_stack)

with open('app/src/main/java/com/example/service/FolderStyleDialog.kt', 'w') as f:
    f.write(content)
