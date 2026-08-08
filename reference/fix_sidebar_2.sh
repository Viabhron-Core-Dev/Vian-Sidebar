#!/bin/bash
sed -i '/} else if (action == "ebook_reader") {/i\
            } else if (action == "translation_floating") {\
                if (translationWindowManager == null) {\
                    translationWindowManager = TranslationWindowManager(this@SidebarService)\
                }\
                translationWindowManager?.show()\
' app/src/main/java/com/example/service/SidebarService.kt
