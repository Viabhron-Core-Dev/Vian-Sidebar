import re
with open('app/src/main/java/com/example/feature/miniapps/MiniAppManager.kt', 'r') as f:
    data = f.read()

data = data.replace('            (it is CalculatorFloatingWindow && pageType == "calculator") ||\n', '')
data = data.replace('            (it is CompassFloatingWindow && pageType == "compass") ||\n', '')
data = data.replace('            "calculator" -> CalculatorFloatingWindow(context)\n', '')
data = data.replace('            "compass" -> CompassFloatingWindow(context)\n', '')

with open('app/src/main/java/com/example/feature/miniapps/MiniAppManager.kt', 'w') as f:
    f.write(data)
