with open('app/build.gradle.kts', 'r') as f:
    content = f.read()

content = content.replace(
    'implementation("androidx.recyclerview:recyclerview:1.3.2")',
    'implementation("androidx.recyclerview:recyclerview:1.3.2")\n    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")'
)

with open('app/build.gradle.kts', 'w') as f:
    f.write(content)
