with open("app/src/main/java/com/example/feature/miniapps/reader/FloatingReaderService.kt", "r") as f:
    text = f.read()

text = text.replace("serviceLifecycleOwner?.onPause()", "// serviceLifecycleOwner?.onPause()")
text = text.replace("serviceLifecycleOwner?.onStop()", "// serviceLifecycleOwner?.onStop()")
text = text.replace("serviceLifecycleOwner?.onDestroy()", "// serviceLifecycleOwner?.onDestroy()")
text = text.replace("serviceLifecycleOwner?.onCreate()", "// serviceLifecycleOwner?.onCreate()")
text = text.replace("serviceLifecycleOwner?.onStart()", "// serviceLifecycleOwner?.onStart()")
text = text.replace("serviceLifecycleOwner?.onResume()", "// serviceLifecycleOwner?.onResume()")

with open("app/src/main/java/com/example/feature/miniapps/reader/FloatingReaderService.kt", "w") as f:
    f.write(text)
