package org.lab_05.project

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SimulationViewModel {
    var requestsToProcess by mutableStateOf("300")
    var clientArrivalMin by mutableStateOf("8")
    var clientArrivalMax by mutableStateOf("12")
    var operator1Min by mutableStateOf("15")
    var operator1Max by mutableStateOf("25")
    var operator2Min by mutableStateOf("30")
    var operator2Max by mutableStateOf("50")
    var operator3Min by mutableStateOf("20")
    var operator3Max by mutableStateOf("60")
    var computer1Time by mutableStateOf("15")
    var computer2Time by mutableStateOf("30")

    var simulationResult by mutableStateOf<SimulationResult?>(null)
    var isRunning by mutableStateOf(false)

    fun startSimulation(scope: CoroutineScope) {
        scope.launch {
            isRunning = true
            simulationResult = null

            val params = SimulationParams(
                requestsToProcess = requestsToProcess.toIntOrNull() ?: 300,
                clientArrivalMin = clientArrivalMin.toDoubleOrNull() ?: 8.0,
                clientArrivalMax = clientArrivalMax.toDoubleOrNull() ?: 12.0,
                operator1Min = operator1Min.toDoubleOrNull() ?: 15.0,
                operator1Max = operator1Max.toDoubleOrNull() ?: 25.0,
                operator2Min = operator2Min.toDoubleOrNull() ?: 30.0,
                operator2Max = operator2Max.toDoubleOrNull() ?: 50.0,
                operator3Min = operator3Min.toDoubleOrNull() ?: 20.0,
                operator3Max = operator3Max.toDoubleOrNull() ?: 60.0,
                computer1Time = computer1Time.toDoubleOrNull() ?: 15.0,
                computer2Time = computer2Time.toDoubleOrNull() ?: 30.0
            )

            val result = withContext(Dispatchers.Default) {
                InformationCenterSimulation().run(params)
            }

            simulationResult = result
            isRunning = false
        }
    }
}

@Composable
fun App() {
    val viewModel = remember { SimulationViewModel() }
    val coroutineScope = rememberCoroutineScope()

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Параметры моделирования", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Row {
                    Column {
                        SectionTitle("Необходимое кол-во заявок")
                        ParameterTextField(
                            "Штук",
                            viewModel.requestsToProcess,
                            { viewModel.requestsToProcess = it },
                            isIntegerOnly = true
                        )

                        SectionTitle("Время прихода клиентов(мин)")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ParameterTextField(
                                "От",
                                viewModel.clientArrivalMin,
                                { viewModel.clientArrivalMin = it })
                            ParameterTextField(
                                "До",
                                viewModel.clientArrivalMax,
                                { viewModel.clientArrivalMax = it })
                        }

                        SectionTitle("Время обработки(мин)")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ParameterTextField(
                                "Компьютер 1",
                                viewModel.computer1Time,
                                { viewModel.computer1Time = it })
                            ParameterTextField(
                                "Компьютер 2",
                                viewModel.computer2Time,
                                { viewModel.computer2Time = it })
                        }
                    }

                    Spacer(Modifier.width(8.dp))

                    Column {
                        SectionTitle("Время обработки(мин): Оператор 1")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ParameterTextField("От", viewModel.operator1Min, { viewModel.operator1Min = it })
                            ParameterTextField("До", viewModel.operator1Max, { viewModel.operator1Max = it })
                        }

                        SectionTitle("Время обработки(мин): Оператор 2")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ParameterTextField("От", viewModel.operator2Min, { viewModel.operator2Min = it })
                            ParameterTextField("До", viewModel.operator2Max, { viewModel.operator2Max = it })
                        }

                        SectionTitle("Время обработки(мин): Оператор 3")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ParameterTextField("От", viewModel.operator3Min, { viewModel.operator3Min = it })
                            ParameterTextField("До", viewModel.operator3Max, { viewModel.operator3Max = it })
                        }
                    }
                }

                Button(
                    onClick = { viewModel.startSimulation(coroutineScope) },
                    enabled = !viewModel.isRunning,
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    if (viewModel.isRunning) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Начать моделирование")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                // Отображение результатов
                viewModel.simulationResult?.let { result ->
                    ResultCard(result)
                }
            }
        }
    }
}

@Composable
fun ParameterTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isIntegerOnly: Boolean = false,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            val regex = if (isIntegerOnly) {
                Regex("^\\d*$")
            } else {
                Regex("^\\d*\\.?\\d*$")
            }
            if (newValue.matches(regex)) {
                onValueChange(newValue)
            }
        },
        label = { Text(label) },
        modifier = modifier.width(150.dp),
        singleLine = true
    )
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
fun ResultCard(result: SimulationResult) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Результаты моделирования", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Успешно обработано заявок: ${result.totalRequests}", fontSize = 16.sp)
            Text("Отклонено заявок: ${result.rejectedRequests}", fontSize = 16.sp)
            Text(
                text = "Вероятность отказа: %.2f".format(result.rejectionProbability * 100) + " %",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
