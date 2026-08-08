#!/bin/bash
# First modify DictionaryWindowManager.kt imports
sed -i 's/import java.util.Locale/import java.util.Locale\
import android.widget.Spinner\
import android.widget.ArrayAdapter\
import android.widget.AdapterView\
import com.google.mlkit.common.model.DownloadConditions\
import com.google.mlkit.nl.languageid.LanguageIdentification\
import com.google.mlkit.nl.translate.TranslateLanguage\
import com.google.mlkit.nl.translate.Translation\
import com.google.mlkit.nl.translate.TranslatorOptions/' app/src/main/java/com/example/service/DictionaryWindowManager.kt

# Change show signature
sed -i 's/fun show(startFullscreen: Boolean = false) {/fun show(startFullscreen: Boolean = false, initialTabTranslate: Boolean = false) {/' app/src/main/java/com/example/service/DictionaryWindowManager.kt

# Let's insert the code right after inflate
