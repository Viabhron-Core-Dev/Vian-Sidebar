#!/bin/bash
sed -i '/private fun findFreePort(): Int {/,/    }/c\
    private fun findFreePort(): Int {\
        try {\
            val socket = ServerSocket(0)\
            val freePort = socket.localPort\
            socket.close()\
            return freePort\
        } catch (e: Exception) {}\
        var port = 8080\
        while (port < 8100) {\
            try {\
                val socket = ServerSocket(port)\
                socket.close()\
                return port\
            } catch (e: Exception) {\
                port++\
            }\
        }\
        return 8080\
    }\
' app/src/main/java/com/example/service/PwaWindowManager.kt
