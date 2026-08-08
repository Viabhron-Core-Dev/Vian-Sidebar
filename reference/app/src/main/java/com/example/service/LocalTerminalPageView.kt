package com.example.service

import android.content.Context
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ScrollView
import android.widget.TextView
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter

class LocalTerminalPageView(context: Context) : FrameLayout(context) {

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var process: Process? = null
    private var writer: PrintWriter? = null
    
    private val tvTerminalOutput: TextView
    private val etTerminalInput: EditText
    private val scrollView: ScrollView
    
    private val outputBuffer = StringBuilder()
    private var lineCount = 0
    private val maxLines = 1000

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.page_local_terminal, this, true)
        
        tvTerminalOutput = view.findViewById(R.id.tvTerminalOutput)
        etTerminalInput = view.findViewById(R.id.etTerminalInput)
        scrollView = view.findViewById(R.id.scrollView)
        
        val btnSend: ImageButton = view.findViewById(R.id.btnSend)
        
        btnSend.setOnClickListener { sendCommand() }
        etTerminalInput.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND || 
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                sendCommand()
                true
            } else {
                false
            }
        }

        startShell()
    }

    private fun startShell() {
        scope.launch(Dispatchers.IO) {
            try {
                val pb = ProcessBuilder("/system/bin/sh")
                pb.redirectErrorStream(true)
                val p = pb.start()
                process = p
                writer = PrintWriter(OutputStreamWriter(p.outputStream), true)
                
                val reader = BufferedReader(InputStreamReader(p.inputStream))
                
                withContext(Dispatchers.Main) {
                    appendOutput("Welcome to Local Terminal\nRunning /system/bin/sh")
                }

                while (true) {
                    val line = reader.readLine() ?: break
                    withContext(Dispatchers.Main) {
                        appendOutput(line)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    appendOutput("Error starting shell: ${e.message}")
                }
            }
        }
    }
    
    private fun sendCommand() {
        val cmd = etTerminalInput.text.toString()
        if (cmd.isNotBlank()) {
            appendOutput("$ $cmd")
            etTerminalInput.text.clear()
            scope.launch(Dispatchers.IO) {
                writer?.println(cmd)
            }
        }
    }
    
    private fun appendOutput(text: String) {
        outputBuffer.append(text).append("\n")
        lineCount++
        
        if (lineCount > maxLines) {
            val idx = outputBuffer.indexOf("\n", outputBuffer.length / 2)
            if (idx != -1) {
                outputBuffer.delete(0, idx + 1)
                lineCount /= 2
            }
        }
        
        tvTerminalOutput.text = outputBuffer.toString()
        scrollView.post {
            scrollView.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        writer?.close()
        process?.destroy()
        scope.cancel()
    }
}
