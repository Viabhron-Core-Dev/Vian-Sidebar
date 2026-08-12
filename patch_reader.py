import re
with open('app/src/main/java/com/example/feature/miniapps/reader/FloatingReaderService.kt', 'r') as f:
    data = f.read()

data = data.replace('        serviceLifecycleOwner?.let {', '        // serviceLifecycleOwner?.let {')
data = data.replace('            floatingView.setViewTreeViewModelStoreOwner(it)', '        //     floatingView.setViewTreeViewModelStoreOwner(it)')
data = data.replace('            floatingView.setViewTreeSavedStateRegistryOwner(it)', '        //     floatingView.setViewTreeSavedStateRegistryOwner(it)')
data = data.replace('        }\n\n        handleView', '        // }\n\n        handleView')

with open('app/src/main/java/com/example/feature/miniapps/reader/FloatingReaderService.kt', 'w') as f:
    f.write(data)
