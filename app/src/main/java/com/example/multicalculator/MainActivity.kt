package com.example.multicalculator

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.*

class MainActivity : AppCompatActivity() {

    private lateinit var display: TextView
    private lateinit var history: TextView

    private var expression = ""
    private var memory = 0.0
    private var historyText = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        display = findViewById(R.id.display)
        history = findViewById(R.id.history)

        setupButtons()
    }

    private fun setupButtons() {

        val numbers = mapOf(
            R.id.btn0 to "0",
            R.id.btn1 to "1",
            R.id.btn2 to "2",
            R.id.btn3 to "3",
            R.id.btn4 to "4",
            R.id.btn5 to "5",
            R.id.btn6 to "6",
            R.id.btn7 to "7",
            R.id.btn8 to "8",
            R.id.btn9 to "9"
        )

        numbers.forEach { (id, value) ->
            findViewById<Button>(id).setOnClickListener {
                expression += value
                updateDisplay()
            }
        }

        findViewById<Button>(R.id.btnDot).setOnClickListener {
            expression += "."
            updateDisplay()
        }

        findViewById<Button>(R.id.btnPlus).setOnClickListener {
            addOperator("+")
        }

        findViewById<Button>(R.id.btnMinus).setOnClickListener {
            addOperator("-")
        }

        findViewById<Button>(R.id.btnMultiply).setOnClickListener {
            addOperator("*")
        }

        findViewById<Button>(R.id.btnDivide).setOnClickListener {
            addOperator("/")
        }

        findViewById<Button>(R.id.btnPower).setOnClickListener {
            addOperator("^")
        }

        findViewById<Button>(R.id.btnOpen).setOnClickListener {
            expression += "("
            updateDisplay()
        }

        findViewById<Button>(R.id.btnClose).setOnClickListener {
            expression += ")"
            updateDisplay()
        }

        findViewById<Button>(R.id.btnClear).setOnClickListener {
            expression = ""
            display.text = "0"
        }

        findViewById<Button>(R.id.btnBack).setOnClickListener {
            if (expression.isNotEmpty()) {
                expression = expression.dropLast(1)
                updateDisplay()
            }
        }

        findViewById<Button>(R.id.btnEqual).setOnClickListener {
            calculate()
        }

        findViewById<Button>(R.id.btnPercent).setOnClickListener {
            percent()
        }

        findViewById<Button>(R.id.btnSquare).setOnClickListener {
            unaryOperation { it.pow(2) }
        }

        findViewById<Button>(R.id.btnCube).setOnClickListener {
            unaryOperation { it.pow(3) }
        }

        findViewById<Button>(R.id.btnSqrt).setOnClickListener {
            unaryOperation {
                if (it < 0) throw Exception()
                sqrt(it)
            }
        }

        findViewById<Button>(R.id.btnCbrt).setOnClickListener {
            unaryOperation {
                cbrt(it)
            }
        }

        findViewById<Button>(R.id.btnSin).setOnClickListener {
            unaryOperation {
                sin(Math.toRadians(it))
            }
        }

        findViewById<Button>(R.id.btnCos).setOnClickListener {
            unaryOperation {
                cos(Math.toRadians(it))
            }
        }

        findViewById<Button>(R.id.btnTan).setOnClickListener {
            unaryOperation {
                tan(Math.toRadians(it))
            }
        }

        findViewById<Button>(R.id.btnLog).setOnClickListener {
            unaryOperation {
                if (it <= 0) throw Exception()
                log10(it)
            }
        }

        findViewById<Button>(R.id.btnLn).setOnClickListener {
            unaryOperation {
                if (it <= 0) throw Exception()
                ln(it)
            }
        }

        findViewById<Button>(R.id.btnPi).setOnClickListener {
            expression += Math.PI
            updateDisplay()
        }

        findViewById<Button>(R.id.btnE).setOnClickListener {
            expression += Math.E
            updateDisplay()
        }

        // MEMORY

        findViewById<Button>(R.id.btnMC).setOnClickListener {
            memory = 0.0
        }

        findViewById<Button>(R.id.btnMR).setOnClickListener {
            expression = formatNumber(memory)
            updateDisplay()
        }

        findViewById<Button>(R.id.btnMPlus).setOnClickListener {
            memory += currentValue()
        }

        findViewById<Button>(R.id.btnMMinus).setOnClickListener {
            memory -= currentValue()
        }
    }

    private fun addOperator(operator: String) {

        if (expression.isEmpty()) return

        val last = expression.last()

        if (last !in "+-*/^") {
            expression += operator
            updateDisplay()
        }
    }

    private fun unaryOperation(operation: (Double) -> Double) {

        try {

            val value = currentValue()
            val result = operation(value)

            if (result.isNaN() || result.isInfinite()) {
                throw Exception()
            }

            expression = formatNumber(result)

            updateDisplay()

        } catch (_: Exception) {
            display.text = "Ошибка"
        }
    }

    private fun percent() {

        try {

            val value = currentValue()

            expression = formatNumber(value / 100)

            updateDisplay()

        } catch (_: Exception) {
            display.text = "Ошибка"
        }
    }

    private fun currentValue(): Double {

        if (expression.isEmpty()) {
            return 0.0
        }

        return ExpressionParser(expression).parse()
    }

    private fun calculate() {

        try {

            val oldExpression = expression

            val result = currentValue()

            if (result.isNaN() || result.isInfinite()) {
                throw Exception()
            }

            expression = formatNumber(result)

            display.text = expression

            historyText =
                "$oldExpression = ${formatNumber(result)}\n$historyText"

            history.text = historyText

        } catch (_: Exception) {

            display.text = "Ошибка"
        }
    }

    private fun updateDisplay() {

        display.text =
            if (expression.isEmpty()) "0"
            else expression
    }

    private fun formatNumber(number: Double): String {

        return if (number % 1.0 == 0.0) {
            number.toLong().toString()
        } else {
            String.format("%.10f", number)
                .trimEnd('0')
                .trimEnd('.')
        }
    }

    // ============================
    // EXPRESSION PARSER
    // ============================

    class ExpressionParser(
        private val text: String
    ) {

        private var position = 0

        fun parse(): Double {

            val result = parseExpression()

            if (position < text.length) {
                throw Exception()
            }

            return result
        }

        private fun parseExpression(): Double {

            var result = parseTerm()

            while (position < text.length) {

                skipSpaces()

                if (position >= text.length) break

                when (text[position]) {

                    '+' -> {
                        position++
                        result += parseTerm()
                    }

                    '-' -> {
                        position++
                        result -= parseTerm()
                    }

                    else -> break
                }
            }

            return result
        }

        private fun parseTerm(): Double {

            var result = parsePower()

            while (position < text.length) {

                skipSpaces()

                if (position >= text.length) break

                when (text[position]) {

                    '*' -> {
                        position++
                        result *= parsePower()
                    }

                    '/' -> {
                        position++

                        val value = parsePower()

                        if (value == 0.0) {
                            throw ArithmeticException()
                        }

                        result /= value
                    }

                    else -> break
                }
            }

            return result
        }

        private fun parsePower(): Double {

            var result = parseFactor()

            skipSpaces()

            if (
                position < text.length &&
                text[position] == '^'
            ) {

                position++

                val exponent = parsePower()

                result = result.pow(exponent)
            }

            return result
        }

        private fun parseFactor(): Double {

            skipSpaces()

            if (position >= text.length) {
                throw Exception()
            }

            if (text[position] == '+') {
                position++
                return parseFactor()
            }

            if (text[position] == '-') {
                position++
                return -parseFactor()
            }

            if (text[position] == '(') {

                position++

                val result = parseExpression()

                skipSpaces()

                if (
                    position >= text.length ||
                    text[position] != ')'
                ) {
                    throw Exception()
                }

                position++

                return result
            }

            val start = position

            while (
                position < text.length &&
                (
                    text[position].isDigit() ||
                    text[position] == '.'
                )
            ) {
                position++
            }

            if (start == position) {
                throw Exception()
            }

            return text
                .substring(start, position)
                .toDouble()
        }

        private fun skipSpaces() {

            while (
                position < text.length &&
                text[position].isWhitespace()
            ) {
                position++
            }
        }
    }
}