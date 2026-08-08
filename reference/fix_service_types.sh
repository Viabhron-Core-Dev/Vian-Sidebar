#!/bin/bash
sed -i '/types = types or android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE/a\
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {\
                    types = types or android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION\
                }\
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {\
                    types = types or android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE\
                }\
' app/src/main/java/com/example/service/SidebarService.kt
