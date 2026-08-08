import sys

with open('app/src/main/java/com/example/service/FloatingReaderService.kt', 'r') as f:
    lines = f.readlines()

# Line 147
# We need to find `onSpeedUpdate = { down, up ->` and close it properly
for i in range(len(lines)):
    if "onSpeedUpdate = { down, up ->" in lines[i]:
        # go down until we see `}` and `onDailyDataUpdate`
        for j in range(i, i+10):
            if "onDailyDataUpdate" in lines[j]:
                if lines[j-1].strip() == "}":
                    lines[j-1] = lines[j-1].replace("}", "},")
                break
        break

# Line 993 and 2190
# Let's find `v.postDelayed({` and match its `}`
for i in range(len(lines)):
    if "v.postDelayed({" in lines[i]:
        for j in range(i, i+50):
            if lines[j].strip() == "}":
                # Check if it's the right one by indentation?
                # Actually, there are multiple `}`. The one matching postDelayed should be `}, 50)`
                # Let's just look at line 993
                pass

# Let's just do it directly.
def replace_line_near(target, search, replacement):
    for i in range(len(lines)):
        if target in lines[i]:
            for j in range(i, min(i+50, len(lines))):
                if search in lines[j]:
                    lines[j] = lines[j].replace(search, replacement)
                    return

replace_line_near("v.postDelayed({", "                    }", "                    }, 50)")
replace_line_near("scrollView.postDelayed({", "                    }", "                    }, 50)")

with open('app/src/main/java/com/example/service/FloatingReaderService.kt', 'w') as f:
    f.writelines(lines)
