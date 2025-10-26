package org.lab_02.project

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.util.LinkedList
import java.util.Queue

private fun isValidIntensity(text: String): Boolean {
    if (text.isBlank()) return true
    return text.replace(',', '.').toDoubleOrNull()?.takeIf { it > 0 } != null
}

/**
 * Фильтрация ввода
 */
private fun canBeTypedAsNonNegativeDouble(text: String): Boolean {
    if (text.isEmpty()) return true
    return text.matches(Regex("^\\d*([.,]?\\d*)$"))
}

/**
 * Является ли граф состояний сильно связным
 */
private fun isGraphStronglyConnected(matrix: List<List<String>>, size: Int): Boolean {
    if (size <= 1) return true

    val adj = List(size) { mutableListOf<Int>() }   //  реальный
    val reversedAdj = List(size) { mutableListOf<Int>() }   // зеркальный

    for (i in 0 until size) {
        for (j in 0 until size) {
            if (i == j) continue
            // ребра
            if ((matrix[i][j].replace(',', '.').toDoubleOrNull() ?: 0.0) > 0.0) {
                adj[i].add(j)
                reversedAdj[j].add(i)
            }
        }
    }

    // обход в ширину
    fun bfs(startNode: Int, adjacencyList: List<List<Int>>): Set<Int> {
        val visited = mutableSetOf<Int>()
        val queue: Queue<Int> = LinkedList()

        visited.add(startNode)
        queue.add(startNode)

        while (queue.isNotEmpty()) {
            val node = queue.poll()
            adjacencyList[node].forEach { neighbor ->
                if (neighbor !in visited) {
                    visited.add(neighbor)
                    queue.add(neighbor)
                }
            }
        }
        return visited
    }

    // из 0-й вершины в любую X на реальном
    if (bfs(0, adj).size != size) return false
    // из 0-й вершины в любую X на зеркальном эквивалентно реальном из X в 0-ю
    if (bfs(0, reversedAdj).size != size) return false
    return true
}

private fun createInitialMatrix(size: Int): List<MutableList<String>> {
    return List(size) { i -> MutableList(size) { j -> if (i == j) "0.0" else "" } }
}

private fun createBooleanMatrix(size: Int): List<MutableList<Boolean>> {
    return List(size) { MutableList(size) { false } }
}

@Composable
@Preview
fun App() {
    var matrixSize by remember { mutableStateOf(3) }
    var matrixSizeText by remember { mutableStateOf(matrixSize.toString()) }
    var isSizeError by remember { mutableStateOf(false) }

    var matrixTextValues by remember { mutableStateOf(createInitialMatrix(matrixSize)) }
    var matrixInputErrors by remember { mutableStateOf(createBooleanMatrix(matrixSize)) }
    var connectivityError by remember { mutableStateOf<String?>(null) }

    var calculationResult by remember { mutableStateOf<CalculationResult?>(null) }
    var calculationErrorMessage by remember { mutableStateOf<String?>(null) }

    val hasInputErrors = matrixInputErrors.flatten().any { it }

    // проверка графа при каждом изменении матрицы
    LaunchedEffect(matrixTextValues) {
        connectivityError = if (!isGraphStronglyConnected(matrixTextValues, matrixSize)) {
            "Ошибка: граф не является сильно связным"
        } else {
            null
        }
    }

    fun changeMatrixSize(newSize: Int) {
        matrixSize = newSize
        matrixTextValues = createInitialMatrix(newSize)
        matrixInputErrors = createBooleanMatrix(newSize)
        calculationResult = null
        calculationErrorMessage = null
    }

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = matrixSizeText,
                onValueChange = { newText ->
                    if (newText.all { it.isDigit() } && newText.length <= 2) {
                        matrixSizeText = newText
                        val newSize = newText.toIntOrNull()
                        if (newSize != null && newSize in 2..10) {
                            isSizeError = false
                            changeMatrixSize(newSize)
                        } else {
                            isSizeError = true
                        }
                    }
                },
                label = { Text("Количество состояний") },
                supportingText = { if (isSizeError) Text("Введите число от 2 до 10") },
                isError = isSizeError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.width(220.dp)
            )

            Text(
                "Матрица интенсивностей переходов состояний (λ)",
                style = MaterialTheme.typography.titleMedium
            )
            Column(
                modifier = Modifier.offset(x = (-22).dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.width(40.dp))
                    for (j in 0 until matrixSize) {
                        Text("S${j + 1}", Modifier.width(80.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
                    }
                }

                for (i in 0 until matrixSize) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "S${i + 1}",
                            Modifier.width(40.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall
                        )
                        for (j in 0 until matrixSize) {
                            val isDiagonal = (i == j)
                            val isValidAndFilled = matrixTextValues[i][j].isNotBlank() && !matrixInputErrors[i][j]
                            OutlinedTextField(
                                value = matrixTextValues[i][j],
                                onValueChange = { newText ->
                                    if (canBeTypedAsNonNegativeDouble(newText)) {
                                        val newValues = matrixTextValues.map { it.toMutableList() }.toMutableList()
                                        val newErrors = matrixInputErrors.map { it.toMutableList() }.toMutableList()
                                        newValues[i][j] = newText
                                        newErrors[i][j] = !isValidIntensity(newText)
                                        matrixTextValues = newValues
                                        matrixInputErrors = newErrors
                                    }
                                },
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(56.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                enabled = !isDiagonal,
                                isError = matrixInputErrors[i][j],
                                singleLine = true, // Запрещаем перенос строки и рост ячейки
                                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                                colors = when {
                                    isDiagonal -> TextFieldDefaults.colors(
                                        disabledContainerColor = Color(0xFFE0E0E0),
                                        disabledTextColor = Color(0xFF616161)
                                    )
                                    isValidAndFilled -> TextFieldDefaults.colors(
                                        focusedTextColor = Color.Black,
                                        unfocusedTextColor = Color.Black,
                                        focusedIndicatorColor = Color(0xFF4CAF50),
                                        unfocusedIndicatorColor = Color(0xFF4CAF50),
                                        focusedContainerColor = Color(0xFFFFFFFF),
                                        unfocusedContainerColor = Color(0xFF94E098)
                                    )
                                    else -> TextFieldDefaults.colors(
                                        focusedTextColor = Color.Black,
                                        unfocusedTextColor = Color.Black,
                                        focusedContainerColor = Color(0xFFFFFFFF),
                                        unfocusedContainerColor = Color(0xFFFFFFFF),
                                        errorIndicatorColor = Color(0xFFAF0000),
                                        errorContainerColor = Color(0xFFEE9DA3)
                                    )
                                }
                            )
                        }
                    }
                }
            }

            if (connectivityError != null) {
                Text(
                    text = connectivityError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }

            Button(
                onClick = {
                    val intensityMatrix = matrixTextValues.map { row ->
                        row.map { (it.replace(',', '.').toDoubleOrNull() ?: 0.0) }
                    }
                    calculationResult = MarkovChainSolver.solve(intensityMatrix)
                    calculationErrorMessage = if (calculationResult == null) "Не удалось выполнить расчет. Проверьте матрицу." else null
                },
                enabled = !isSizeError && !hasInputErrors && connectivityError == null
            ) {
                Text("Решить")
            }

            if (calculationErrorMessage != null) {
                Text(calculationErrorMessage!!, color = MaterialTheme.colorScheme.error)
            }

            calculationResult?.let { result ->
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Предельные вероятности:",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        result.probabilities.forEachIndexed { index, p ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "p${index + 1}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Box(
                                    modifier = Modifier
                                        .size(width = 80.dp, height = 56.dp)
                                        .clip(MaterialTheme.shapes.extraSmall)
                                        .background(Color(0xFFFFFFFF))
                                        .border(
                                            1.dp,
                                            Color.Gray,
                                            MaterialTheme.shapes.extraSmall
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = String.format("%.6f", p),
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Среднее время пребывания в состояниях:", style = MaterialTheme.typography.titleMedium)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        result.times.forEachIndexed { index, t ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "t${index + 1}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Box(
                                    modifier = Modifier
                                        .size(width = 80.dp, height = 56.dp)
                                        .clip(MaterialTheme.shapes.extraSmall)
                                        .background(Color(0xFFFFFFFF))
                                        .border(
                                            1.dp,
                                            Color.Gray, 
                                            MaterialTheme.shapes.extraSmall
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = String.format("%.6f", t),
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}