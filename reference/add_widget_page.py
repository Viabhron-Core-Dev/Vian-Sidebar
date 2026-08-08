import re

with open('app/src/main/java/com/example/PageManagementSettingsScreen.kt', 'r') as f:
    content = f.read()

widget_type = """                            "contacts" to "Contacts",
                            "scheduler" to "Scheduler",
                            "calculator" to "Calculator",
                            "compass" to "Compass",
                            "reader" to "Reader",
                            "notification" to "Notification History",
                            "widget" to "App Widget"
                        )"""

content = re.sub(r'"contacts" to "Contacts",\s*"scheduler" to "Scheduler",\s*"calculator" to "Calculator",\s*"compass" to "Compass",\s*"reader" to "Reader",\s*"notification" to "Notification History"\s*\)', widget_type, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/PageManagementSettingsScreen.kt', 'w') as f:
    f.write(content)

