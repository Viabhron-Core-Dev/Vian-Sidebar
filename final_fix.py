with open('app/src/main/java/com/example/feature/settings/SettingsActivity.kt', 'r') as f:
    lines = f.readlines()

out = []
skip = 0
for i, line in enumerate(lines):
    if skip > 0:
        skip -= 1
        continue
    
    if "import com.example.BackupHelper" in line:
        continue
    if "isFirstLaunch =" in line:
        continue
    
    if "val intent = Intent(context, com.example." in line and "Activity::class.java).apply" in line:
        # PwaManagerActivity, LogKeeperActivity, AppyworkSettingsActivity
        out.append('                        android.widget.Toast.makeText(context, "Not Migrated", android.widget.Toast.LENGTH_SHORT).show()\n')
        # Skip the next 2-3 lines (addFlags, closing brace, startActivity)
        skip = 0
        j = i + 1
        while j < len(lines):
            if "context.startActivity(intent)" in lines[j]:
                skip = j - i
                break
            j += 1
        continue
        
    if "val result = runCatching { BackupHelper" in line or "val res = runCatching { BackupHelper" in line:
        # replace the whole runCatching block and the subsequent if(result.isSuccess)
        out.append('                        android.widget.Toast.makeText(context, "Not Migrated", android.widget.Toast.LENGTH_SHORT).show()\n')
        j = i + 1
        while j < len(lines):
            if lines[j].strip() == "}":
                # we need to find the matching brace of the `launch {` block?
                # actually, `val result = ...` is not a block if it's on one line.
                # Let's just skip until we see `} else {` and then `}`
                pass
            
            # The structure is:
            # val res = runCatching { BackupHelper... }
            # if (res.isSuccess) { Toast } else { Toast }
            # We want to skip all that.
            if "} else {" in lines[j]:
                skip = j - i + 2 # skip `} else {`, the toast, and `}`
                break
            j += 1
        continue
        
    out.append(line)

with open('app/src/main/java/com/example/feature/settings/SettingsActivity.kt', 'w') as f:
    f.writelines(out)
