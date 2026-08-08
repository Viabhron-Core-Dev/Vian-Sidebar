/fun WidgetPickerScreen\(/ {
    print "@OptIn(ExperimentalMaterial3Api::class)"
    print "fun WidgetPickerScreen("
    print "    providers: List<AppWidgetProviderInfo>,"
    print "    actionType: String,"
    print "    onWidgetSelected: (AppWidgetProviderInfo) -> Unit,"
    print "    onCancel: () -> Unit"
    print ") {"
    skip = 1
    next
}
skip && /\) \{/ {
    skip = 0
    next
}
skip { next }
/Row\(/ && /verticalAlignment = Alignment.CenterVertically/ {
    print "                                    val spanX = Math.ceil((provider.minWidth + 30) / 70.0).toInt()"
    print "                                    val spanY = Math.ceil((provider.minHeight + 30) / 70.0).toInt()"
    print "                                    val is1x1 = spanX <= 1 && spanY <= 1"
    print "                                    val isSidebar = actionType == \"ADD_ELEMENT\""
    print "                                    val enabled = !isSidebar || is1x1"
    print "                                    Row("
    print "                                        modifier = Modifier"
    print "                                            .fillMaxWidth()"
    print "                                            .clickable(enabled = enabled) { onWidgetSelected(provider) }"
    print "                                            .padding(vertical = 12.dp, horizontal = 16.dp),"
    print "                                        verticalAlignment = Alignment.CenterVertically"
    print "                                    ) {"
    skip2 = 1
    next
}
skip2 && /verticalAlignment = Alignment.CenterVertically/ { skip2 = 0; next }
skip2 && /Modifier/ { next }
skip2 && /clickable/ { next }
skip2 && /\.padding/ { next }
skip2 && /Row\(/ { next }
skip2 { skip2 = 0 } # fallback

/Text\(/ && /text = provider.loadLabel\(pm\)/ {
    print "                                        Column(modifier = Modifier.weight(1f)) {"
    print "                                            Text("
    print "                                                text = provider.loadLabel(pm),"
    print "                                                style = MaterialTheme.typography.bodyLarge,"
    print "                                                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),"
    print "                                                maxLines = 1,"
    print "                                                overflow = TextOverflow.Ellipsis"
    print "                                            )"
    print "                                            Text("
    print "                                                text = \"${spanX}x${spanY}\","
    print "                                                style = MaterialTheme.typography.bodySmall,"
    print "                                                color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)"
    print "                                            )"
    print "                                        }"
    skip3 = 1
    next
}
skip3 && /overflow = TextOverflow.Ellipsis/ {
    skip3 = 0
    next
}
skip3 { next }

{ print }
