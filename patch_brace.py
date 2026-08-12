with open('app/src/main/java/com/example/feature/miniapps/reader/FloatingReaderService.kt', 'r') as f:
    lines = f.readlines()

for i, line in enumerate(lines):
    if "serviceLifecycleOwner?.let" in line:
        for j in range(i, i+5):
            if lines[j].strip() == "}":
                lines[j] = "        // }\n"
                break
        break

with open('app/src/main/java/com/example/feature/miniapps/reader/FloatingReaderService.kt', 'w') as f:
    f.writelines(lines)
