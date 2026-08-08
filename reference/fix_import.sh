#!/bin/bash
sed -i '20i\
import androidx.compose.material.icons.Icons\
import androidx.compose.material.icons.filled.ArrowBack
' app/src/main/java/com/example/service/BarcodeScannerActivity.kt
sed -i 's/androidx.compose.material.icons.Icons.Filled.ArrowBack/Icons.Filled.ArrowBack/g' app/src/main/java/com/example/service/BarcodeScannerActivity.kt
