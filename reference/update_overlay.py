import re

with open('app/src/main/java/com/example/service/AddElementOverlayView.kt', 'r') as f:
    content = f.read()

# Add handling for WIDGET action type in handleActionClick
widget_handler = """            ActionType.WIDGET -> {
                val intent = android.content.Intent(context, com.example.WidgetPickerActivity::class.java).apply {
                    putExtra("ACTION_TYPE", "ADD_ELEMENT")
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (targetFolderUuid != null) {
                    intent.putExtra("FOLDER_UUID", targetFolderUuid)
                }
                if (onElementSelected != null) {
                    intent.putExtra("IS_ELEMENT_CALLBACK", true)
                }
                context.startActivity(intent)
                close()
            }
            ActionType.FOLDER -> {"""

content = content.replace("            ActionType.FOLDER -> {", widget_handler)

with open('app/src/main/java/com/example/service/AddElementOverlayView.kt', 'w') as f:
    f.write(content)

