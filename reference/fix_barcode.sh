#!/bin/bash
sed -i '/else if (action == "settings") {/i \            } else if (action == "barcode_scanner") {\
                val intent = Intent(this, com.example.service.BarcodeScannerActivity::class.java)\
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)\
                startActivity(intent)' app/src/main/java/com/example/service/SidebarService.kt
