#!/bin/bash
sed -i '/addItem(com.example.R.drawable.ic_library_books, "eBook Reader") {/i\
        addItem(android.R.drawable.ic_menu_gallery, "Hybrid Grid") {\
            finishWithId("system:hybrid_grid_floating")\
        }\
' app/src/main/java/com/example/AddElementActivity.kt
