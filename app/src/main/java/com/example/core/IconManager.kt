package com.example.core

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Centralized IconManager as per Phase 1.
 * Currently uses standard Material icons as placeholders.
 * Prepared for custom drawables/icons swap in the future.
 */
object IconManager {
    val AppyworkIcon: ImageVector = Icons.Default.Code
    val DictionaryIcon: ImageVector = Icons.Default.Book
    val TranslationIcon: ImageVector = Icons.Default.Translate
    val ReadAloudIcon: ImageVector = Icons.Default.VolumeUp
    val BrowserIcon: ImageVector = Icons.Default.Language
    val CallRecorderIcon: ImageVector = Icons.Default.Mic
    val NetSpeedIcon: ImageVector = Icons.Default.Speed
    val ScreenRecordIcon: ImageVector = Icons.Default.Videocam
    val SettingsIcon: ImageVector = Icons.Default.Settings
    val CloseIcon: ImageVector = Icons.Default.Close
    val FoldIcon: ImageVector = Icons.Default.Minimize
    val ResizeIcon: ImageVector = Icons.Default.OpenWith
    val DragHandleIcon: ImageVector = Icons.Default.DragHandle
}
