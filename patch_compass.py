filepath = 'app/src/main/java/com/example/feature/sidebar/CompassPageView.kt'
with open(filepath, 'r') as f:
    content = f.read()

old_class = """class CompassPageView(context: Context) : FrameLayout(context), SensorEventListener {"""
new_class = """class CompassPageView(context: Context) : FrameLayout(context), SensorEventListener, SidebarPageControllable {"""
content = content.replace(old_class, new_class)

old_attached = """    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        accelerometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        magnetometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
    }"""
new_attached = """    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // We defer to onPageSelected for battery saving, but if it's not managed by ViewPager, we fallback here
    }
    
    override fun onPageSelected() {
        accelerometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        magnetometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
    }
    
    override fun onPageUnselected() {
        sensorManager.unregisterListener(this)
    }"""
content = content.replace(old_attached, new_attached)

with open(filepath, 'w') as f:
    f.write(content)
print("Updated CompassPageView")
