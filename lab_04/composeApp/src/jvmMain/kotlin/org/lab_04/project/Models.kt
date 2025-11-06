package org.lab_04.project

import kotlin.random.Random

private data class Event(val time: Double, val type: String)

fun eventModel(
    generator: Distribution,
    processor: Distribution,
    totalTasks: Int,
    repeatPercentage: Int
): Int {
    var processedTasks = 0
    var curQueueLen = 0
    var maxQueueLen = 0
    val events = mutableListOf(Event(generate(generator), "g"))
    var isFree = true
    var processFlag = false

    while (processedTasks < totalTasks) {
        val event = events.removeFirst()

        when (event.type) {
            "g" -> {
                curQueueLen++
                if (curQueueLen > maxQueueLen) {
                    maxQueueLen = curQueueLen
                }
                addEvent(events, Event(event.time + generate(generator), "g"))
                if (isFree) {
                    processFlag = true
                }
            }
            "p" -> {
                processedTasks++
                if (Random.nextInt(1, 101) <= repeatPercentage) {
                    curQueueLen++
                }
                processFlag = true
            }
        }

        if (processFlag) {
            if (curQueueLen > 0) {
                curQueueLen--
                addEvent(events, Event(event.time + generate(processor), "p"))
                isFree = false
            } else {
                isFree = true
            }
            processFlag = false
        }
    }
    return maxQueueLen
}

private fun addEvent(events: MutableList<Event>, newEvent: Event) {
    val index = events.binarySearch { it.time.compareTo(newEvent.time) }
    if (index < 0) {
        events.add(-index - 1, newEvent)
    } else {
        events.add(index, newEvent)
    }
}

fun stepModel(
    generator: Distribution,
    processor: Distribution,
    totalTasks: Int,
    repeatPercentage: Int,
    step: Double
): Int {
    var processedTasks = 0
    var tCurr = 0.0
    var tGen = generate(generator)
    var tProc = 0.0
    var maxQueueLen = 0

    val queue = ArrayDeque<Double>()

    while (processedTasks < totalTasks) {
        while (tCurr >= tGen) {
            queue.add(tGen)
            if (queue.size > maxQueueLen) {
                maxQueueLen = queue.size
            }
            tGen += generate(generator)
        }

        if (tCurr >= tProc && queue.isNotEmpty()) {
            processedTasks++
            if (Random.nextInt(1, 101) <= repeatPercentage) {
                queue.add(tProc)
            }

            queue.removeFirst()
            tProc = tCurr + generate(processor)
        }

        tCurr += step
    }
    return maxQueueLen
}