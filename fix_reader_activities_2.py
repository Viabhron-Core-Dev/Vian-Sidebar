import re

with open("app/src/main/java/com/example/feature/miniapps/reader/FloatingReaderService.kt", "r") as f:
    text = f.read()

text = re.sub(
    r'(floatingView\.findViewById<View>\(R\.id\.btn_top_notes\)\?\.setOnClickListener \{.*?^\s*\})',
    r'// \1',
    text,
    flags=re.DOTALL | re.MULTILINE
)

text = re.sub(
    r'(val intent = android\.content\.Intent\(this, com\.example\.TrackerActivity::class\.java\).*?startActivity\(intent\).*?\n)',
    r'// \1',
    text,
    flags=re.DOTALL | re.MULTILINE
)

with open("app/src/main/java/com/example/feature/miniapps/reader/FloatingReaderService.kt", "w") as f:
    f.write(text)
