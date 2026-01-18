package org.lab_06.project

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SimulationViewModel {
    var totalPassengers by mutableStateOf("300")
    var arrivalIntervalMin by mutableStateOf("1.0")
    var arrivalIntervalMax by mutableStateOf("3.0")

    // Веса (нормируем)
    var probWeightCitizen by mutableStateOf("60")
    var probWeightForeigner by mutableStateOf("20")
    var probWeightFamily by mutableStateOf("20")

    var familySizeMin by mutableStateOf("2")
    var familySizeMax by mutableStateOf("4")

    // паспортный контроль
    // РФ
    var timeCitizenMin by mutableStateOf("1.0")
    var timeCitizenMax by mutableStateOf("2.0")
    var rejectCitizen by mutableStateOf("0.1")

    // Иностранцы
    var timeForeignerMin by mutableStateOf("3.0")
    var timeForeignerMax by mutableStateOf("6.0")
    var rejectForeigner by mutableStateOf("5.0")

    // Семьи (всю семью сразу)
    var timeFamilyMin by mutableStateOf("4.0")
    var timeFamilyMax by mutableStateOf("8.0")
    var rejectFamily by mutableStateOf("1.0")

    // досмотр (на 1 чел)
    var timeScannerMin by mutableStateOf("1.5")
    var timeScannerMax by mutableStateOf("3.0")
    var rejectScanner by mutableStateOf("2.0")

    var simulationResult by mutableStateOf<SimulationResult?>(null)
    var isRunning by mutableStateOf(false)

    fun startSimulation(scope: CoroutineScope) {
        scope.launch {
            isRunning = true
            simulationResult = null

            val params = SimulationParams(
                totalLimit = totalPassengers.toIntOrNull() ?: 300,
                arrivalRange = (arrivalIntervalMin.toDoubleOrNull() ?: 1.0) to (arrivalIntervalMax.toDoubleOrNull()
                    ?: 3.0),

                weightCitizen = probWeightCitizen.toIntOrNull() ?: 60,
                weightForeigner = probWeightForeigner.toIntOrNull() ?: 20,
                weightFamily = probWeightFamily.toIntOrNull() ?: 20,

                familySizeRange = (familySizeMin.toIntOrNull() ?: 2) to (familySizeMax.toIntOrNull() ?: 4),

                timeCitizenRange = (timeCitizenMin.toDoubleOrNull() ?: 1.0) to (timeCitizenMax.toDoubleOrNull() ?: 2.0),
                timeForeignerRange = (timeForeignerMin.toDoubleOrNull() ?: 3.0) to (timeForeignerMax.toDoubleOrNull()
                    ?: 6.0),
                timeFamilyRange = (timeFamilyMin.toDoubleOrNull() ?: 4.0) to (timeFamilyMax.toDoubleOrNull() ?: 8.0),

                // отказы документы %
                probRejectCitizen = (rejectCitizen.toDoubleOrNull() ?: 0.1) / 100.0,
                probRejectForeigner = (rejectForeigner.toDoubleOrNull() ?: 5.0) / 100.0,
                probRejectFamily = (rejectFamily.toDoubleOrNull() ?: 1.0) / 100.0,

                // досмотр
                timeScannerPerPersonRange = (timeScannerMin.toDoubleOrNull() ?: 1.5) to (timeScannerMax.toDoubleOrNull()
                    ?: 3.0),
                probRejectScanner = (rejectScanner.toDoubleOrNull() ?: 2.0) / 100.0
            )

            val result = withContext(Dispatchers.Default) {
                ComplexAirportSimulation().run(params)
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
    val scrollState = rememberScrollState()

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Аэропорт", fontSize = 20.sp, fontWeight = FontWeight.Bold)

                Row {
                    Column {
                        Text("Параметры генерации", fontWeight = FontWeight.Bold)

                        SectionTitle("Необходимое кол-во заявок")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ParameterTextField(
                                "Штук",
                                viewModel.totalPassengers,
                                { viewModel.totalPassengers = it },
                                true
                            )
                        }

                        SectionTitle("Время прихода пассажиров")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ParameterTextField(
                                "Интервал От",
                                viewModel.arrivalIntervalMin,
                                { viewModel.arrivalIntervalMin = it })
                            ParameterTextField(
                                "Интервал До",
                                viewModel.arrivalIntervalMax,
                                { viewModel.arrivalIntervalMax = it })
                        }

                        SectionTitle("Веса генерации типов")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ParameterTextField(
                                "РФ",
                                viewModel.probWeightCitizen,
                                { viewModel.probWeightCitizen = it },
                                true
                            )
                            ParameterTextField(
                                "Инстр",
                                viewModel.probWeightForeigner,
                                { viewModel.probWeightForeigner = it },
                                true
                            )
                            ParameterTextField(
                                "Семья",
                                viewModel.probWeightFamily,
                                { viewModel.probWeightFamily = it },
                                true
                            )
                        }

                        SectionTitle("Размер семьи (чел)")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ParameterTextField("От", viewModel.familySizeMin, { viewModel.familySizeMin = it }, true)
                            ParameterTextField("До", viewModel.familySizeMax, { viewModel.familySizeMax = it }, true)
                        }
                    }

                    Spacer(Modifier.width(8.dp))

                    Column {
                        Text("Паспортный контроль (Время / Отказ %)", fontWeight = FontWeight.Bold)

                        SectionTitle("Граждане РФ (2 офицера)")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ParameterTextField("Мин", viewModel.timeCitizenMin, { viewModel.timeCitizenMin = it })
                            ParameterTextField("Макс", viewModel.timeCitizenMax, { viewModel.timeCitizenMax = it })
                            ParameterTextField("Отказ %", viewModel.rejectCitizen, { viewModel.rejectCitizen = it })
                        }

                        SectionTitle("Иностранцы (1 офицер)")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ParameterTextField("Мин", viewModel.timeForeignerMin, { viewModel.timeForeignerMin = it })
                            ParameterTextField("Макс", viewModel.timeForeignerMax, { viewModel.timeForeignerMax = it })
                            ParameterTextField("Отказ %", viewModel.rejectForeigner, { viewModel.rejectForeigner = it })
                        }

                        SectionTitle("Семьи (1 офицер)")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ParameterTextField("Мин", viewModel.timeFamilyMin, { viewModel.timeFamilyMin = it })
                            ParameterTextField("Макс", viewModel.timeFamilyMax, { viewModel.timeFamilyMax = it })
                            ParameterTextField("Отказ %", viewModel.rejectFamily, { viewModel.rejectFamily = it })
                        }

                        Text("Досмотр (сканеры 3 шт)", fontWeight = FontWeight.Bold)

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ParameterTextField(
                                "Время 1 чел От",
                                viewModel.timeScannerMin,
                                { viewModel.timeScannerMin = it })
                            ParameterTextField(
                                "Время 1 чел До",
                                viewModel.timeScannerMax,
                                { viewModel.timeScannerMax = it })
                            ParameterTextField("Отказ %", viewModel.rejectScanner, { viewModel.rejectScanner = it })
                        }
                    }
                }

                Button(
                    onClick = { viewModel.startSimulation(coroutineScope) },
                    enabled = !viewModel.isRunning,
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    if (viewModel.isRunning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Начать моделирование")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

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
fun ResultCard(res: SimulationResult) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Результаты", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            Text("Обработано заявок: ${res.totalProcessedGroups}")
            Text("Всего людей: ${res.totalPeople}")
            Text("Успешно вылетели: ${res.successPeople} чел.")
            Spacer(Modifier.height(4.dp))
            Text(
                "Отказы (Документы): ${res.rejectedPassportPeople} чел. (${res.rejectedPassportGroups} заявок)",
                color = MaterialTheme.colorScheme.error
            )
            Text(
                "Отказы (Сканер): ${res.rejectedScannerPeople} чел. (${res.rejectedScannerGroups} заявок)",
                color = MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.height(4.dp))
            Text("Макс. очередь РФ: ${res.maxQueueRF}")
            Text("Макс. очередь Иностр: ${res.maxQueueForeign}")
            Text("Макс. очередь Семьи: ${res.maxQueueFamily}")
            Text("Макс. очередь Сканер: ${res.maxQueueScanner}")
        }
    }
}
