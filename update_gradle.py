with open("app/build.gradle.kts", "r") as f:
    text = f.read()

text = text.replace(
    'implementation("com.google.android.gms:play-services-mlkit-language-id:17.0.0")',
    'implementation("com.google.android.gms:play-services-mlkit-language-id:17.0.0")\n    implementation("com.google.mlkit:translate:17.0.2")'
)

with open("app/build.gradle.kts", "w") as f:
    f.write(text)
