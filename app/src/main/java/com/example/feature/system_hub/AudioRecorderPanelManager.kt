package com.example.feature.system_hub

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.documentfile.provider.DocumentFile
import com.example.R
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class AudioRecorderPanelManager private constructor(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val prefs = context.getSharedPreferences("audio_record_prefs", Context.MODE_PRIVATE)
    private val appPrefs = context.getSharedPreferences("FloatingReaderPrefs", Context.MODE_PRIVATE)

    private var floatingView: View? = null
    private var layoutParams: WindowManager.LayoutParams? = null

    private var mediaRecorder: MediaRecorder? = null
    private var tempFile: File? = null
    private var isRecording = false
    private var isPaused = false

    private var elapsedSeconds = 0
    private val handler = Handler(Looper.getMainLooper())

    private var btnPausePlay: ImageButton? = null
    private var ivRecStatus: ImageView? = null
    private var tvTimer: TextView? = null
    private var layoutRecordControls: LinearLayout? = null
    private var layoutNameInput: LinearLayout? = null
    private var etRecordingName: EditText? = null

    private val timerRunnable = object : Runnable {
        override fun run() {
            if (isRecording && !isPaused) {
                elapsedSeconds++
                updateTimerText()
                // Blink indicator
                if (elapsedSeconds % 2 == 0) {
                    ivRecStatus?.setColorFilter(Color.RED)
                } else {
                    ivRecStatus?.setColorFilter(Color.parseColor("#FF6666"))
                }
            }
            if (isRecording) {
                handler.postDelayed(this, 1000)
            }
        }
    }

    private fun updateTimerText() {
        val minutes = elapsedSeconds / 60
        val seconds = elapsedSeconds % 60
        tvTimer?.text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    fun isShowing(): Boolean = floatingView != null

    fun toggle() {
        if (isShowing()) {
            close()
        } else {
            show()
        }
    }

    fun show() {
        if (floatingView != null) return

        val inflater = LayoutInflater.from(context)
        floatingView = inflater.inflate(R.layout.overlay_audio_recorder, null)

        val savedX = prefs.getInt("pos_x", 0)
        val savedY = prefs.getInt("pos_y", 160)

        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val lp = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            x = savedX
            y = savedY
        }
        layoutParams = lp

        val view = floatingView ?: return
        layoutRecordControls = view.findViewById(R.id.layout_record_controls)
        layoutNameInput = view.findViewById(R.id.layout_name_input)
        ivRecStatus = view.findViewById(R.id.iv_rec_status)
        tvTimer = view.findViewById(R.id.tv_timer)
        btnPausePlay = view.findViewById(R.id.btn_pause_play)
        val btnNext = view.findViewById<ImageButton>(R.id.btn_next)
        val btnStop = view.findViewById<ImageButton>(R.id.btn_stop)
        val btnClose = view.findViewById<ImageButton>(R.id.btn_close)

        etRecordingName = view.findViewById(R.id.et_recording_name)
        val btnSaveName = view.findViewById<Button>(R.id.btn_save_name)
        val btnCancelName = view.findViewById<ImageButton>(R.id.btn_cancel_name)

        btnPausePlay?.setOnClickListener {
            togglePauseResume()
        }

        btnNext?.setOnClickListener {
            handleNext()
        }

        btnStop?.setOnClickListener {
            handleStop()
        }

        btnClose?.setOnClickListener {
            close()
        }

        btnSaveName?.setOnClickListener {
            val customName = etRecordingName?.text?.toString()?.trim()
            val fileName = if (!customName.isNullOrEmpty()) customName else generateDefaultFileName()
            finalizeAndSaveRecording(fileName)
            close()
        }

        btnCancelName?.setOnClickListener {
            discardTempRecording()
            close()
        }

        // Draggable Touch Listener
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f

        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = lp.x
                    initialY = lp.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    lp.x = initialX + (event.rawX - initialTouchX).toInt()
                    lp.y = initialY - (event.rawY - initialTouchY).toInt()
                    try {
                        windowManager.updateViewLayout(view, lp)
                    } catch (e: Exception) {}
                    true
                }
                MotionEvent.ACTION_UP -> {
                    prefs.edit()
                        .putInt("pos_x", lp.x)
                        .putInt("pos_y", lp.y)
                        .apply()
                    true
                }
                else -> false
            }
        }

        try {
            windowManager.addView(view, lp)
            startNewRecording()
        } catch (e: Exception) {
            Log.e("AudioRecorderPanel", "Failed to add floating view", e)
            floatingView = null
        }
    }

    private fun startNewRecording() {
        try {
            stopRecorderInternal()

            val tempDir = File(context.cacheDir, "audio_recorder_temp")
            if (!tempDir.exists()) tempDir.mkdirs()
            tempFile = File(tempDir, "temp_rec_${System.currentTimeMillis()}.m4a")

            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(tempFile?.absolutePath)
                prepare()
                start()
            }
            mediaRecorder = recorder
            isRecording = true
            isPaused = false
            elapsedSeconds = 0
            updateTimerText()
            btnPausePlay?.setImageResource(android.R.drawable.ic_media_pause)
            ivRecStatus?.setColorFilter(Color.RED)

            handler.removeCallbacks(timerRunnable)
            handler.post(timerRunnable)

            com.example.core.LogKeeper.writeLog("AudioRecorderPanel", "Started recording: ${tempFile?.name}")
        } catch (e: Exception) {
            Log.e("AudioRecorderPanel", "Error starting recorder", e)
            com.example.core.LogKeeper.writeLog("AudioRecorderPanel", "Error starting recording: ${e.message}")
            Toast.makeText(context, "Cannot start recorder: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun togglePauseResume() {
        if (!isRecording) return
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Toast.makeText(context, "Pause/Resume not supported on this Android version", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            if (isPaused) {
                mediaRecorder?.resume()
                isPaused = false
                btnPausePlay?.setImageResource(android.R.drawable.ic_media_pause)
                ivRecStatus?.setColorFilter(Color.RED)
                com.example.core.LogKeeper.writeLog("AudioRecorderPanel", "Recording resumed")
            } else {
                mediaRecorder?.pause()
                isPaused = true
                btnPausePlay?.setImageResource(android.R.drawable.ic_media_play)
                ivRecStatus?.setColorFilter(Color.parseColor("#FFA500")) // Orange
                com.example.core.LogKeeper.writeLog("AudioRecorderPanel", "Recording paused")
            }
        } catch (e: Exception) {
            Log.e("AudioRecorderPanel", "Error toggle pause/resume", e)
        }
    }

    private fun handleNext() {
        if (!isRecording) return
        val defaultName = generateDefaultFileName()
        finalizeAndSaveRecording(defaultName)
        Toast.makeText(context, "Saved: $defaultName\nStarting next recording...", Toast.LENGTH_SHORT).show()
        startNewRecording()
    }

    private fun handleStop() {
        if (!isRecording) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                mediaRecorder?.pause()
                isPaused = true
            } catch (e: Exception) {}
        }
        handler.removeCallbacks(timerRunnable)

        // Switch to naming layout
        layoutRecordControls?.visibility = View.GONE
        layoutNameInput?.visibility = View.VISIBLE

        val defaultName = generateDefaultFileName().removeSuffix(".m4a")
        etRecordingName?.setText(defaultName)
        etRecordingName?.selectAll()

        // Enable focus for keyboard input
        val lp = layoutParams
        val view = floatingView
        if (lp != null && view != null) {
            lp.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            try {
                windowManager.updateViewLayout(view, lp)
            } catch (e: Exception) {}
        }
    }

    private fun generateDefaultFileName(): String {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        return "VOICE_${sdf.format(Date())}.m4a"
    }

    private fun finalizeAndSaveRecording(userChosenName: String) {
        stopRecorderInternal()

        val source = tempFile ?: return
        if (!source.exists() || source.length() <= 0) {
            source.delete()
            return
        }

        var finalName = userChosenName.trim()
        if (!finalName.endsWith(".m4a", ignoreCase = true) && !finalName.endsWith(".3gp", ignoreCase = true)) {
            finalName += ".m4a"
        }

        val saveFolderUriStr = appPrefs.getString("call_recorder_save_folder", null)
        var savedSuccessfully = false

        if (!saveFolderUriStr.isNullOrEmpty()) {
            try {
                val folderUri = Uri.parse(saveFolderUriStr)
                val treeDoc = DocumentFile.fromTreeUri(context, folderUri)
                if (treeDoc != null && treeDoc.exists()) {
                    val destDoc = treeDoc.createFile("audio/mp4", finalName)
                    if (destDoc != null) {
                        context.contentResolver.openOutputStream(destDoc.uri)?.use { out ->
                            FileInputStream(source).use { inp ->
                                inp.copyTo(out)
                            }
                        }
                        savedSuccessfully = true
                        com.example.core.LogKeeper.writeLog("AudioRecorderPanel", "Saved to SAF: $finalName")
                    }
                }
            } catch (e: Exception) {
                Log.e("AudioRecorderPanel", "Failed to save to SAF", e)
            }
        }

        if (!savedSuccessfully) {
            try {
                val recordsDir = File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), ".Records")
                if (!recordsDir.exists()) recordsDir.mkdirs()

                val nomedia = File(recordsDir, ".nomedia")
                if (!nomedia.exists()) nomedia.createNewFile()

                val destFile = File(recordsDir, finalName)
                source.copyTo(destFile, overwrite = true)
                savedSuccessfully = true
                com.example.core.LogKeeper.writeLog("AudioRecorderPanel", "Saved to internal Records: ${destFile.name}")
            } catch (e: Exception) {
                Log.e("AudioRecorderPanel", "Failed to save to recordsDir", e)
            }
        }

        source.delete()
        tempFile = null

        if (savedSuccessfully) {
            Toast.makeText(context, "Saved recording: $finalName", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Failed to save recording", Toast.LENGTH_SHORT).show()
        }
    }

    private fun discardTempRecording() {
        stopRecorderInternal()
        tempFile?.delete()
        tempFile = null
    }

    private fun stopRecorderInternal() {
        handler.removeCallbacks(timerRunnable)
        if (isRecording) {
            try {
                mediaRecorder?.stop()
            } catch (e: Exception) {}
            try {
                mediaRecorder?.release()
            } catch (e: Exception) {}
            mediaRecorder = null
            isRecording = false
            isPaused = false
        }
    }

    fun close() {
        if (isRecording && layoutNameInput?.visibility != View.VISIBLE) {
            // Auto save current file before closing
            finalizeAndSaveRecording(generateDefaultFileName())
        } else {
            discardTempRecording()
        }

        floatingView?.let { view ->
            try {
                if (view.isAttachedToWindow) {
                    windowManager.removeView(view)
                }
            } catch (e: Exception) {}
            floatingView = null
        }
        layoutParams = null
        isRecording = false
        isPaused = false
        elapsedSeconds = 0
    }

    companion object {
        @Volatile
        private var instance: AudioRecorderPanelManager? = null

        fun getInstance(context: Context): AudioRecorderPanelManager {
            return instance ?: synchronized(this) {
                instance ?: AudioRecorderPanelManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
