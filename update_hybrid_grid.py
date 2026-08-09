import re

with open('app/src/main/java/com/example/feature/sidebar/HybridGridPageView.kt', 'r') as f:
    content = f.read()

pattern1 = r'''val intent = Intent\(context, PageWindowService::class\.java\)\.apply \{
\s*action = "TOGGLE"
\s*putExtra\("PAGE_TYPE", parsed\.pageType\)
\s*\}
\s*context\.startService\(intent\)'''

content = re.sub(pattern1, r'com.example.feature.miniapps.MiniAppManager.toggleApp(context, parsed.pageType)', content)
content = re.sub(r'import com\.example\.service\.PageWindowService\n', '', content)

with open('app/src/main/java/com/example/feature/sidebar/HybridGridPageView.kt', 'w') as f:
    f.write(content)
