import re

with open('app/src/main/java/com/example/feature/miniapps/reader/FloatingReaderService.kt', 'r') as f:
    text = f.read()

old_unfold = """        } else if (unfold) {
            val lastBook = prefs.getInt("last_book_id", -1)
            if (currentBook == null && lastBook != -1) loadBook(lastBook)
            setFolded(false)
        }"""

new_unfold = """        } else if (unfold) {
            val lastBook = prefs.getInt("last_book_id", -1)
            if (currentBook == null && lastBook != -1) loadBook(lastBook)
            if (floatingView?.windowToken != null && !isFolded) {
                setFolded(true)
            } else {
                setFolded(false)
            }
        }"""

text = text.replace(old_unfold, new_unfold)

with open('app/src/main/java/com/example/feature/miniapps/reader/FloatingReaderService.kt', 'w') as f:
    f.write(text)
