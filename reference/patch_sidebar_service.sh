#!/bin/bash
sed -i '/private var translationWindowManager/a\    private var hybridGridWindowManager: HybridGridWindowManager? = null' app/src/main/java/com/example/service/SidebarService.kt
sed -i '/} else if (action == "translation_floating") {/i\
            } else if (action == "hybrid_grid_floating" || action == "hybrid_grid_floating_exit_edit") {\
                if (hybridGridWindowManager == null) {\
                    hybridGridWindowManager = HybridGridWindowManager(this@SidebarService)\
                }\
                hybridGridWindowManager?.show(action == "hybrid_grid_floating_exit_edit")\
                hybridGridWindowManager?.reloadGrid()\
' app/src/main/java/com/example/service/SidebarService.kt
