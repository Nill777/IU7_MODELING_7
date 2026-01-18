package org.lab_05.project

import kotlin.random.Random

data class SimulationParams(
    val requestsToProcess: Int = 300,
    val clientArrivalMin: Double = 8.0,
    val clientArrivalMax: Double = 12.0,
    val operator1Min: Double = 15.0,
    val operator1Max: Double = 25.0,
    val operator2Min: Double = 30.0,
    val operator2Max: Double = 50.0,
    val operator3Min: Double = 20.0,
    val operator3Max: Double = 60.0,
    val computer1Time: Double = 15.0,
    val computer2Time: Double = 30.0
)

data class SimulationResult(
    val totalRequests: Int,
    val rejectedRequests: Int
) {
    val rejectionProbability: Double = if (totalRequests > 0) {
        rejectedRequests.toDouble() / totalRequests
    } else {
        0.0
    }
}

class InformationCenterSimulation {

    // Внутренние классы для представления состояния сущностей
    private class Operator(val id: Int) {
        var isBusy = false
        var freeAtTime: Double = 0.0
    }

    private class Computer(val id: Int) {
        var isBusy = false
        var freeAtTime: Double = 0.0
    }

    // Типы событий в модели
    private enum class EventType {
        CLIENT_ARRIVAL,
        OPERATOR_FINISHED,
        COMPUTER_FINISHED
    }

    // события, которые будут храниться в очереди
    // Comparable позволяет автоматически сортировать события по времени
    private data class Event(
        val time: Double,
        val type: EventType,
        val entityId: Int? = null // ID оператора или компьютера
    ) : Comparable<Event> {
        override fun compareTo(other: Event) = time.compareTo(other.time)
    }

    private fun uniform(min: Double, max: Double): Double {
        return Random.nextDouble(min,  max)
    }

    fun run(params: SimulationParams): SimulationResult {
        // Очередь событий
        val events = java.util.PriorityQueue<Event>()

        val operators = listOf(Operator(0), Operator(1), Operator(2))
        val computers = listOf(Computer(0), Computer(1))

        // Накопители (очереди к компьютерам)
        val accumulatorToComputer1 = mutableListOf<Double>()
        val accumulatorToComputer2 = mutableListOf<Double>()

        var currentTime = 0.0
        var generatedRequests = 0
        var processedRequests = 0
        var rejectedRequests = 0

        // первое событие - приход клиента
        events.add(Event(uniform(params.clientArrivalMin, params.clientArrivalMax), EventType.CLIENT_ARRIVAL))
        generatedRequests++


        // Работаем, пока не обработаем нужное количество заявок
        while (processedRequests + rejectedRequests < params.requestsToProcess) {
            // Извлекаем ближайшее по времени событие
            val event = events.poll() ?: break // Если очередь пуста, выходим
            currentTime = event.time

            when (event.type) {
                // Пришел новый клиент
                EventType.CLIENT_ARRIVAL -> {
                    // Планируем приход следующего клиента, если не достигли лимита
                    if (generatedRequests < params.requestsToProcess) {
                        val nextArrivalTime = currentTime + uniform(params.clientArrivalMin, params.clientArrivalMax)
                        events.add(Event(nextArrivalTime, EventType.CLIENT_ARRIVAL))
                        generatedRequests++
                    }

                    // Ищем свободного оператора
                    val freeOperator = operators.find { !it.isBusy }

                    if (freeOperator != null) {
                        // оператор найден, занимаем его
                        freeOperator.isBusy = true
                        val processingTime = when (freeOperator.id) {
                            0 -> uniform(params.operator1Min, params.operator1Max)
                            1 -> uniform(params.operator2Min, params.operator2Max)
                            else -> uniform(params.operator3Min, params.operator3Max)
                        }
                        freeOperator.freeAtTime = currentTime + processingTime
                        // Планируем событие освобождения оператора
                        events.add(Event(freeOperator.freeAtTime, EventType.OPERATOR_FINISHED, freeOperator.id))
                    } else {
                        // Все операторы заняты - отказываем
                        rejectedRequests++
                    }
                }

                // Оператор закончил обработку заявки
                EventType.OPERATOR_FINISHED -> {
                    val operatorId = event.entityId!!
                    operators[operatorId].isBusy = false

                    // Отправляем заявку в нужный накопитель (очередь к компьютеру)
                    if (operatorId == 0 || operatorId == 1) {
                        accumulatorToComputer1.add(currentTime)
                    } else {
                        accumulatorToComputer2.add(currentTime)
                    }
                }

                // Компьютер закончил обработку заявки
                EventType.COMPUTER_FINISHED -> {
                    val computerId = event.entityId!!
                    computers[computerId].isBusy = false
                    processedRequests++
                }
            }

            // Проверка и запуск компьютеров после каждого события
            if (!computers[0].isBusy && accumulatorToComputer1.isNotEmpty()) {
                computers[0].isBusy = true
                accumulatorToComputer1.removeFirst() // Забираем заявку из очереди
                val finishTime = currentTime + params.computer1Time
                computers[0].freeAtTime = finishTime
                events.add(Event(finishTime, EventType.COMPUTER_FINISHED, 0))
            }

            if (!computers[1].isBusy && accumulatorToComputer2.isNotEmpty()) {
                computers[1].isBusy = true
                accumulatorToComputer2.removeFirst() // Забираем заявку из очереди
                val finishTime = currentTime + params.computer2Time
                computers[1].freeAtTime = finishTime
                events.add(Event(finishTime, EventType.COMPUTER_FINISHED, 1))
            }
        }

        return SimulationResult(
            totalRequests = generatedRequests,
            rejectedRequests = rejectedRequests
        )
    }
}
