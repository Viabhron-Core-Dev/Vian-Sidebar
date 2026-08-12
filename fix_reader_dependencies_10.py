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
//         if (false) {
// // //             val windowManager = PwaWindowManager(this, pwa)
// //             pwaWindows[pwa.id] = windowManager
// //             windowManager.show()
//         }
//     }
//
//     fun removePwaWindow(id: Int) {
// //        pwaWindows.remove(id)
//     }
//
//     fun toggleDictionaryWindow() {
//         if (dictWindowManager == null) {
// //             dictWindowManager = DictionaryWindowManager(this)
//         }
// //         dictWindowManager?.toggle()
//     }"""

if bad_block in text:
    print("Found bad block")
    text = text.replace(bad_block, good_block)
else:
    print("Could not find bad block!")

# Let's just do a regex sub to be sure
text = re.sub(r'//     fun launchPwa\(pwa:\s*PwaEntry\)\s*\{.*?dictWindowManager\?\.toggle\(\)\s*\}', good_block, text, flags=re.DOTALL)
text = re.sub(r'//     fun launchPwa\(pwa:\s*Any\)\s*\{.*?dictWindowManager\?\.toggle\(\)\s*\}', good_block, text, flags=re.DOTALL)

with open("app/src/main/java/com/example/feature/miniapps/reader/FloatingReaderService.kt", "w") as f:
    f.write(text)
