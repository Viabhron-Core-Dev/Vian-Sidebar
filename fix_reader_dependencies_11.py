with open("app/src/main/java/com/example/feature/miniapps/reader/FloatingReaderService.kt", "r") as f:
    lines = f.readlines()

for i in range(235, 256):
    if i < len(lines):
        lines[i] = "// " + lines[i]

with open("app/src/main/java/com/example/feature/miniapps/reader/FloatingReaderService.kt", "w") as f:
    f.writelines(lines)
