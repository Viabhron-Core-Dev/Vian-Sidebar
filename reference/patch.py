with open("app/src/main/java/com/example/AppPickerActivity.kt", "r") as f:
    lines = f.readlines()

# Remove the bad lines at the end
while "manager.ensureLoaded()" in lines[-1] or "}" in lines[-1]:
    lines.pop()

lines.append("        }\n")
lines.append("        manager.ensureLoaded()\n")
lines.append("    }\n")
lines.append("}\n")

with open("app/src/main/java/com/example/AppPickerActivity.kt", "w") as f:
    f.writelines(lines)
