package com.example

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

data class CalculationItem(
    val id: String = UUID.randomUUID().toString(),
    val expression: String,
    val result: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class CalculatorUiState(
    val expression: String = "",
    val result: String = "",
    val realtimePreview: String = "",
    val history: List<CalculationItem> = emptyList(),
    val isFreshResult: Boolean = false
)

class CalculatorViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState = _uiState.asStateFlow()

    fun onKeyPress(key: String) {
        _uiState.update { currentState ->
            var newExpression = currentState.expression
            var newResult = currentState.result
            var newRealtimePreview = currentState.realtimePreview
            var newIsFreshResult = currentState.isFreshResult
            var newHistory = currentState.history

            when (key) {
                "AC" -> {
                    newExpression = ""
                    newResult = ""
                    newRealtimePreview = ""
                    newIsFreshResult = false
                }
                "C" -> {
                    newExpression = ""
                    newRealtimePreview = ""
                    newIsFreshResult = false
                }
                "⌫" -> {
                    if (newExpression.isNotEmpty()) {
                        newExpression = newExpression.trimEnd()
                        if (newExpression.isNotEmpty()) {
                            newExpression = newExpression.dropLast(1)
                        }
                        newExpression = newExpression.trimEnd()
                    }
                    newIsFreshResult = false
                }
                "=" -> {
                    val evaluated = evaluateExpression(newExpression)
                    if (evaluated != null && evaluated != "Divide by zero") {
                        val formattedResult = evaluated
                        val item = CalculationItem(
                            expression = newExpression,
                            result = formattedResult
                        )
                        newHistory = listOf(item) + newHistory
                        newExpression = formattedResult
                        newResult = formattedResult
                        newRealtimePreview = ""
                        newIsFreshResult = true
                    } else if (evaluated == "Divide by zero") {
                        newResult = "Divide by zero"
                        newRealtimePreview = ""
                    }
                }
                "+/-" -> {
                    newExpression = toggleLastNumberSign(newExpression)
                    newIsFreshResult = false
                }
                "%" -> {
                    val trimmed = newExpression.trim()
                    if (trimmed.isNotEmpty() && (trimmed.last().isDigit() || trimmed.last() == '%')) {
                        newExpression = "$trimmed%"
                    }
                    newIsFreshResult = false
                }
                "+", "−", "×", "÷" -> {
                    newIsFreshResult = false
                    val trimmed = newExpression.trimEnd()
                    if (trimmed.isEmpty()) {
                        if (key == "−") {
                            newExpression = "−"
                        }
                    } else {
                        val lastChar = trimmed.last()
                        if (lastChar == '+' || lastChar == '−' || lastChar == '×' || lastChar == '÷') {
                            newExpression = trimmed.dropLast(1).trimEnd() + " " + key + " "
                        } else {
                            newExpression = "$trimmed $key "
                        }
                    }
                }
                "." -> {
                    newIsFreshResult = false
                    val lastToken = newExpression.split(" ").lastOrNull() ?: ""
                    if (!lastToken.contains(".")) {
                        if (lastToken.isEmpty() || lastToken == "+" || lastToken == "−" || lastToken == "×" || lastToken == "÷") {
                            newExpression += "0."
                        } else {
                            newExpression += "."
                        }
                    }
                }
                else -> { // Digit
                    if (newIsFreshResult) {
                        newExpression = key
                        newIsFreshResult = false
                    } else {
                        newExpression += key
                    }
                }
            }

            // Realtime preview calculation
            newRealtimePreview = if (newExpression.trim().isNotEmpty() && 
                !newExpression.endsWith(" ") && 
                newExpression != newResult && 
                !newExpression.split(" ").last().endsWith(".") &&
                newExpression != "−"
            ) {
                val eval = evaluateExpression(newExpression)
                if (eval != null && eval != "Divide by zero") {
                    eval
                } else {
                    ""
                }
            } else {
                ""
            }

            currentState.copy(
                expression = newExpression,
                result = newResult,
                realtimePreview = newRealtimePreview,
                isFreshResult = newIsFreshResult,
                history = newHistory
            )
        }
    }

    fun selectHistoryItem(item: CalculationItem) {
        _uiState.update { currentState ->
            currentState.copy(
                expression = item.expression,
                result = item.result,
                realtimePreview = "",
                isFreshResult = true
            )
        }
    }

    fun clearHistory() {
        _uiState.update { currentState ->
            currentState.copy(history = emptyList())
        }
    }

    private fun toggleLastNumberSign(expr: String): String {
        if (expr.isEmpty()) return "−"
        
        val tokens = expr.split(" ").toMutableList()
        val lastToken = tokens.lastOrNull() ?: return expr
        if (lastToken.isEmpty()) return expr
        
        if (lastToken == "+" || lastToken == "−" || lastToken == "×" || lastToken == "÷") {
            return expr
        }
        
        if (lastToken.startsWith("−")) {
            tokens[tokens.size - 1] = lastToken.substring(1)
        } else {
            tokens[tokens.size - 1] = "−$lastToken"
        }
        
        return tokens.joinToString(" ")
    }

    fun evaluateExpression(expr: String): String? {
        try {
            val prepared = expr
                .replace("×", "*")
                .replace("÷", "/")
                .replace("−", "-")
                .replace(" ", "")
                .trim()
                
            if (prepared.isEmpty()) return null
            
            val tokens = mutableListOf<String>()
            var currentNum = StringBuilder()
            
            var i = 0
            while (i < prepared.length) {
                val c = prepared[i]
                if (c.isDigit() || c == '.' || c == '%') {
                    currentNum.append(c)
                    i++
                } else if (c == '*' || c == '/' || c == '+' || c == '-') {
                    if (currentNum.isNotEmpty()) {
                        tokens.add(currentNum.toString())
                        currentNum = StringBuilder()
                    }
                    if (c == '-' && (tokens.isEmpty() || tokens.last() in listOf("+", "-", "*", "/"))) {
                        currentNum.append(c)
                    } else {
                        tokens.add(c.toString())
                    }
                    i++
                } else {
                    i++
                }
            }
            if (currentNum.isNotEmpty()) {
                tokens.add(currentNum.toString())
            }
            
            if (tokens.isEmpty()) return null
            
            if (tokens.last() in listOf("+", "-", "*", "/")) {
                tokens.removeAt(tokens.lastIndex)
            }
            
            if (tokens.isEmpty()) return null
            
            val processedTokens = mutableListOf<String>()
            for (token in tokens) {
                if (token.contains("%")) {
                    val valueStr = token.replace("%", "")
                    var numValue = valueStr.toDoubleOrNull() ?: 0.0
                    numValue /= 100.0
                    processedTokens.add(numValue.toString())
                } else {
                    processedTokens.add(token)
                }
            }
            
            val afterMultDiv = mutableListOf<String>()
            var j = 0
            while (j < processedTokens.size) {
                val token = processedTokens[j]
                if (token == "*" || token == "/") {
                    val op = token
                    if (afterMultDiv.isEmpty() || j + 1 >= processedTokens.size) {
                        return null
                    }
                    val prevVal = afterMultDiv.removeAt(afterMultDiv.lastIndex).toDoubleOrNull() ?: return null
                    val nextVal = processedTokens[j + 1].toDoubleOrNull() ?: return null
                    
                    val result = if (op == "*") {
                        prevVal * nextVal
                    } else {
                        if (nextVal == 0.0) return "Divide by zero"
                        prevVal / nextVal
                    }
                    afterMultDiv.add(result.toString())
                    j += 2
                } else {
                    afterMultDiv.add(token)
                    j++
                }
            }
            
            if (afterMultDiv.isEmpty()) return null
            var total = afterMultDiv[0].toDoubleOrNull() ?: return null
            var k = 1
            while (k < afterMultDiv.size) {
                val op = afterMultDiv[k]
                if (k + 1 >= afterMultDiv.size) return null
                val nextVal = afterMultDiv[k + 1].toDoubleOrNull() ?: return null
                if (op == "+") {
                    total += nextVal
                } else if (op == "-") {
                    total -= nextVal
                }
                k += 2
            }
            
            return formatResult(total)
        } catch (e: Exception) {
            return null
        }
    }

    private fun formatResult(value: Double): String {
        if (value.isNaN()) return "Ошибка"
        if (value.isInfinite()) return "Бесконечность"
        
        if (value % 1.0 == 0.0 && value <= Long.MAX_VALUE && value >= Long.MIN_VALUE) {
            return value.toLong().toString()
        }
        
        val str = value.toString()
        if (str.endsWith(".0")) {
            return str.dropLast(2)
        }
        
        if (str.contains("E") || str.contains("e")) {
            return str
        }
        
        return try {
            val formatted = String.format(java.util.Locale.US, "%.8f", value)
            var clean = formatted.trimEnd('0')
            if (clean.endsWith(".")) {
                clean = clean.substring(0, clean.length - 1)
            }
            clean
        } catch (e: Exception) {
            str
        }
    }
}
