with open('app/src/main/java/com/example/feature/settings/SettingsActivity.kt', 'r') as f:
    lines = f.readlines()

out = []
skip = 0
for i, line in enumerate(lines):
    if skip > 0:
        skip -= 1
        continue
    
    if "val res = /* BackupHelper call */" in line:
        # replace the whole if/else block below it with a simple toast or nothing
        out.append(line.replace("val res = /* BackupHelper call */", "Toast.makeText(context, \"Not Migrated\", Toast.LENGTH_SHORT).show()"))
        # We need to skip the next lines that use `res`
        # usually 5 lines: if (res.isSuccess) { ... } else { ... }
        skip = 5
    else:
        out.append(line)

with open('app/src/main/java/com/example/feature/settings/SettingsActivity.kt', 'w') as f:
    f.writelines(out)
