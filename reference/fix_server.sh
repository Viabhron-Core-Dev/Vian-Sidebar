#!/bin/bash
sed -i '/return newFixedLengthResponse(Response.Status.OK, mimeType, bytes.inputStream(), bytes.size.toLong())/c\
                val response = newFixedLengthResponse(Response.Status.OK, mimeType, bytes.inputStream(), bytes.size.toLong())\
                response.addHeader("Access-Control-Allow-Origin", "*")\
                response.addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS")\
                return response\
' app/src/main/java/com/example/service/PwaServer.kt

sed -i '/uri.endsWith(".wasm") -> "application\/wasm"/a\
            uri.endsWith(".geojson") -> "application/geo+json"\
' app/src/main/java/com/example/service/PwaServer.kt
