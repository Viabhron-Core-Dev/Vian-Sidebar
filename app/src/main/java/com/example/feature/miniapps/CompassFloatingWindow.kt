package com.example.feature.miniapps

import android.content.Context
import android.view.View
import android.view.WindowManager
import com.example.core.FloatingWindow

class CompassFloatingWindow(context: Context) : FloatingWindow(context, "Compass") {

    private var compassView: CompassPageView? = null

    override fun createContentView(): View {
        if (compassView == null) {
            compassView = CompassPageView(context)
        }
        return compassView!!
    }
}
