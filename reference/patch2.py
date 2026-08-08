with open("app/src/main/java/com/example/AppPickerActivity.kt", "r") as f:
    content = f.read()

import re
content = re.sub(
    r'\} else \{\s*view\.findViewById<ImageView>\(1\)\.setImageResource\(android\.R\.drawable\.sym_def_app_icon\)\s*\}',
    """} else {
                            view.findViewById<ImageView>(1).setImageResource(android.R.drawable.sym_def_app_icon)
                            scope.launch {
                                val loaded = manager.loadIcon(appInfo.packageName)
                                withContext(Dispatchers.Main) {
                                    if (loaded != null) {
                                        view.findViewById<ImageView>(1).setImageBitmap(loaded)
                                    }
                                }
                            }
                        }""",
    content
)

with open("app/src/main/java/com/example/AppPickerActivity.kt", "w") as f:
    f.write(content)
