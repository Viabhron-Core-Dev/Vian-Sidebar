#!/bin/bash
sed -i 's/Text("Dictionary Settings")/Text("Dictionary \& Translations")/g' app/src/main/java/com/example/DictionarySettingsScreen.kt

sed -i '/import androidx.compose.material3.\*/a\
import androidx.compose.material.icons.filled.Translate\
import androidx.compose.foundation.layout.Arrangement
' app/src/main/java/com/example/DictionarySettingsScreen.kt

cat << 'INNER_EOF' >> app/src/main/java/com/example/DictionarySettingsScreen.kt

@Composable
fun TranslationSettingsSection(context: Context) {
    val prefs = context.getSharedPreferences("TranslationPrefs", Context.MODE_PRIVATE)
    var targetLanguage by remember { mutableStateOf(prefs.getString("default_target_lang", com.google.mlkit.nl.translate.TranslateLanguage.ENGLISH) ?: com.google.mlkit.nl.translate.TranslateLanguage.ENGLISH) }
    
    Column(modifier = Modifier.padding(top = 16.dp)) {
        Text(
            text = "Translations",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        ListItem(
            headlineContent = { Text("Default Target Language") },
            supportingContent = { Text(java.util.Locale(targetLanguage).displayLanguage) },
            modifier = Modifier.clickable {
                // For simplicity, cycle through a few common languages or open manager.
                // Or better, just open translation manager to pick.
                context.startActivity(Intent(context, com.example.service.TranslationManagementActivity::class.java))
            }
        )
        Divider()
        ListItem(
            headlineContent = { Text("Manage Language Models") },
            supportingContent = { Text("Download offline ML Kit translation models") },
            trailingContent = { Icon(Icons.Filled.Translate, "Translate") },
            modifier = Modifier.clickable {
                context.startActivity(Intent(context, com.example.service.TranslationManagementActivity::class.java))
            }
        )
    }
}
INNER_EOF

# Inject TranslationSettingsSection inside DictionarySettingsScreen just before the end of the LazyColumn or Column.
sed -i '/Text("You can import StarDict dictionaries/i\
            TranslationSettingsSection(context)\
            Spacer(modifier = Modifier.height(16.dp))' app/src/main/java/com/example/DictionarySettingsScreen.kt

