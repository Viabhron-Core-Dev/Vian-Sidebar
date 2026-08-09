package com.example.feature.miniapps

import android.content.Context
import android.view.View
import android.view.WindowManager
import com.example.core.FloatingWindow

class CalculatorFloatingWindow(context: Context) : FloatingWindow(context, "Calculator") {

    private var calculatorView: CalculatorPageView? = null

    override fun createContentView(): View {
        if (calculatorView == null) {
            calculatorView = CalculatorPageView(context)
        }
        return calculatorView!!
    }
}
