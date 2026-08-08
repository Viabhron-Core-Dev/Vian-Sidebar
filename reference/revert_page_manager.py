import re

with open('app/src/main/java/com/example/utils/PageManager.kt', 'r') as f:
    content = f.read()

replacement = """
        try {
            val arr = JSONArray(pagesJson)
            for (i in 0 until arr.length()) {
                list.add(SidebarPage.fromJson(arr.getJSONObject(i)))
            }
"""

content = re.sub(r'try \{\n\s*val arr = JSONArray\(pagesJson\)\n\s*for \(i in 0 until arr\.length\(\)\) \{.*?list\.add\(p\)\n\s*\}', replacement.strip(), content, flags=re.DOTALL)

with open('app/src/main/java/com/example/utils/PageManager.kt', 'w') as f:
    f.write(content)

print("Reverted PageManager.")
