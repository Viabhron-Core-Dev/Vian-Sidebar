package com.example.service

import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import com.example.R
import java.text.DecimalFormat

class CalculatorPageView(context: Context) : FrameLayout(context) {

    private var expression = ""
    private var resultText = ""
    private var expressionCompleted = false

    private val tvExpression: TextView
    private val tvResult: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.page_calculator, this, true)
        tvExpression = findViewById(R.id.tv_expression)
        tvResult = findViewById(R.id.tv_result)

        val tableLayout = findViewById<TableLayout>(R.id.tableLayout)
        
        // Setup click listeners for all textviews inside the tablelayout
        for (i in 0 until (getChildAt(0) as android.view.ViewGroup).childCount) {
             val view = (getChildAt(0) as android.view.ViewGroup).getChildAt(i)
             if (view is TableLayout) {
                 for (j in 0 until view.childCount) {
                     val row = view.getChildAt(j) as TableRow
                     for (k in 0 until row.childCount) {
                         val btn = row.getChildAt(k) as TextView
                         btn.setOnClickListener { onBtnClick(btn.text.toString()) }
                     }
                 }
             }
        }
        updateUI()
    }

    private fun onBtnClick(btn: String) {
        when (btn) {
            "C" -> onClear()
            "DEL" -> onDelete()
            "=" -> onEqual()
            else -> onInput(btn)
        }
    }

    private fun onClear() {
        expression = ""
        resultText = ""
        expressionCompleted = false
        updateUI()
    }

    private fun onDelete() {
        if (expressionCompleted) {
            expression = ""
            resultText = ""
            expressionCompleted = false
        } else if (expression.isNotEmpty()) {
            expression = expression.dropLast(1)
            val res = evaluateExpression(expression)
            if (res != "Error" && res.isNotEmpty() && expression.any { it in listOf('+', '-', 'x', '÷', '%') }) {
                resultText = "=$res"
            } else {
                resultText = ""
            }
        }
        updateUI()
    }

    private fun onInput(input: String) {
        if (expressionCompleted) {
            expression = if (input in listOf("+", "-", "x", "÷", "%")) {
                resultText.removePrefix("=") + input
            } else {
                input
            }
            expressionCompleted = false
        } else {
            expression += input
        }
        val res = evaluateExpression(expression)
        if (res != "Error" && res.isNotEmpty() && expression.any { it in listOf('+', '-', 'x', '÷', '%') }) {
            resultText = "=$res"
        } else {
            resultText = ""
        }
        updateUI()
    }

    private fun onEqual() {
        if (expression.isNotEmpty()) {
            val res = evaluateExpression(expression)
            if (res != "Error") {
                resultText = "=$res"
                expressionCompleted = true
            }
        }
        updateUI()
    }

    private fun updateUI() {
        val displayTextTop = if (resultText.isNotEmpty() || expressionCompleted) formatExpression(expression) else ""
        val displayTextBottom = when {
            expression.isEmpty() -> "0"
            resultText.isNotEmpty() -> resultText
            else -> formatExpression(expression)
        }

        tvExpression.text = displayTextTop
        tvResult.text = displayTextBottom
    }

    private fun evaluateExpression(expr: String): String {
        if (expr.isEmpty()) return ""
        val cleanExpr = expr.replace("x", "*").replace("÷", "/")
        return try {
            val result = evalBasic(cleanExpr)
            val format = DecimalFormat("0.######")
            format.format(result)
        } catch (e: Exception) {
            "Error"
        }
    }

    private fun formatExpression(expr: String): String {
        val regex = Regex("(\\d+\\.?\\d*)")
        return regex.replace(expr) { matchResult ->
            try {
                val numStr = matchResult.value
                if (numStr.contains(".")) {
                    val parts = numStr.split(".")
                    val formatter = DecimalFormat("#,###")
                    formatter.format(parts[0].toLong()) + "." + parts[1]
                } else {
                    val formatter = DecimalFormat("#,###")
                    formatter.format(numStr.toLong())
                }
            } catch (e: Exception) {
                matchResult.value
            }
        }
    }

    private fun evalBasic(str: String): Double {
        return object : Any() {
            var pos = -1
            var ch = 0
            fun nextChar() {
                ch = if (++pos < str.length) str[pos].toInt() else -1
            }

            fun eat(charToEat: Int): Boolean {
                while (ch == ' '.toInt()) nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < str.length) throw RuntimeException("Unexpected: " + ch.toChar())
                return x
            }

            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    if (eat('+'.toInt())) x += parseTerm() // addition
                    else if (eat('-'.toInt())) x -= parseTerm() // subtraction
                    else return x
                }
            }

            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    if (eat('*'.toInt())) x *= parseFactor() // multiplication
                    else if (eat('/'.toInt())) x /= parseFactor() // division
                    else return x
                }
            }

            fun parseFactor(): Double {
                if (eat('+'.toInt())) return parseFactor() // unary plus
                if (eat('-'.toInt())) return -parseFactor() // unary minus
                var x: Double
                val startPos = pos
                if (eat('('.toInt())) { // parentheses
                    x = parseExpression()
                    eat(')'.toInt())
                } else if (ch >= '0'.toInt() && ch <= '9'.toInt() || ch == '.'.toInt()) { // numbers
                    while (ch >= '0'.toInt() && ch <= '9'.toInt() || ch == '.'.toInt()) nextChar()
                    x = str.substring(startPos, pos).toDouble()
                } else {
                    throw RuntimeException("Unexpected: " + ch.toChar())
                }
                while (eat('%'.toInt())) {
                    x /= 100.0
                }
                return x
            }
        }.parse()
    }
}
