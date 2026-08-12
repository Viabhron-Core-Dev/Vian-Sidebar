with open('reference/app/src/main/java/com/example/SettingsActivity.kt', 'r') as f:
    lines = f.readlines()

out = []
skip = False
skip_brace_count = 0
in_run_catching = False

i = 0
while i < len(lines):
    line = lines[i]
    
    # 1. Package name and imports
    if line.startswith("package com.example"):
        out.append("package com.example.feature.settings\n")
        i += 1
        continue
    
    if line.startswith("import com.example."):
        if "LogKeeper" in line:
            out.append("import com.example.core.LogKeeper\n")
        elif "BackupHelper" not in line:
            out.append(line)
        i += 1
        continue
    
    if "com.example.LogKeeper" in line:
        out.append(line.replace("com.example.LogKeeper", "com.example.core.LogKeeper"))
        i += 1
        continue
    
    # 2. isFirstLaunch = true,
    if "isFirstLaunch = true," in line:
        # omit it
        i += 1
        continue

    # 3. LogKeeperActivity, PwaManagerActivity, AppyworkSettingsActivity
    if "val intent = Intent(context, com.example." in line and "Activity::class.java).apply {" in line:
        out.append('                        android.widget.Toast.makeText(context, "Not Migrated", android.widget.Toast.LENGTH_SHORT).show()\n')
        # Skip this block until the matching `}` and `context.startActivity(intent)`
        # Usually it's just 3-4 lines
        while "context.startActivity(intent)" not in lines[i]:
            i += 1
        i += 1
        continue
        
    # 4. BackupHelper runCatching blocks
    if "val res = runCatching" in line or "val result = runCatching" in line:
        if "BackupHelper" in "".join(lines[i:i+15]):
            out.append('                        android.widget.Toast.makeText(context, "Not Migrated", android.widget.Toast.LENGTH_SHORT).show()\n')
            # skip until we see the closing brace of the `} else {` block
            # Usually:
            # val res = runCatching { ... }
            # if (res.isSuccess) {
            #    Toast
            # } else {
            #    Toast
            # }
            # Wait for `} else {` and then the next `}`
            while "} else {" not in lines[i]:
                i += 1
            # Now skip until `}`
            i += 1
            while lines[i].strip() != "}":
                i += 1
            i += 1
            continue
            
    out.append(line)
    i += 1

out.append("""
@androidx.compose.runtime.Composable fun NetSpeedSettingsScreen(onBack: () -> Unit) {}
@androidx.compose.runtime.Composable fun CallRecorderSettingsScreen(onBack: () -> Unit) {}
@androidx.compose.runtime.Composable fun DictionarySettingsScreen(onBack: () -> Unit) {}
@androidx.compose.runtime.Composable fun WelcomeScreen(onContinue: () -> Unit) {}
@androidx.compose.runtime.Composable fun BrowserSettingsScreen(onBack: () -> Unit) {}
""")

with open('app/src/main/java/com/example/feature/settings/SettingsActivity.kt', 'w') as f:
    f.writelines(out)

