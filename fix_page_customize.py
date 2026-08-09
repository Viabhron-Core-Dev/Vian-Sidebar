import re

with open('app/src/main/java/com/example/feature/settings/PageCustomizeScreen.kt', 'r') as f:
    content = f.read()

# Replace the onClick block
pattern = r'onClick = \{\s*val intent = if.*?context\.startActivity\(intent\)\s*\},'
content = re.sub(pattern, 'onClick = { /* Not supported in this phase */ },', content, flags=re.DOTALL)

with open('app/src/main/java/com/example/feature/settings/PageCustomizeScreen.kt', 'w') as f:
    f.write(content)
