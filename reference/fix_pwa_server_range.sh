#!/bin/bash
cat << 'INNER' > app/src/main/java/com/example/service/PwaServer.kt
package com.example.service

import fi.iki.elonen.NanoHTTPD
import java.io.InputStream
import java.util.zip.ZipFile

class PwaServer(port: Int, private val zipFilePath: String) : NanoHTTPD("127.0.0.1", port) {
    private var zipFile: ZipFile? = null

    override fun start() {
        zipFile = ZipFile(zipFilePath)
        super.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false)
    }

    override fun stop() {
        super.stop()
        try {
            zipFile?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun serve(session: IHTTPSession): Response {
        if (session.method == Method.OPTIONS) {
            val response = newFixedLengthResponse(Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT, "")
            response.addHeader("Access-Control-Allow-Origin", "*")
            response.addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
            response.addHeader("Access-Control-Allow-Headers", "*")
            return response
        }

        var uri = session.uri
        if (uri == "/") uri = "/index.html"
        uri = uri.removePrefix("/")
        
        try {
            val entry = zipFile?.getEntry(uri)
            if (entry != null) {
                val mimeType = getMimeTypeForExt(uri)
                val totalLength = entry.size

                val rangeHeader = session.headers["range"]
                
                var startFrom: Long = 0
                var endAt: Long = totalLength - 1

                if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                    val range = rangeHeader.substring("bytes=".length)
                    val minus = range.indexOf('-')
                    try {
                        if (minus > 0) {
                            startFrom = range.substring(0, minus).toLong()
                            endAt = range.substring(minus + 1).takeIf { it.isNotEmpty() }?.toLong() ?: (totalLength - 1)
                        }
                    } catch (e: NumberFormatException) {
                        e.printStackTrace()
                    }
                }

                val contentLength = endAt - startFrom + 1
                if (contentLength < 0) {
                    val res = newFixedLengthResponse(Response.Status.RANGE_NOT_SATISFIABLE, NanoHTTPD.MIME_PLAINTEXT, "")
                    res.addHeader("Content-Range", "bytes 0-0/$totalLength")
                    return res
                }

                val inputStream = zipFile!!.getInputStream(entry)
                if (startFrom > 0) {
                    inputStream.skip(startFrom)
                }
                
                // Read exactly contentLength bytes to avoid serving the whole file when not needed
                // If it's a small file or not a range request, reading the whole stream is fine
                // For a proper 206, we can wrap the input stream but NanoHTTPD's chunked response might be better.
                // However, NanoHTTPD's newFixedLengthResponse will only read `contentLength` bytes from the stream.
                val res = newFixedLengthResponse(
                    if (rangeHeader != null) Response.Status.PARTIAL_CONTENT else Response.Status.OK,
                    mimeType,
                    inputStream,
                    contentLength
                )
                
                res.addHeader("Accept-Ranges", "bytes")
                res.addHeader("Content-Length", contentLength.toString())
                if (rangeHeader != null) {
                    res.addHeader("Content-Range", "bytes $startFrom-$endAt/$totalLength")
                }
                res.addHeader("Access-Control-Allow-Origin", "*")
                res.addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
                return res
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return newFixedLengthResponse(Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "404 Not Found")
    }

    private fun getMimeTypeForExt(uri: String): String {
        return when {
            uri.endsWith(".html") -> "text/html"
            uri.endsWith(".js") -> "application/javascript"
            uri.endsWith(".mjs") -> "application/javascript"
            uri.endsWith(".css") -> "text/css"
            uri.endsWith(".json") -> "application/json"
            uri.endsWith(".png") -> "image/png"
            uri.endsWith(".jpg") || uri.endsWith(".jpeg") -> "image/jpeg"
            uri.endsWith(".svg") -> "image/svg+xml"
            uri.endsWith(".ico") -> "image/x-icon"
            uri.endsWith(".wasm") -> "application/wasm"
            uri.endsWith(".geojson") -> "application/geo+json"
            else -> "application/octet-stream"
        }
    }
}
INNER
