package org.lab_04.project

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

enum class DistType(val title: String) {
    UNIFORM("Равномерное распределение"),
    EXPONENTIAL("Экспоненциальное распределение"),
    NORMAL("Нормальное распределение"),
    ERLANG("Распределение Эрланга")
}

@Composable
fun App() {
    var generatorType by remember { mutableStateOf(DistType.UNIFORM) }
    var processorType by remember { mutableStateOf(DistType.NORMAL) }
    
    var uniformA by remember { mutableStateOf("0.0") }
    var uniformB by remember { mutableStateOf("1.0") }
    var expLambda by remember { mutableStateOf("1.0") }
    var normalMu by remember { mutableStateOf("0.5") }
    var normalSigma by remember { mutableStateOf("0.1") }
    var erlangK by remember { mutableStateOf("2") }
    var erlangLambda by remember { mutableStateOf("1.0") }

    var percentText by remember { mutableStateOf("0") }
    var stepAnswer by remember { mutableStateOf(0) }
    var eventAnswer by remember { mutableStateOf(0) }
    var isInputValid by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val totalTasks = 1000
    val step = 0.01

    LaunchedEffect(
        generatorType, processorType, uniformA, uniformB, expLambda,
        normalMu, normalSigma, erlangK, erlangLambda, percentText
    ) {
        var currentErrorMessage = ""
        var isValid = true

        fun validateDistribution(type: DistType, source: String): String {
            return when (type) {
                DistType.UNIFORM -> {
                    val a = uniformA.toDoubleOrNull()
                    val b = uniformB.toDoubleOrNull()
                    when {
                        a == null || b == null -> "Для равномерного закона 'a' и 'b' должны быть числами"
                        a < 0 || b < 0 -> "Параметры 'a' и 'b' не могут быть отрицательными"
                        a >= b -> "Параметр 'a' должен быть строго меньше 'b'"
                        else -> ""
                    }
                }

                DistType.EXPONENTIAL -> {
                    val lambda = expLambda.toDoubleOrNull()
                    when {
                        lambda == null -> "Для экспоненциального закона 'λ' должна быть числом"
                        lambda <= 0 -> "Параметр 'λ' должен быть больше нуля"
                        else -> ""
                    }
                }

                DistType.NORMAL -> {
                    val mu = normalMu.toDoubleOrNull()
                    val sigma = normalSigma.toDoubleOrNull()
                    when {
                        mu == null || sigma == null -> "Для нормального закона 'μ' и 'σ' должны быть числами"
                        sigma <= 0 -> "Параметр 'σ' должен быть больше нуля"
                        else -> ""
                    }
                }

                DistType.ERLANG -> {
                    val k = erlangK.toIntOrNull()
                    val lambda = erlangLambda.toDoubleOrNull()
                    when {
                        k == null || lambda == null -> "Для закона Эрланга 'k' и 'λ' должны быть числами"
                        k <= 0 -> "Параметр 'k' должен быть целым числом больше нуля"
                        lambda <= 0 -> "Параметр 'λ' должен быть больше нуля"
                        else -> ""
                    }
                }
            }
        }

        currentErrorMessage = validateDistribution(generatorType, "генератора")
        if (currentErrorMessage.isEmpty()) {
            currentErrorMessage = validateDistribution(processorType, "обработчика")
        }

        val percent = percentText.toIntOrNull()
        if (currentErrorMessage.isEmpty() && (percent == null || percent !in 0..100)) {
            currentErrorMessage = "Процент возврата должен быть целым числом от 0 до 100"
        }

        if (currentErrorMessage.isNotEmpty()) {
            isValid = false
        }

        errorMessage = currentErrorMessage
        isInputValid = isValid
    }

    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Генератор заявок", style = MaterialTheme.typography.h6)
            DistributionSelector(
                selected = generatorType,
                onSelected = { generatorType = it }
            )
            Spacer(Modifier.height(8.dp))
            when (generatorType) {
                DistType.UNIFORM -> Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ParameterTextField(
                        label = "a (мин. время)",
                        value = uniformA,
                        onValueChange = { uniformA = it },
                        modifier = Modifier.weight(1f)
                    )
                    ParameterTextField(
                        label = "b (макс. время)",
                        value = uniformB,
                        onValueChange = { uniformB = it },
                        modifier = Modifier.weight(1f)
                    )
                }

                DistType.EXPONENTIAL -> ParameterTextField(
                    label = "λ (интенсивность)",
                    value = expLambda,
                    onValueChange = { expLambda = it })

                DistType.NORMAL -> Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ParameterTextField(
                        label = "μ (мат. ожидание)",
                        value = normalMu,
                        onValueChange = { normalMu = it },
                        modifier = Modifier.weight(1f)
                    )
                    ParameterTextField(
                        label = "σ (отклонение)",
                        value = normalSigma,
                        onValueChange = { normalSigma = it },
                        modifier = Modifier.weight(1f)
                    )
                }

                DistType.ERLANG -> Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ParameterTextField(
                        label = "k (порядок)",
                        value = erlangK,
                        onValueChange = { erlangK = it },
                        isIntegerOnly = true,
                        modifier = Modifier.weight(1f)
                    )
                    ParameterTextField(
                        label = "λ (интенсивность)",
                        value = erlangLambda,
                        onValueChange = { erlangLambda = it },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 10.dp))

            Text("Обработчик заявок", style = MaterialTheme.typography.h6)
            DistributionSelector(
                selected = processorType,
                onSelected = { processorType = it }
            )
            Spacer(Modifier.height(8.dp))
            when (processorType) {
                DistType.UNIFORM -> Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ParameterTextField(
                        label = "a (мин. время)",
                        value = uniformA,
                        onValueChange = { uniformA = it },
                        modifier = Modifier.weight(1f)
                    )
                    ParameterTextField(
                        label = "b (макс. время)",
                        value = uniformB,
                        onValueChange = { uniformB = it },
                        modifier = Modifier.weight(1f)
                    )
                }

                DistType.EXPONENTIAL -> ParameterTextField(
                    label = "λ (интенсивность)",
                    value = expLambda,
                    onValueChange = { expLambda = it })

                DistType.NORMAL -> Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ParameterTextField(
                        label = "μ (мат. ожидание)",
                        value = normalMu,
                        onValueChange = { normalMu = it },
                        modifier = Modifier.weight(1f)
                    )
                    ParameterTextField(
                        label = "σ (отклонение)",
                        value = normalSigma,
                        onValueChange = { normalSigma = it },
                        modifier = Modifier.weight(1f)
                    )
                }

                DistType.ERLANG -> Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ParameterTextField(
                        label = "k (порядок)",
                        value = erlangK,
                        onValueChange = { erlangK = it },
                        isIntegerOnly = true,
                        modifier = Modifier.weight(1f)
                    )
                    ParameterTextField(
                        label = "λ (интенсивность)",
                        value = erlangLambda,
                        onValueChange = { erlangLambda = it },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 10.dp))

            Text("Обратная связь", style = MaterialTheme.typography.subtitle1)
            ParameterTextField(
                value = percentText,
                onValueChange = { percentText = it },
                label = "Процент возврата заявок",
                isIntegerOnly = true
            )

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colors.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Button(
                enabled = isInputValid,
                onClick = {
                    val generatorDist = when (generatorType) {
                        DistType.UNIFORM -> Distribution.Uniform(uniformA.toDouble(), uniformB.toDouble())
                        DistType.EXPONENTIAL -> Distribution.Exponential(expLambda.toDouble())
                        DistType.NORMAL -> Distribution.Normal(normalMu.toDouble(), normalSigma.toDouble())
                        DistType.ERLANG -> Distribution.Erlang(erlangK.toInt(), erlangLambda.toDouble())
                    }
                    val processorDist = when (processorType) {
                        DistType.UNIFORM -> Distribution.Uniform(uniformA.toDouble(), uniformB.toDouble())
                        DistType.EXPONENTIAL -> Distribution.Exponential(expLambda.toDouble())
                        DistType.NORMAL -> Distribution.Normal(normalMu.toDouble(), normalSigma.toDouble())
                        DistType.ERLANG -> Distribution.Erlang(erlangK.toInt(), erlangLambda.toDouble())
                    }

                    val process = Process(
                        generator = generatorDist,
                        processor = processorDist,
                        totalTasks = totalTasks,
                        repeatPercentage = percentText.toInt(),
                        step = step
                    )
                    val (eventAns, stepAns) = process.getAnswers()
                    eventAnswer = eventAns
                    stepAnswer = stepAns
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Рассчитать")
            }

            Divider(modifier = Modifier.padding(top = 10.dp))

            Text("Результаты", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.h6)
            Column(modifier = Modifier.fillMaxWidth()) {
                Row {
                    TableCell(text = "Метод", weight = 1f, isHeader = true)
                    TableCell(text = "Длина очереди", weight = 1f, isHeader = true)
                }
                Row {
                    TableCell(text = "Событийный", weight = 1f)
                    TableCell(text = eventAnswer.toString(), weight = 1f)
                }
                Row {
                    TableCell(text = "Пошаговый (шаг=$step)", weight = 1f)
                    TableCell(text = stepAnswer.toString(), weight = 1f)
                }
            }
        }
    }
}

@Composable
fun DistributionSelector(selected: DistType, onSelected: (DistType) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .border(1.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.4f))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(selected.title, modifier = Modifier.weight(1f))
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            DistType.entries.forEach { distType ->
                DropdownMenuItem(onClick = {
                    onSelected(distType)
                    expanded = false
                }) {
                    Text(distType.title)
                }
            }
        }
    }
}

@Composable
fun ParameterTextField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit,
    isIntegerOnly: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            val regex = if (isIntegerOnly) {
                Regex("^\\d*$")
            } else {
                Regex("^-?\\d*\\.?\\d*$")
            }
            if (newValue.matches(regex)) {
                onValueChange(newValue)
            }
        },
        label = { Text(label) },
        modifier = modifier
    )
}

@Composable
fun RowScope.TableCell(
    text: String,
    weight: Float,
    isHeader: Boolean = false
) {
    Box(
        modifier = Modifier
            .border(1.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.2f))
            .weight(weight)
            .padding(2.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.align(Alignment.Center),
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
        )
    }
}
