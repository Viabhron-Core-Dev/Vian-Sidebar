import re

with open('app/src/main/java/com/example/core/HandleService.kt', 'r') as f:
    text = f.read()

# Add netSpeedManager and screenStateReceiver properties
imports = """import com.example.feature.miniapps.reader.ReaderHandleView"""
new_imports = """import com.example.feature.miniapps.reader.ReaderHandleView
import com.example.core.NetSpeedManager
import com.example.feature.system_hub.CallRecorderManager"""
text = text.replace(imports, new_imports)

props = """    private var readerHandleView: ReaderHandleView? = null"""
new_props = """    private var readerHandleView: ReaderHandleView? = null
    private var netSpeedManager: NetSpeedManager? = null
    private var callRecorderManager: CallRecorderManager? = null
    private var screenStateReceiver: BroadcastReceiver? = null
    private var downSpeed: Long = 0
    private var upSpeed: Long = 0"""
text = text.replace(props, new_props)

# In onCreate, setup the managers
on_create = """        if (prefs.getBoolean("reader_handle_enabled", false)) {
            readerHandleView?.attach()
        }

        reloadHandles()
    }"""
new_on_create = """        if (prefs.getBoolean("reader_handle_enabled", false)) {
            readerHandleView?.attach()
        }

        setupNetSpeed()
        setupCallRecorder()
        setupScreenStateReceiver()
        reloadHandles()
    }"""
text = text.replace(on_create, new_on_create)

# In onDestroy, stop managers
on_destroy = """    override fun onDestroy() {
        super.onDestroy()
        triggerHandleViews.forEach { it.detach() }
        triggerHandleViews.clear()
        readerHandleView?.detach()
        unregisterReceiver(reloadReceiver)
    }"""
new_on_destroy = """    override fun onDestroy() {
        super.onDestroy()
        triggerHandleViews.forEach { it.detach() }
        triggerHandleViews.clear()
        readerHandleView?.detach()
        unregisterReceiver(reloadReceiver)
        screenStateReceiver?.let { unregisterReceiver(it) }
        netSpeedManager?.stop()
        callRecorderManager?.stopListening()
    }"""
text = text.replace(on_destroy, new_on_destroy)

# Add setup methods
extra_methods = """
    private fun setupNetSpeed() {
        if (prefs.getBoolean("netspeed_enabled", false)) {
            if (netSpeedManager == null) {
                netSpeedManager = NetSpeedManager(this, prefs, 
                    onSpeedUpdate = { down, up ->
                        downSpeed = down
                        upSpeed = up
                        updateForegroundNotification()
                    },
                    onDailyDataUpdate = { _, _ -> }
                )
            }
            netSpeedManager?.start()
        } else {
            netSpeedManager?.stop()
            downSpeed = 0
            upSpeed = 0
            updateForegroundNotification()
        }
    }

    private fun setupCallRecorder() {
        if (callRecorderManager == null) {
            callRecorderManager = CallRecorderManager(this, prefs)
        }
        if (prefs.getBoolean("call_recorder_enabled", false) || prefs.getBoolean("call_recorder_manual_enabled", false)) {
            callRecorderManager?.startListening()
        } else {
            callRecorderManager?.stopListening()
        }
    }

    private fun setupScreenStateReceiver() {
        screenStateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    Intent.ACTION_SCREEN_ON -> {
                        if (prefs.getBoolean("netspeed_enabled", false)) {
                            netSpeedManager?.start()
                        }
                    }
                    Intent.ACTION_SCREEN_OFF -> {
                        netSpeedManager?.stop()
                    }
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        registerReceiver(screenStateReceiver, filter)
    }
    
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == "netspeed_enabled") {
            setupNetSpeed()
        } else if (key == "call_recorder_enabled" || key == "call_recorder_manual_enabled") {
            setupCallRecorder()
        }
    }
    
    private fun formatSpeed(bytes: Long): String {
        if (bytes < 1024) return "${bytes} B/s"
        val kb = bytes / 1024.0
        if (kb < 1024) return String.format("%.1f KB/s", kb)
        val mb = kb / 1024.0
        return String.format("%.1f MB/s", mb)
    }

    private fun updateForegroundNotification() {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
        
        val contentText = if (prefs.getBoolean("netspeed_enabled", false)) {
            "↓ ${formatSpeed(downSpeed)}   ↑ ${formatSpeed(upSpeed)}"
        } else {
            "Listening for edge gestures"
        }
        
        val notification = NotificationCompat.Builder(this, "handle_channel")
            .setContentTitle("Handles Active")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_menu_crop) // Placeholder icon
            .setContentIntent(pendingIntent)
            .build()
        
        val manager = getSystemService(NotificationManager::class.java)
        manager?.notify(2, notification)
    }
"""

text = text.replace('    private fun startForegroundService() {', extra_methods + '\n    private fun startForegroundService() {')

with open('app/src/main/java/com/example/core/HandleService.kt', 'w') as f:
    f.write(text)

