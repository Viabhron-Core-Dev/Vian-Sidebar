package com.example.service

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

@SuppressLint("SetJavaScriptEnabled", "ViewConstructor")
class TermuxPageView(context: Context) : FrameLayout(context) {

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var process: Process? = null
    private var outputStream: OutputStream? = null
    private var inputStream: InputStream? = null

    private val webViewTerminal: WebView
    private val llInstallScreen: LinearLayout
    private val btnInstallTermux: Button
    private val btnStartShell: Button
    private val tvInstallStatus: TextView
    private val pbInstall: ProgressBar

    private val prootUrl = "https://github.com/proot-me/proot/releases/download/v5.3.0/proot-v5.3.0-aarch64-static"
    private val alpineUrl = "https://dl-cdn.alpinelinux.org/alpine/v3.18/releases/aarch64/alpine-minirootfs-3.18.4-aarch64.tar.gz"

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.page_termux, this, true)

        webViewTerminal = view.findViewById(R.id.webViewTerminal)
        llInstallScreen = view.findViewById(R.id.llInstallScreen)
        btnInstallTermux = view.findViewById(R.id.btnInstallTermux)
        btnStartShell = view.findViewById(R.id.btnStartShell)
        tvInstallStatus = view.findViewById(R.id.tvInstallStatus)
        pbInstall = view.findViewById(R.id.pbInstall)

        setupWebView()

        checkInstallation()

        btnStartShell.setOnClickListener {
            startLocalShell()
        }

        btnInstallTermux.setOnClickListener {
            installEnvironment()
        }
    }

    private fun checkInstallation() {
        val prootFile = File(context.filesDir, "proot")
        val rootfsDir = File(context.filesDir, "alpine")
        if (prootFile.exists() && rootfsDir.exists()) {
            btnInstallTermux.text = "Environment Installed"
            btnInstallTermux.isEnabled = false
            btnStartShell.text = "Start Alpine Shell (PRoot)"
        }
    }

    private fun setupWebView() {
        webViewTerminal.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            cacheMode = WebSettings.LOAD_NO_CACHE
        }
        webViewTerminal.webChromeClient = WebChromeClient()
        webViewTerminal.webViewClient = WebViewClient()
        webViewTerminal.addJavascriptInterface(TerminalInterface(), "Android")
        webViewTerminal.loadUrl("file:///android_asset/xterm.html")
    }

    private inner class TerminalInterface {
        @JavascriptInterface
        fun sendInput(input: String) {
            scope.launch(Dispatchers.IO) {
                try {
                    outputStream?.write(input.toByteArray(StandardCharsets.UTF_8))
                    outputStream?.flush()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        @JavascriptInterface
        fun onResize(cols: Int, rows: Int) {}
    }

    private fun installEnvironment() {
        btnInstallTermux.isEnabled = false
        tvInstallStatus.visibility = View.VISIBLE
        pbInstall.visibility = View.VISIBLE
        
        scope.launch(Dispatchers.IO) {
            try {
                updateStatus("Downloading PRoot...")
                val prootFile = File(context.filesDir, "proot")
                downloadFile(prootUrl, prootFile)
                prootFile.setExecutable(true)

                updateStatus("Downloading Alpine Linux rootfs...")
                val alpineArchive = File(context.filesDir, "alpine.tar.gz")
                downloadFile(alpineUrl, alpineArchive)

                updateStatus("Extracting Alpine Linux...")
                val rootfsDir = File(context.filesDir, "alpine")
                rootfsDir.mkdirs()
                
                // Extract using system tar
                val process = ProcessBuilder("tar", "xzf", alpineArchive.absolutePath, "-C", rootfsDir.absolutePath).start()
                process.waitFor()
                
                alpineArchive.delete()

                withContext(Dispatchers.Main) {
                    updateStatus("Installation Complete!")
                    pbInstall.visibility = View.GONE
                    checkInstallation()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    updateStatus("Error: ${e.message}")
                    btnInstallTermux.isEnabled = true
                }
            }
        }
    }

    private suspend fun updateStatus(status: String) {
        withContext(Dispatchers.Main) {
            tvInstallStatus.text = status
        }
    }

    private fun downloadFile(urlString: String, dest: File) {
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        connection.connect()
        
        val input: InputStream = connection.inputStream
        val output = FileOutputStream(dest)
        
        val buffer = ByteArray(4096)
        var count: Int
        while (input.read(buffer).also { count = it } != -1) {
            output.write(buffer, 0, count)
        }
        
        output.flush()
        output.close()
        input.close()
    }

    private fun startLocalShell() {
        llInstallScreen.visibility = View.GONE
        webViewTerminal.visibility = View.VISIBLE

        scope.launch(Dispatchers.IO) {
            try {
                val prootFile = File(context.filesDir, "proot")
                val rootfsDir = File(context.filesDir, "alpine")
                
                val pb = if (prootFile.exists() && rootfsDir.exists()) {
                    // Start PRoot Alpine shell
                    ProcessBuilder(
                        prootFile.absolutePath,
                        "-r", rootfsDir.absolutePath,
                        "-0", "-w", "/root",
                        "-b", "/dev", "-b", "/proc", "-b", "/sys",
                        "/bin/sh", "-i"
                    )
                } else {
                    // Fallback to local shell
                    ProcessBuilder("/system/bin/sh", "-i")
                }
                
                pb.environment()["TERM"] = "xterm-256color"
                pb.environment()["HOME"] = "/root"
                pb.environment()["PATH"] = "/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin"
                pb.redirectErrorStream(true)
                
                val p = pb.start()
                process = p
                outputStream = p.outputStream
                inputStream = p.inputStream

                val buffer = ByteArray(4096)
                while (true) {
                    val read = inputStream?.read(buffer) ?: break
                    if (read == -1) break
                    val output = String(buffer, 0, read, StandardCharsets.UTF_8)
                    writeToTerminal(output)
                }
            } catch (e: Exception) {
                writeToTerminal("\r\nError starting shell: ${e.message}\r\n")
            }
        }
    }

    private fun writeToTerminal(text: String) {
        val encoded = Base64.encodeToString(text.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP)
        val js = "if(typeof writeTerminal === 'function') { writeTerminal(decodeURIComponent(escape(window.atob('$encoded')))); }"
        Handler(Looper.getMainLooper()).post {
            webViewTerminal.evaluateJavascript(js, null)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        try {
            outputStream?.close()
            inputStream?.close()
            process?.destroy()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        scope.cancel()
    }
}
