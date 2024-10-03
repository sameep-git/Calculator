package edu.tcu.sameepshah.calculator

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.lang.Double.parseDouble
import java.math.BigDecimal
import java.math.RoundingMode

class MainActivity : AppCompatActivity() {
    private var afterOperator: Boolean = false
    private var firstDigitZero: Boolean = true
    private var hasDot: Boolean = false
    private var valueStartIndex: Int = 0
    private var divZero: Boolean = false
    private var opStk: ArrayDeque<String> = ArrayDeque()
    private var valStk: ArrayDeque<BigDecimal> = ArrayDeque()
    private var equation:  MutableList<String> = mutableListOf()
    private var ops: MutableMap<String, Int> = mutableMapOf("$" to 0, "-" to 1, "+" to 1, "/" to 2, "*" to 2)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val resultTv = findViewById<TextView>(R.id.result_tv)
        findViewById<Button>(R.id.zero_btn).setOnClickListener { onDigit(resultTv,"0") }
        findViewById<Button>(R.id.one_btn).setOnClickListener { onDigit(resultTv,"1") }
        findViewById<Button>(R.id.two_btn).setOnClickListener { onDigit(resultTv,"2") }
        findViewById<Button>(R.id.three_btn).setOnClickListener { onDigit(resultTv,"3") }
        findViewById<Button>(R.id.four_btn).setOnClickListener { onDigit(resultTv,"4") }
        findViewById<Button>(R.id.five_btn).setOnClickListener { onDigit(resultTv,"5") }
        findViewById<Button>(R.id.six_btn).setOnClickListener { onDigit(resultTv,"6") }
        findViewById<Button>(R.id.seven_btn).setOnClickListener { onDigit(resultTv,"7") }
        findViewById<Button>(R.id.eight_btn).setOnClickListener { onDigit(resultTv,"8") }
        findViewById<Button>(R.id.nine_btn).setOnClickListener { onDigit(resultTv,"9") }
        findViewById<Button>(R.id.add_btn).setOnClickListener { onOperator(resultTv, "+") }
        findViewById<Button>(R.id.subtract_btn).setOnClickListener { onOperator(resultTv, "-") }
        findViewById<Button>(R.id.multiply_btn).setOnClickListener { onOperator(resultTv, "*") }
        findViewById<Button>(R.id.divide_btn).setOnClickListener { onOperator(resultTv, "/") }
        findViewById<Button>(R.id.dot_btn).setOnClickListener { onDot(resultTv) }
        findViewById<Button>(R.id.clear_btn).setOnClickListener { onClear(resultTv) }
        findViewById<Button>(R.id.equal_btn).setOnClickListener { onEqual(resultTv) }
    }
    private fun onDigit(resultTv: TextView, digit: String) {
        if (divZero) return
        val sb = StringBuilder()
        if (firstDigitZero) {
            if (digit != "0") {
                sb.append(resultTv.text.slice(0..<resultTv.text.length - 1).toString())
                sb.append(digit)
                resultTv.text = sb.toString()
                firstDigitZero = false
            }
        } else if (afterOperator) {
            sb.append(resultTv.text)
            sb.append(digit)
            resultTv.text = sb.toString()
            if (digit == "0") firstDigitZero = true
            afterOperator = false
        } else {
            sb.append(resultTv.text)
            sb.append(digit)
            resultTv.text = sb.toString()
        }
    }
    private fun onOperator(resultTv: TextView, operator: String) {
        if (divZero) return
        val sb = StringBuilder()
        if (!afterOperator) {
            sb.append(resultTv.text)
            sb.append(operator)
            resultTv.text = sb.toString()
            afterOperator = true
            firstDigitZero = false
            hasDot = false
            equation.add(resultTv.text.slice(valueStartIndex..<resultTv.text.length - 1).toString())
            equation.add(operator)
            valueStartIndex = resultTv.text.length
        }
    }

    private fun onDot(resultTv: TextView) {
        if (divZero) return
        val sb = StringBuilder()
        sb.append(resultTv.text)
        if (afterOperator) {
            sb.append("0")
        }
        if (!hasDot) {
            sb.append(".")
            hasDot = true
        }
        firstDigitZero = false
        resultTv.text = sb.toString()
    }

    private fun onClear(resultTv: TextView) {
        if (divZero) divZero = false
        resultTv.text = "0"
        valStk.clear()
        opStk.clear()
        equation.clear()
        valueStartIndex = 0
        hasDot = false
        afterOperator = false
        firstDigitZero = true
    }

    private fun onEqual(resultTv: TextView) {
        if (divZero) return
        val sb: StringBuilder = StringBuilder()
        if(!afterOperator && equation.size != 0) {
            equation.add(resultTv.text.slice(valueStartIndex..<resultTv.text.length).toString())
            val res: BigDecimal = evalExp(equation)
            if (!divZero) {
                sb.append(res.toString())
                resultTv.text = sb.toString()
                valueStartIndex = 0
                hasDot = false
                afterOperator = false
                valStk.clear()
                opStk.clear()
                equation.clear()
            } else resultTv.text = buildString {
        append("Error!\nTap CLR to\ncontinue.")
    }
        } else {
            println("Invalid expression.")
        }
    }

    private fun evalExp(equation: MutableList<String>): BigDecimal {
        for (i in 0..<equation.size) {
            if(isNumber(equation[i])) {
                valStk.addLast(equation[i].toBigDecimal())
            } else {
                repeatOps(equation[i])
                if (divZero) break
                opStk.addLast(equation[i])
            }
        }
        repeatOps("$")
        if (divZero) return BigDecimal.ZERO
        return valStk.last()
    }

    private fun isNumber(token: String): Boolean {
        var number = true
        try {
            parseDouble(token)
        } catch (e: NumberFormatException) {
            number = false
        }
        return number
    }

    private fun doOp() {
        val x = valStk.removeLast()
        val y = valStk.removeLast()
        val op = opStk.removeLast()
        if (op == "+") {
            valStk.addLast(y.add(x).setScale(8, RoundingMode.HALF_UP).stripTrailingZeros())
        } else if (op == "-") {
            valStk.addLast(y.subtract(x).setScale(8, RoundingMode.HALF_UP).stripTrailingZeros())
        } else if (op == "*") {
            valStk.addLast(y.multiply(x).setScale(8, RoundingMode.HALF_UP).stripTrailingZeros())
        } else if (op == "/") {
            if (x != BigDecimal.ZERO) valStk.addLast(y.divide(x, 8, RoundingMode.HALF_UP).stripTrailingZeros())
            else divZero = true
        }
    }

    private fun repeatOps(refOp: String) {
        while(valStk.size > 1 && (ops.getValue(opStk.last()) >= ops.getValue(refOp))) {
            doOp()
            if (divZero) break
        }
    }
}