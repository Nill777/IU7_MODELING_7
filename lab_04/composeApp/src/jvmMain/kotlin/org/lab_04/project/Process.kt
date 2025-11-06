package org.lab_04.project

class Process(
    private val generator: Distribution,
    private val processor: Distribution,
    private val totalTasks: Int,
    private val repeatPercentage: Int,
    private val step: Double
) {
    fun getAnswers(): Pair<Int, Int> {
        val answerEvent = eventModel(generator, processor, totalTasks, repeatPercentage)
        val answerStep = stepModel(generator, processor, totalTasks, repeatPercentage, step)
        return Pair(answerEvent, answerStep)
    }
}