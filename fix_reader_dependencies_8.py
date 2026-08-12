import re

with open("app/src/main/java/com/example/feature/miniapps/reader/FloatingReaderService.kt", "r") as f:
    text = f.read()

# I will replace the problematic block with a commented block
bad_block = """//     fun launchPwa(pwa: PwaEntry) {
        if (!pwaWindows.containsKey(pwa.id)) {
// //             val windowManager = PwaWindowManager(this, pwa)
            pwaWindows[pwa.id] = windowManager
//             windowManager.show()
        }
    }

    fun removePwaWindow(id: Int) {
        pwaWindows.remove(id)
    }

    fun toggleDictionaryWindow() {
        if (dictWindowManager == null) {
//             dictWindowManager = DictionaryWindowManager(this)
        }
//         dictWindowManager?.toggle()
    }"""

good_block = """//     fun launchPwa(pwa: Any) {
//         if (!pwaWindows.containsKey(pwa.id)) {
// // //             val windowManager = PwaWindowManager(this, pwa)
//             pwaWindows[pwa.id] = windowManager
// //             windowManager.show()
//         }
//     }
//
//     fun removePwaWindow(id: Int) {
//         pwaWindows.remove(id)
//     }
//
//     fun toggleDictionaryWindow() {
//         if (dictWindowManager == null) {
// //             dictWindowManager = DictionaryWindowManager(this)
//         }
// //         dictWindowManager?.toggle()
//     }"""

text = text.replace(bad_block, good_block)

with open("app/src/main/java/com/example/feature/miniapps/reader/FloatingReaderService.kt", "w") as f:
    f.write(text)
