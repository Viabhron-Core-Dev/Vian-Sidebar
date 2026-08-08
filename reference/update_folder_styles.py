import re

def replace_in_file(filename, old, new):
    with open(filename, 'r') as f:
        content = f.read()
    content = content.replace(old, new)
    with open(filename, 'w') as f:
        f.write(content)

replace_in_file('app/src/main/java/com/example/service/AppsPageView.kt', 
                'FolderStyleDrawable(item.items.size', 
                'FolderStyleDrawable(item.folderStyle')

replace_in_file('app/src/main/java/com/example/service/SidebarEditOverlayView.kt', 
                'FolderStyleDrawable(item.items.size', 
                'FolderStyleDrawable(item.folderStyle')

with open('app/src/main/java/com/example/service/FolderStyleDialog.kt', 'r') as f:
    content = f.read()

# Update styles list
styles_old = """    val styles = listOf(
        "Folder", "Stack",
        "Tile", "Action folder",
        "Folder\\nCircle + Background", "Stack\\nCircle + Background",
        "Tile\\nCircle + Background", "Action folder\\nCircle + Background",
        "Folder\\nCircle (Border)", "Stack\\nCircle (Border)",
        "Tile\\nCircle (Border)", "Action folder\\nCircle (Border)"
    )"""

styles_new = """    val styles = listOf(
        "Grid", "Stack"
    )"""
content = content.replace(styles_old, styles_new)

# Update preview
content = content.replace('FolderStyleDrawable(item.items.size', 'FolderStyleDrawable(position')

# Update draw method in FolderStyleDrawable
draw_old = """
        if (styleIndex <= 4) {
            drawGrid(canvas, sx, sy, symbolSize)
        } else {
            drawStack(canvas, sx, sy, symbolSize)
        }
"""
draw_new = """
        if (styleIndex == 1) {
            drawStack(canvas, sx, sy, symbolSize)
        } else {
            drawGrid(canvas, sx, sy, symbolSize)
        }
"""
content = content.replace(draw_old, draw_new)

# Update drawGrid to handle 3x3
grid_old = r'private fun drawGrid\(canvas: Canvas, x: Float, y: Float, size: Float\) \{.*?(?=private fun drawStack)'
grid_new = """private fun drawGrid(canvas: Canvas, x: Float, y: Float, size: Float) {
        if (miniIcons.isEmpty()) return
        val count = miniIcons.size
        
        if (count == 1) {
            val p = size * 0.1f
            val ix = x + p
            val iy = y + p
            val isize = size - 2*p
            canvas.drawBitmap(miniIcons[0], null, RectF(ix, iy, ix + isize, iy + isize), iconPaint)
        } else if (count == 2) {
            val padding = size * 0.05f
            val iconSize = (size - padding * 3) / 2f
            val startY = y + (size - iconSize) / 2f
            for (i in 0 until 2) {
                val ix = x + padding + i * (iconSize + padding)
                canvas.drawBitmap(miniIcons[i], null, RectF(ix, startY, ix + iconSize, startY + iconSize), iconPaint)
            }
        } else if (count <= 4) {
            val padding = size * 0.05f
            val iconSize = (size - padding * 3) / 2f
            for (i in 0 until count) {
                val row = i / 2
                val col = i % 2
                val ix = x + padding + col * (iconSize + padding)
                val iy = y + padding + row * (iconSize + padding)
                canvas.drawBitmap(miniIcons[i], null, RectF(ix, iy, ix + iconSize, iy + iconSize), iconPaint)
            }
        } else {
            // up to 9 (3x3 grid)
            val maxCount = minOf(9, count)
            val padding = size * 0.05f
            val iconSize = (size - padding * 4) / 3f
            for (i in 0 until maxCount) {
                val row = i / 3
                val col = i % 3
                val ix = x + padding + col * (iconSize + padding)
                val iy = y + padding + row * (iconSize + padding)
                canvas.drawBitmap(miniIcons[i], null, RectF(ix, iy, ix + iconSize, iy + iconSize), iconPaint)
            }
        }
    }
    
    """
content = re.sub(grid_old, grid_new, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/service/FolderStyleDialog.kt', 'w') as f:
    f.write(content)

print("Updated folder styles.")
