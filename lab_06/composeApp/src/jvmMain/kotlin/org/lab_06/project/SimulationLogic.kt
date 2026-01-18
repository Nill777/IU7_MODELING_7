package org.lab_06.project

import java.util.PriorityQueue
import kotlin.random.Random

enum class ClientType {
    CITIZEN_RF,
    FOREIGNER,
    FAMILY
}

data class Client(
    val type: ClientType,
    val size: Int, // кол-во людей в заявке
    val arrivalTime: Double
)

data class SimulationParams(
    val totalLimit: Int, // лимит количества заявок
    val arrivalRange: Pair<Double, Double>,

    val weightCitizen: Int,
    val weightForeigner: Int,
    val weightFamily: Int,

    val familySizeRange: Pair<Int, Int>,

    val timeCitizenRange: Pair<Double, Double>,
    val timeForeignerRange: Pair<Double, Double>,
    val timeFamilyRange: Pair<Double, Double>, // время обслуживания всей семьи на документы

    val probRejectCitizen: Double,
    val probRejectForeigner: Double,
    val probRejectFamily: Double,

    val timeScannerPerPersonRange: Pair<Double, Double>,
    val probRejectScanner: Double
)

data class SimulationResult(
    val totalProcessedGroups: Int,
    val totalPeople: Int,
    val successPeople: Int,

    val rejectedPassportGroups: Int,
    val rejectedPassportPeople: Int,

    val rejectedScannerGroups: Int,
    val rejectedScannerPeople: Int,

    val maxQueueRF: Int,
    val maxQueueForeign: Int,
    val maxQueueFamily: Int,
    val maxQueueScanner: Int
)

class ComplexAirportSimulation {
    private enum class EventType {
        ARRIVAL,
        PASSPORT_DONE,
        SCANNER_DONE
    }

    private data class Event(
        val time: Double,
        val type: EventType,
        val client: Client? = null,
        val serverId: Int? = null
    ) : Comparable<Event> {
        override fun compareTo(other: Event) = time.compareTo(other.time)
    }

    // офицер или сканер
    private class Server(val id: Int) {
        var isBusy = false
    }

    private fun uniform(range: Pair<Double, Double>) = Random.nextDouble(range.first, range.second)
    private fun uniformInt(range: Pair<Int, Int>) = Random.nextInt(range.first, range.second + 1)

    fun run(params: SimulationParams): SimulationResult {
        val events = PriorityQueue<Event>()
        var currentTime = 0.0

        // ресурсы
        // id 0,1 -> РФ
        // id 2 -> Иностранцы
        // id 3 -> Семьи
        val officersRF = listOf(Server(0), Server(1))
        val officerForeign = listOf(Server(2))
        val officerFamily = listOf(Server(3))

        // id 10,11,12 -> Сканеры
        val scanners = listOf(Server(10), Server(11), Server(12))

        // очереди
        val queueRF = ArrayDeque<Client>()
        val queueForeign = ArrayDeque<Client>()
        val queueFamily = ArrayDeque<Client>()
        val queueScanner = ArrayDeque<Client>()

        // статистика
        var generatedGroups = 0
        var processedGroups = 0 // всего, неважно успех или отказ

        var totalPeople = 0
        var successPeople = 0

        var rejPassGroups = 0
        var rejPassPeople = 0
        var rejScanGroups = 0
        var rejScanPeople = 0

        var maxQ_RF = 0
        var maxQ_For = 0
        var maxQ_Fam = 0
        var maxQ_Scan = 0

        // первая заявка
        events.add(Event(uniform(params.arrivalRange), EventType.ARRIVAL))
        generatedGroups++


        while (processedGroups < params.totalLimit) {
            val event = events.poll() ?: break
            currentTime = event.time

            when (event.type) {
                EventType.ARRIVAL -> {
                    // планируем следующего, если лимит генерации не исчерпан
                    if (generatedGroups < params.totalLimit) {
                        events.add(Event(currentTime + uniform(params.arrivalRange), EventType.ARRIVAL))
                        generatedGroups++
                    }

                    // определяем тип
                    val totalWeight = params.weightCitizen + params.weightForeigner + params.weightFamily
                    val rnd = Random.nextInt(totalWeight)

                    val newClient: Client
                    if (rnd < params.weightCitizen) {
                        newClient = Client(ClientType.CITIZEN_RF, 1, currentTime)
                    } else if (rnd < params.weightCitizen + params.weightForeigner) {
                        newClient = Client(ClientType.FOREIGNER, 1, currentTime)
                    } else {
                        val size = uniformInt(params.familySizeRange)
                        newClient = Client(ClientType.FAMILY, size, currentTime)
                    }
                    totalPeople += newClient.size

                    // распределяем по очередям
                    when (newClient.type) {
                        ClientType.CITIZEN_RF -> {
                            val freeOp = officersRF.find { !it.isBusy }
                            if (freeOp != null) {
                                freeOp.isBusy = true
                                val duration = uniform(params.timeCitizenRange)
                                events.add(Event(currentTime + duration, EventType.PASSPORT_DONE, newClient, freeOp.id))
                            } else {
                                queueRF.add(newClient)
                                if (queueRF.size > maxQ_RF) maxQ_RF = queueRF.size
                            }
                        }
                        ClientType.FOREIGNER -> {
                            val freeOp = officerForeign.find { !it.isBusy }
                            if (freeOp != null) {
                                freeOp.isBusy = true
                                val duration = uniform(params.timeForeignerRange)
                                events.add(Event(currentTime + duration, EventType.PASSPORT_DONE, newClient, freeOp.id))
                            } else {
                                queueForeign.add(newClient)
                                if (queueForeign.size > maxQ_For) maxQ_For = queueForeign.size
                            }
                        }
                        ClientType.FAMILY -> {
                            val freeOp = officerFamily.find { !it.isBusy }
                            if (freeOp != null) {
                                freeOp.isBusy = true
                                val duration = uniform(params.timeFamilyRange) // время на всю семью сразу
                                events.add(Event(currentTime + duration, EventType.PASSPORT_DONE, newClient, freeOp.id))
                            } else {
                                queueFamily.add(newClient)
                                if (queueFamily.size > maxQ_Fam) maxQ_Fam = queueFamily.size
                            }
                        }
                    }
                }

                EventType.PASSPORT_DONE -> {
                    val client = event.client!!
                    val sId = event.serverId!!

                    // освобождаем офицера и берем следующего из его очереди
                    if (sId in 0..1) { // РФ
                        val officer = officersRF.find { it.id == sId }!!
                        officer.isBusy = false
                        if (queueRF.isNotEmpty()) {
                            officer.isBusy = true
                            val nextC = queueRF.removeFirst()
                            events.add(Event(currentTime + uniform(params.timeCitizenRange), EventType.PASSPORT_DONE, nextC, sId))
                        }
                    } else if (sId == 2) { // Иностранцы
                        officerForeign[0].isBusy = false
                        if (queueForeign.isNotEmpty()) {
                            officerForeign[0].isBusy = true
                            val nextC = queueForeign.removeFirst()
                            events.add(Event(currentTime + uniform(params.timeForeignerRange), EventType.PASSPORT_DONE, nextC, sId))
                        }
                    } else { // Семья
                        officerFamily[0].isBusy = false
                        if (queueFamily.isNotEmpty()) {
                            officerFamily[0].isBusy = true
                            val nextC = queueFamily.removeFirst()
                            events.add(Event(currentTime + uniform(params.timeFamilyRange), EventType.PASSPORT_DONE, nextC, sId))
                        }
                    }

                    // отказ по документам
                    val rejectProb = when(client.type) {
                        ClientType.CITIZEN_RF -> params.probRejectCitizen
                        ClientType.FOREIGNER -> params.probRejectForeigner
                        ClientType.FAMILY -> params.probRejectFamily
                    }

                    if (Random.nextDouble() < rejectProb) {
                        // отказ всей заявке
                        rejPassGroups++
                        rejPassPeople += client.size
                        processedGroups++ // заявка покинула систему
                    } else {
                        // в очередь к сканерам
                        val freeScanner = scanners.find { !it.isBusy }
                        if (freeScanner != null) {
                            freeScanner.isBusy = true
                            // время сканирования = сумма всех
                            var totalScanTime = 0.0
                            repeat(client.size) {
                                totalScanTime += uniform(params.timeScannerPerPersonRange)
                            }

                            events.add(Event(currentTime + totalScanTime, EventType.SCANNER_DONE, client, freeScanner.id))
                        } else {
                            queueScanner.add(client)
                            if (queueScanner.size > maxQ_Scan) maxQ_Scan = queueScanner.size
                        }
                    }
                }

                EventType.SCANNER_DONE -> {
                    val client = event.client!!
                    val sId = event.serverId!!

                    // освобождаем сканер и берем следующего
                    scanners.find { it.id == sId }!!.isBusy = false
                    if (queueScanner.isNotEmpty()) {
                        val nextC = queueScanner.removeFirst()
                        val scanner = scanners.find { it.id == sId }!!
                        scanner.isBusy = true

                        var totalScanTime = 0.0
                        repeat(nextC.size) {
                            totalScanTime += uniform(params.timeScannerPerPersonRange)
                        }
                        events.add(Event(currentTime + totalScanTime, EventType.SCANNER_DONE, nextC, sId))
                    }

                    // отказ по контрабанде
                    if (Random.nextDouble() < params.probRejectScanner) {
                        rejScanGroups++
                        rejScanPeople += client.size
                    } else {
                        successPeople += client.size
                    }
                    processedGroups++ // заявка обработана полностью
                }
            }
        }

        return SimulationResult(
            totalProcessedGroups = processedGroups,
            totalPeople = totalPeople,
            successPeople = successPeople,
            rejectedPassportGroups = rejPassGroups,
            rejectedPassportPeople = rejPassPeople,
            rejectedScannerGroups = rejScanGroups,
            rejectedScannerPeople = rejScanPeople,
            maxQueueRF = maxQ_RF,
            maxQueueForeign = maxQ_For,
            maxQueueFamily = maxQ_Fam,
            maxQueueScanner = maxQ_Scan
        )
    }
}
