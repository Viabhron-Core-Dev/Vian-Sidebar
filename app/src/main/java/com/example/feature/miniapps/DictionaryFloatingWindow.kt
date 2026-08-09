package com.example.feature.miniapps

import android.content.Context
import android.view.View
import android.view.WindowManager
import com.example.core.FloatingWindow

class DictionaryFloatingWindow(context: Context) : FloatingWindow(context, "Dictionary") {

    private var dictionaryView: DictionaryPageView? = null

    override fun createContentView(): View {
        if (dictionaryView == null) {
            dictionaryView = DictionaryPageView(context)
        }
        return dictionaryView!!
    }
}
