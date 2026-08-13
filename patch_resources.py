with open('app/src/main/java/com/example/feature/sidebar/ResourcesTrackerPageView.kt', 'r') as f:
    text = f.read()

text = text.replace('class ResourcesTrackerPageView(context: Context, private val scope: CoroutineScope) : FrameLayout(context), Choreographer.FrameCallback {', 
'''class ResourcesTrackerPageView(context: Context, private val scope: CoroutineScope) : FrameLayout(context), Choreographer.FrameCallback, SidebarPageControllable {''')

old_attached = """    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isTracking = true
        Choreographer.getInstance().postFrameCallback(this)
        
        scope.launch(Dispatchers.Main) {
            while(isTracking) {
                updateMemoryStats()
                delay(1000)
            }
        }
        
        scope.launch(Dispatchers.Main) {
            ActiveAppTracker.activeApps.collect { apps ->
                adapter.submitList(apps)
            }
        }
    }
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        isTracking = false
        Choreographer.getInstance().removeFrameCallback(this)
    }"""

new_attached = """    private var trackingJob: Job? = null
    private var appsJob: Job? = null

    override fun onPageSelected() {
        if (isTracking) return
        isTracking = true
        Choreographer.getInstance().postFrameCallback(this)
        
        trackingJob = scope.launch(Dispatchers.Main) {
            while(isTracking) {
                updateMemoryStats()
                delay(1000)
            }
        }
        
        appsJob = scope.launch(Dispatchers.Main) {
            ActiveAppTracker.activeApps.collect { apps ->
                adapter.submitList(apps)
            }
        }
    }
    
    override fun onPageUnselected() {
        isTracking = false
        Choreographer.getInstance().removeFrameCallback(this)
        trackingJob?.cancel()
        appsJob?.cancel()
        trackingJob = null
        appsJob = null
    }"""

text = text.replace(old_attached, new_attached)

with open('app/src/main/java/com/example/feature/sidebar/ResourcesTrackerPageView.kt', 'w') as f:
    f.write(text)
