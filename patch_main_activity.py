with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    text = f.read()

old_block = """                        onContinue = {
                            prefs.edit().putBoolean("first_launch", false).apply()
                            startSidebarService()
                            startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                            finish()
                        },"""

new_block = """                        onContinue = {
                            prefs.edit().putBoolean("first_launch", false).apply()
                            startSidebarService()
                            startActivity(Intent(this@MainActivity, SettingsActivity::class.java).apply {
                                putExtra("start_route", "handles")
                            })
                            finish()
                        },"""

text = text.replace(old_block, new_block)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(text)
