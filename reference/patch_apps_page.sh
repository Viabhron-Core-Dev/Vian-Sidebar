#!/bin/bash
sed -i '/intent.putExtra("ACTION_ID", "system:translation_floating")/a\
                        context.startService(intent)\
                    } else if (item.action == "hybrid_grid_floating") {\
                        val intent = android.content.Intent(context, SidebarService::class.java)\
                        intent.action = "EXECUTE_ACTION"\
                        intent.putExtra("ACTION_ID", "system:hybrid_grid_floating")\
' app/src/main/java/com/example/service/AppsPageView.kt
