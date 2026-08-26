package com.example.feature.settings

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.app.usage.NetworkStatsManager
import android.app.usage.NetworkStats
import android.net.NetworkCapabilities
import android.os.RemoteException
import android.content.pm.PackageManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import android.graphics.drawable.Drawable
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import java.util.Calendar
import androidx.compose.foundation.Image
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.sp

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.Manifest
import android.os.Build

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.ui.graphics.Color
import com.example.core.DynamicSpeedIconGenerator
import java.util.Locale

data class AppUsageInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable?,
    val usageBytes: Long
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetSpeedSettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("FloatingReaderPrefs", Context.MODE_PRIVATE) }
    
    var speedIndicatorEnabled by remember { 
        mutableStateOf(prefs.getBoolean("netspeed_enabled", prefs.getBoolean("speed_indicator_enabled", true))) 
    }
    var hideWhenDisconnected by remember {
        mutableStateOf(prefs.getBoolean("hide_when_disconnected", true))
    }
    var speedUnits by remember { mutableStateOf(prefs.getString("speed_units", "Auto") ?: "Auto") }
    var dataUnits by remember { mutableStateOf(prefs.getString("data_units", "Auto") ?: "Auto") }

    // Dynamic Icon Parameters
    var iconFont by remember { mutableStateOf(prefs.getString("speed_icon_font", "sans-serif-condensed") ?: "sans-serif-condensed") }
    var isFakeBold by remember { mutableStateOf(prefs.getBoolean("speed_icon_bold", true)) }
    var numScale by remember { mutableFloatStateOf(prefs.getFloat("speed_icon_num_scale", 1.0f)) }
    var unitScale by remember { mutableFloatStateOf(prefs.getFloat("speed_icon_unit_scale", 1.0f)) }
    var numYOffset by remember { mutableFloatStateOf(prefs.getFloat("speed_icon_num_y_offset", 0f)) }
    var unitYOffset by remember { mutableFloatStateOf(prefs.getFloat("speed_icon_unit_y_offset", 0f)) }
    var bgShape by remember { mutableStateOf(prefs.getString("speed_icon_bg_shape", "None") ?: "None") }
    var bgRadius by remember { mutableFloatStateOf(prefs.getFloat("speed_icon_bg_radius", 4f)) }
    var bgAlpha by remember { mutableIntStateOf(prefs.getInt("speed_icon_bg_alpha", 0)) }
    var layoutMode by remember { mutableStateOf(prefs.getString("speed_icon_layout", "Stacked") ?: "Stacked") }
    // Blurriness reduction & sharpness parameters
    var resScale by remember { mutableFloatStateOf(prefs.getFloat("speed_icon_res_scale", 1.0f)) }
    var aaMode by remember { mutableStateOf(prefs.getString("speed_icon_aa_mode", "Crisp") ?: "Crisp") }
    var letterSpacing by remember { mutableFloatStateOf(prefs.getFloat("speed_icon_letter_spacing", -0.02f)) }
    var strokeWidth by remember { mutableFloatStateOf(prefs.getFloat("speed_icon_stroke_width", 0.0f)) }

    val currentIconConfig = remember(
        iconFont, isFakeBold, numScale, unitScale, numYOffset, unitYOffset,
        bgShape, bgRadius, bgAlpha, layoutMode, resScale, aaMode, letterSpacing, strokeWidth
    ) {
        DynamicSpeedIconGenerator.IconConfig(
            font = iconFont,
            isFakeBold = isFakeBold,
            numScale = numScale,
            unitScale = unitScale,
            numYOffsetDp = numYOffset,
            unitYOffsetDp = unitYOffset,
            bgShape = bgShape,
            bgRadiusDp = bgRadius,
            bgAlpha = bgAlpha,
            layoutMode = layoutMode,
            resScale = resScale,
            aaMode = aaMode,
            letterSpacing = letterSpacing,
            strokeWidthDp = strokeWidth
        )
    }

    val saveIconConfig = {
        prefs.edit()
            .putString("speed_icon_font", iconFont)
            .putBoolean("speed_icon_bold", isFakeBold)
            .putFloat("speed_icon_num_scale", numScale)
            .putFloat("speed_icon_unit_scale", unitScale)
            .putFloat("speed_icon_num_y_offset", numYOffset)
            .putFloat("speed_icon_unit_y_offset", unitYOffset)
            .putString("speed_icon_bg_shape", bgShape)
            .putFloat("speed_icon_bg_radius", bgRadius)
            .putInt("speed_icon_bg_alpha", bgAlpha)
            .putString("speed_icon_layout", layoutMode)
            .putFloat("speed_icon_res_scale", resScale)
            .putString("speed_icon_aa_mode", aaMode)
            .putFloat("speed_icon_letter_spacing", letterSpacing)
            .putFloat("speed_icon_stroke_width", strokeWidth)
            .apply()
        DynamicSpeedIconGenerator.updateActiveConfig(currentIconConfig)
    }

    val applyUltraSharpCompactPreset = {
        iconFont = "sans-serif-condensed"
        isFakeBold = true
        numScale = 1.1f
        unitScale = 1.0f
        layoutMode = "Compact"
        resScale = 1.0f
        aaMode = "Crisp"
        letterSpacing = -0.02f
        strokeWidth = 0.0f
        saveIconConfig()
    }

    val applyUltraSharpStackedPreset = {
        iconFont = "sans-serif-condensed"
        isFakeBold = true
        numScale = 1.05f
        unitScale = 0.95f
        layoutMode = "Stacked"
        resScale = 1.0f
        aaMode = "Crisp"
        letterSpacing = -0.02f
        strokeWidth = 0.0f
        saveIconConfig()
    }

    val resetToDefaults = {
        iconFont = "sans-serif-condensed"
        isFakeBold = true
        numScale = 1.0f
        unitScale = 1.0f
        numYOffset = 0f
        unitYOffset = 0f
        bgShape = "None"
        bgRadius = 4f
        bgAlpha = 0
        layoutMode = "Stacked"
        resScale = 1.0f
        aaMode = "Crisp"
        letterSpacing = -0.02f
        strokeWidth = 0.0f
        
        prefs.edit()
            .putString("speed_icon_font", "sans-serif-condensed")
            .putBoolean("speed_icon_bold", true)
            .putFloat("speed_icon_num_scale", 1.0f)
            .putFloat("speed_icon_unit_scale", 1.0f)
            .putFloat("speed_icon_num_y_offset", 0f)
            .putFloat("speed_icon_unit_y_offset", 0f)
            .putString("speed_icon_bg_shape", "None")
            .putFloat("speed_icon_bg_radius", 4f)
            .putInt("speed_icon_bg_alpha", 0)
            .putString("speed_icon_layout", "Stacked")
            .putFloat("speed_icon_res_scale", 1.0f)
            .putString("speed_icon_aa_mode", "Crisp")
            .putFloat("speed_icon_letter_spacing", -0.02f)
            .putFloat("speed_icon_stroke_width", 0.0f)
            .apply()
        DynamicSpeedIconGenerator.updateActiveConfig(DynamicSpeedIconGenerator.IconConfig())
    }

    val networkStatsManager = remember { context.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager }
    val packageManager = context.packageManager
    
    var hasPermission by remember { 
        mutableStateOf(hasUsageStatsPermission(context))
    }

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    var usageData by remember { mutableStateOf<List<AppUsageInfo>>(emptyList()) }
    var timePeriod by remember { mutableStateOf("Daily") } // Daily, Weekly, Monthly
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val loadData = {
        if (hasPermission) {
            isLoading = true
            coroutineScope.launch(Dispatchers.IO) {
                val data = fetchUsageData(context, networkStatsManager, packageManager, timePeriod)
                withContext(Dispatchers.Main) {
                    usageData = data
                    isLoading = false
                }
            }
        }
    }

    LaunchedEffect(hasPermission, timePeriod) {
        loadData()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Internet Speed Monitor") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(onClick = resetToDefaults) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset Defaults")
                }
            }
        )
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                ListItem(
                    headlineContent = { Text("Enable Internet Speed Monitor") },
                    supportingContent = { Text("Displays real-time upload/download speeds in the status bar") },
                    trailingContent = {
                        Switch(
                            checked = speedIndicatorEnabled,
                            onCheckedChange = { enabled ->
                                speedIndicatorEnabled = enabled
                                prefs.edit()
                                    .putBoolean("netspeed_enabled", enabled)
                                    .putBoolean("speed_indicator_enabled", enabled)
                                    .apply()
                                if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                        notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                }
                            }
                        )
                    }
                )
                Divider()

                ListItem(
                    headlineContent = { Text("Hide when Disconnected") },
                    supportingContent = { Text("Pauses polling and hides the status bar icon when offline (0% CPU/battery)") },
                    trailingContent = {
                        Switch(
                            checked = hideWhenDisconnected,
                            onCheckedChange = { enabled ->
                                hideWhenDisconnected = enabled
                                prefs.edit().putBoolean("hide_when_disconnected", enabled).apply()
                            }
                        )
                    }
                )
                Divider()
                
                // Speed Units
                ListItem(
                    headlineContent = { Text("Speed Units") },
                    supportingContent = { Text(speedUnits) },
                    modifier = Modifier.clickable {
                        speedUnits = when (speedUnits) {
                            "Auto" -> "KB/s"
                            "KB/s" -> "MB/s"
                            "MB/s" -> "Auto"
                            else -> "Auto"
                        }
                        prefs.edit().putString("speed_units", speedUnits).apply()
                    }
                )
                Divider()

                // Data Usage Units
                ListItem(
                    headlineContent = { Text("Data Usage Units") },
                    supportingContent = { Text(dataUnits) },
                    modifier = Modifier.clickable {
                        dataUnits = when (dataUnits) {
                            "Auto" -> "MB"
                            "MB" -> "GB"
                            "GB" -> "GiB"
                            "GiB" -> "Auto"
                            else -> "Auto"
                        }
                        prefs.edit().putString("data_units", dataUnits).apply()
                    }
                )
                Divider()

                // Dynamic Icon Customization Header & Live Preview Card
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Dynamic Status Bar Icon Appearance",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SuggestionChip(
                        onClick = applyUltraSharpCompactPreset,
                        label = { Text("⚡ Compact (Battery-Indicator Pro)", style = MaterialTheme.typography.labelSmall) }
                    )
                    SuggestionChip(
                        onClick = applyUltraSharpStackedPreset,
                        label = { Text("⚡ Stacked (Ultra Sharp)", style = MaterialTheme.typography.labelSmall) }
                    )
                }

                // Live Preview Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Live Status Bar Preview",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Preview box simulating the dark status bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1E2124), RoundedCornerShape(12.dp))
                                .padding(vertical = 14.dp, horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Sample 1: Idle (0 KB/s)
                            val bmp0 = remember(currentIconConfig, speedUnits) {
                                DynamicSpeedIconGenerator.generateStatusBarBitmap(context, 0, speedUnits, currentIconConfig)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Image(
                                    bitmap = bmp0.asImageBitmap(),
                                    contentDescription = "0 KB/s preview",
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Idle", style = MaterialTheme.typography.labelSmall, color = Color(0xFFAAAAAA))
                            }

                            // Sample 2: Active KB/s (450 KB/s)
                            val bmpKb = remember(currentIconConfig, speedUnits) {
                                DynamicSpeedIconGenerator.generateStatusBarBitmap(context, 450 * 1024L, speedUnits, currentIconConfig)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Image(
                                    bitmap = bmpKb.asImageBitmap(),
                                    contentDescription = "450 KB/s preview",
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Normal", style = MaterialTheme.typography.labelSmall, color = Color(0xFFAAAAAA))
                            }

                            // Sample 3: High speed MB/s (14.8 MB/s)
                            val bmpMb = remember(currentIconConfig, speedUnits) {
                                DynamicSpeedIconGenerator.generateStatusBarBitmap(context, (14.8 * 1024 * 1024).toLong(), speedUnits, currentIconConfig)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Image(
                                    bitmap = bmpMb.asImageBitmap(),
                                    contentDescription = "14.8 MB/s preview",
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Fast", style = MaterialTheme.typography.labelSmall, color = Color(0xFFAAAAAA))
                            }
                        }
                    }
                }

                // Controls & Sliders
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    // Layout Mode
                    Text("Display Layout", style = MaterialTheme.typography.titleSmall)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Stacked" to "Stacked (2-Line)", "Compact" to "Compact (1-Line)", "NumberOnly" to "Number Only").forEach { (mode, label) ->
                            FilterChip(
                                selected = layoutMode == mode,
                                onClick = {
                                    layoutMode = mode
                                    saveIconConfig()
                                },
                                label = { Text(label, style = MaterialTheme.typography.bodySmall) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Font Family
                    Text("Font Style", style = MaterialTheme.typography.titleSmall)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "sans-serif-condensed" to "Condensed",
                            "sans-serif" to "Default",
                            "sans-serif-medium" to "Medium",
                            "sans-serif-black" to "Black",
                            "monospace" to "Mono",
                            "serif" to "Serif"
                        ).forEach { (fontKey, label) ->
                            FilterChip(
                                selected = iconFont == fontKey,
                                onClick = {
                                    iconFont = fontKey
                                    saveIconConfig()
                                },
                                label = { Text(label, style = MaterialTheme.typography.bodySmall) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Boldness Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Bold Text Stroke", style = MaterialTheme.typography.titleSmall)
                            Text("Enhance glyph thickness and contrast", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = isFakeBold,
                            onCheckedChange = {
                                isFakeBold = it
                                saveIconConfig()
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Slider: Number Size Scale
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Number Text Size", style = MaterialTheme.typography.titleSmall)
                            Text(String.format(Locale.US, "%.2fx", numScale), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                        }
                        Slider(
                            value = numScale,
                            onValueChange = {
                                numScale = it
                                saveIconConfig()
                            },
                            valueRange = 0.6f..1.4f,
                            steps = 15
                        )
                    }

                    // Slider: Unit Size Scale (only if not NumberOnly)
                    if (layoutMode != "NumberOnly") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Unit Text Size", style = MaterialTheme.typography.titleSmall)
                                Text(String.format(Locale.US, "%.2fx", unitScale), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                            }
                            Slider(
                                value = unitScale,
                                onValueChange = {
                                    unitScale = it
                                    saveIconConfig()
                                },
                                valueRange = 0.5f..1.5f,
                                steps = 19
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Slider: Number Vertical Offset (Y)
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Number Vertical Offset", style = MaterialTheme.typography.titleSmall)
                            Text("${numYOffset.toInt()} dp", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                        }
                        Slider(
                            value = numYOffset,
                            onValueChange = {
                                numYOffset = it
                                saveIconConfig()
                            },
                            valueRange = -6f..6f,
                            steps = 12
                        )
                    }

                    // Slider: Unit Vertical Offset (Y) (only if Stacked)
                    if (layoutMode == "Stacked") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Unit Vertical Offset", style = MaterialTheme.typography.titleSmall)
                                Text("${unitYOffset.toInt()} dp", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                            }
                            Slider(
                                value = unitYOffset,
                                onValueChange = {
                                    unitYOffset = it
                                    saveIconConfig()
                                },
                                valueRange = -6f..6f,
                                steps = 12
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Background Shape
                    Text("Background Shape", style = MaterialTheme.typography.titleSmall)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("None", "Rounded", "Pill", "Square").forEach { shape ->
                            FilterChip(
                                selected = bgShape == shape,
                                onClick = {
                                    bgShape = shape
                                    if (shape != "None" && bgAlpha == 0) {
                                        bgAlpha = 140 // Set default visible alpha
                                    }
                                    saveIconConfig()
                                },
                                label = { Text(shape, style = MaterialTheme.typography.bodySmall) }
                            )
                        }
                    }

                    // Background Radius & Opacity (if shape selected)
                    if (bgShape != "None") {
                        if (bgShape == "Rounded") {
                            Spacer(modifier = Modifier.height(8.dp))
                            Column {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Corner Radius", style = MaterialTheme.typography.titleSmall)
                                    Text("${bgRadius.toInt()} dp", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                                }
                                Slider(
                                    value = bgRadius,
                                    onValueChange = {
                                        bgRadius = it
                                        saveIconConfig()
                                    },
                                    valueRange = 0f..12f,
                                    steps = 12
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Background Opacity", style = MaterialTheme.typography.titleSmall)
                                Text("${((bgAlpha / 255f) * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                            }
                            Slider(
                                value = bgAlpha.toFloat(),
                                onValueChange = {
                                    bgAlpha = it.toInt()
                                    saveIconConfig()
                                },
                                valueRange = 0f..255f,
                                steps = 25
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Clarity & Blurriness Reduction
                    Text("Clarity & Blur Reduction", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                    Text("Eliminates fuzziness and optimizes rendering for high-DPI status bars", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Resolution / Supersampling Multiplier
                    Text("Canvas Supersampling (Resolution)", style = MaterialTheme.typography.titleSmall)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            1.0f to "1.0x (Native)",
                            1.5f to "1.5x (Sharp)",
                            2.0f to "2.0x (Ultra)",
                            3.0f to "3.0x (Max)"
                        ).forEach { (scale, label) ->
                            FilterChip(
                                selected = resScale == scale,
                                onClick = {
                                    resScale = scale
                                    saveIconConfig()
                                },
                                label = { Text(label, style = MaterialTheme.typography.bodySmall) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Anti-Aliasing Mode
                    Text("Anti-Aliasing & Edge Hinting", style = MaterialTheme.typography.titleSmall)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "Smooth" to "Smooth (Subpixel)",
                            "Crisp" to "Crisp (1-Bit Sharp)",
                            "HighContrast" to "High Contrast Outline"
                        ).forEach { (mode, label) ->
                            FilterChip(
                                selected = aaMode == mode,
                                onClick = {
                                    aaMode = mode
                                    saveIconConfig()
                                },
                                label = { Text(label, style = MaterialTheme.typography.bodySmall) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Slider: Letter Spacing
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Character Spacing (Tracking)", style = MaterialTheme.typography.titleSmall)
                            Text(String.format(Locale.US, "%+.2f", letterSpacing), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                        }
                        Slider(
                            value = letterSpacing,
                            onValueChange = {
                                letterSpacing = it
                                saveIconConfig()
                            },
                            valueRange = -0.05f..0.15f,
                            steps = 20
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Slider: Extra Stroke Sharpness Weight
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Extra Sharpness Stroke", style = MaterialTheme.typography.titleSmall)
                            Text(String.format(Locale.US, "%.2f dp", strokeWidth), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                        }
                        Slider(
                            value = strokeWidth,
                            onValueChange = {
                                strokeWidth = it
                                saveIconConfig()
                            },
                            valueRange = 0.0f..1.2f,
                            steps = 12
                        )
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "App Data Usage",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(16.dp, 8.dp)
                )

                if (!hasPermission) {
                    ListItem(
                        headlineContent = { Text("Usage Access Required") },
                        supportingContent = { Text("Tap to grant permission to view app data usage") },
                        modifier = Modifier.clickable {
                            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        }
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf("Daily", "Weekly", "Monthly").forEach { period ->
                            FilterChip(
                                selected = timePeriod == period,
                                onClick = { timePeriod = period },
                                label = { Text(period) },
                                leadingIcon = if (timePeriod == period) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize)) }
                                } else null
                            )
                        }
                    }

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        placeholder = { Text("Search apps") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                        singleLine = true
                    )
                }
            }

            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else if (hasPermission) {
                val filteredData = usageData.filter {
                    it.appName.contains(searchQuery.text, ignoreCase = true) || 
                    it.packageName.contains(searchQuery.text, ignoreCase = true)
                }
                
                items(filteredData) { info ->
                    ListItem(
                        headlineContent = { Text(info.appName) },
                        supportingContent = { Text(formatDataUsage(info.usageBytes, dataUnits)) },
                        leadingContent = {
                            if (info.icon != null) {
                                Image(
                                    bitmap = info.icon.toBitmap(96, 96).asImageBitmap(),
                                    contentDescription = info.appName,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

fun hasUsageStatsPermission(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as android.app.AppOpsManager
    val mode = appOps.checkOpNoThrow(
        android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
        android.os.Process.myUid(),
        context.packageName
    )
    if (mode == android.app.AppOpsManager.MODE_DEFAULT) {
        return context.checkCallingOrSelfPermission(android.Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED
    }
    return mode == android.app.AppOpsManager.MODE_ALLOWED
}

fun formatDataUsage(bytes: Long, units: String): String {
    return when(units) {
        "MB" -> String.format("%.2f MB", bytes / (1000.0 * 1000.0))
        "GB" -> String.format("%.2f GB", bytes / (1000.0 * 1000.0 * 1000.0))
        "GiB" -> String.format("%.2f GiB", bytes / (1024.0 * 1024.0 * 1024.0))
        else -> {
            val mb = bytes / (1024.0 * 1024.0)
            if (mb > 1024) String.format("%.2f GiB", mb / 1024.0) else String.format("%.1f MB", mb)
        }
    }
}

fun fetchUsageData(context: Context, manager: NetworkStatsManager, pm: PackageManager, period: String): List<AppUsageInfo> {
    val uidMap = mutableMapOf<Int, Long>()
    
    val cal = Calendar.getInstance()
    val end = cal.timeInMillis
    when (period) {
        "Daily" -> {
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.clear(Calendar.MINUTE)
            cal.clear(Calendar.SECOND)
            cal.clear(Calendar.MILLISECOND)
        }
        "Weekly" -> cal.add(Calendar.DAY_OF_YEAR, -7)
        "Monthly" -> cal.add(Calendar.MONTH, -1)
    }
    val start = cal.timeInMillis

    try {
        val wifiStats = manager.querySummary(NetworkCapabilities.TRANSPORT_WIFI, null, start, end)
        val bucket = NetworkStats.Bucket()
        while (wifiStats.hasNextBucket()) {
            wifiStats.getNextBucket(bucket)
            uidMap[bucket.uid] = uidMap.getOrDefault(bucket.uid, 0L) + bucket.rxBytes + bucket.txBytes
        }
        wifiStats.close()
        
        val mobileStats = manager.querySummary(NetworkCapabilities.TRANSPORT_CELLULAR, null, start, end)
        while (mobileStats.hasNextBucket()) {
            mobileStats.getNextBucket(bucket)
            uidMap[bucket.uid] = uidMap.getOrDefault(bucket.uid, 0L) + bucket.rxBytes + bucket.txBytes
        }
        mobileStats.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }

    val results = mutableListOf<AppUsageInfo>()
    for ((uid, bytes) in uidMap) {
        if (bytes == 0L) continue
        
        val packages = pm.getPackagesForUid(uid)
        if (packages != null && packages.isNotEmpty()) {
            val packageName = packages[0]
            try {
                val pInfo = pm.getApplicationInfo(packageName, 0)
                val appName = pm.getApplicationLabel(pInfo).toString()
                val icon = pm.getApplicationIcon(pInfo)
                results.add(AppUsageInfo(packageName, appName, icon, bytes))
            } catch (e: PackageManager.NameNotFoundException) {
            }
        }
    }
    
    return results.sortedByDescending { it.usageBytes }
}
