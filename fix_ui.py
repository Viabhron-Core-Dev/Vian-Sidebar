import re

with open('app/src/main/java/com/example/feature/sidebar/SidebarView.kt', 'r') as f:
    content = f.read()

bg_code_old = """        val drawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            val colorHex = prefs.getString("handle_${containerId}_sidebar_color", prefs.getString("sidebar_color", "#000000")) ?: "#000000"
            val baseColor = try { Color.parseColor(colorHex) } catch(e:Exception){ Color.BLACK }
            val r = Color.red(baseColor)
            val g = Color.green(baseColor)
            val b = Color.blue(baseColor)
            setColor(Color.argb(alphaInt, r, g, b))
            
            cornerRadii = if (isRight) {
                floatArrayOf(32f, 32f, 0f, 0f, 0f, 0f, 32f, 32f)
            } else {
                floatArrayOf(0f, 0f, 32f, 32f, 32f, 32f, 0f, 0f)
            }
        }"""

bg_code_new = """        val drawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            val colorHex = prefs.getString("handle_${containerId}_sidebar_color", prefs.getString("sidebar_color", "#1E1E2E")) ?: "#1E1E2E"
            val baseColor = try { Color.parseColor(colorHex) } catch(e:Exception){ Color.parseColor("#1E1E2E") }
            val r = Color.red(baseColor)
            val g = Color.green(baseColor)
            val b = Color.blue(baseColor)
            setColor(Color.argb(alphaInt, r, g, b))
            
            setStroke((1 * density).toInt(), Color.argb(80, 255, 255, 255))
            
            val radius = 16f * density
            cornerRadii = if (isRight) {
                floatArrayOf(radius, radius, 0f, 0f, 0f, 0f, radius, radius)
            } else {
                floatArrayOf(0f, 0f, radius, radius, radius, radius, 0f, 0f)
            }
        }"""

content = content.replace(bg_code_old, bg_code_new)

with open('app/src/main/java/com/example/feature/sidebar/SidebarView.kt', 'w') as f:
    f.write(content)
