with open('app/src/main/java/com/example/feature/sidebar/SidebarView.kt', 'r') as f:
    text = f.read()

text = text.replace('        addView(viewPager)\n\n        addView(viewPager)', '        addView(viewPager)')

with open('app/src/main/java/com/example/feature/sidebar/SidebarView.kt', 'w') as f:
    f.write(text)
