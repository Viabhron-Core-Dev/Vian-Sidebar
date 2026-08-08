import re

with open('app/src/main/java/com/example/WelcomeScreen.kt', 'r') as f:
    content = f.read()

# Add ComponentName and TextUtils imports if needed
if 'import android.content.ComponentName' not in content:
    content = content.replace('import android.content.Context', 'import android.content.Context\nimport android.content.ComponentName\nimport android.text.TextUtils')

# Add checkAccessibilityPermission function
accessibility_check_code = """
private fun isAccessibilityServiceEnabled(context: Context): Boolean {
    val expectedComponentName = ComponentName(context, com.example.service.VianSideAccessibilityService::class.java)
    val enabledServicesSetting = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
    if (enabledServicesSetting == null) return false
    val colonSplitter = TextUtils.SimpleStringSplitter(':')
    colonSplitter.setString(enabledServicesSetting)
    while (colonSplitter.hasNext()) {
        val componentNameString = colonSplitter.next()
        val enabledService = ComponentName.unflattenFromString(componentNameString)
        if (enabledService != null && enabledService == expectedComponentName) {
            return true
        }
    }
    return false
}
"""

if 'isAccessibilityServiceEnabled' not in content:
    content += "\n" + accessibility_check_code

# Add hasAccessibility state
if 'var hasAccessibility ' not in content:
    content = content.replace('var hasUsageAccess by remember { mutableStateOf(checkUsageAccess(context)) }', 
                              'var hasUsageAccess by remember { mutableStateOf(checkUsageAccess(context)) }\n    var hasAccessibility by remember { mutableStateOf(isAccessibilityServiceEnabled(context)) }')

# Update LifecycleEffect to refresh hasAccessibility
if 'hasAccessibility = isAccessibilityServiceEnabled(context)' not in content:
    content = content.replace('hasUsageAccess = checkUsageAccess(context)', 
                              'hasUsageAccess = checkUsageAccess(context)\n                hasAccessibility = isAccessibilityServiceEnabled(context)')

# Add PermissionItem for Accessibility
accessibility_item = """
        PermissionItem(
            title = "Accessibility Access",
            description = "Required to perform global actions like Back, Home, and Recents.",
            isGranted = hasAccessibility,
            onClick = {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                context.startActivity(intent)
            }
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        Button("""

content = content.replace('Button(', accessibility_item.strip() + '\n        Button(')

with open('app/src/main/java/com/example/WelcomeScreen.kt', 'w') as f:
    f.write(content)

print("Updated WelcomeScreen.kt")
