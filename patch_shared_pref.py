import re

with open('app/src/main/java/com/example/core/HandleService.kt', 'r') as f:
    text = f.read()

# Remove the one I injected
bad_impl = """    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == "netspeed_enabled") {
            setupNetSpeed()
        } else if (key == "call_recorder_enabled" || key == "call_recorder_manual_enabled") {
            setupCallRecorder()
        }
    }"""
text = text.replace(bad_impl, "")

# Modify the original one
original_impl = """    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key != null && (key.startsWith("handle_") || key == "handles_list")) {"""
new_impl = """    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == "netspeed_enabled") {
            setupNetSpeed()
        } else if (key == "call_recorder_enabled" || key == "call_recorder_manual_enabled") {
            setupCallRecorder()
        }
        
        if (key != null && (key.startsWith("handle_") || key == "handles_list")) {"""
text = text.replace(original_impl, new_impl)

with open('app/src/main/java/com/example/core/HandleService.kt', 'w') as f:
    f.write(text)

