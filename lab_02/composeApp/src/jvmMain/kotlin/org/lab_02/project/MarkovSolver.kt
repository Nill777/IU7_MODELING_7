package org.lab_02.project

import org.apache.commons.math3.linear.Array2DRowRealMatrix
import org.apache.commons.math3.linear.LUDecomposition

data class CalculationResult(
    val probabilities: List<Double>,
    val times: List<Double>
)

object MarkovChainSolver {
    fun solve(intensityMatrix: List<List<Double>>): CalculationResult? {
        try {
            val matrixSize = intensityMatrix.size
            if (matrixSize == 0) return null
            // матрица коэффициентов для системы уравнений coefficientsArray * вектор вероятностей = constants(вектор своб. членов)
            val coefficientsArray = Array(matrixSize) { DoubleArray(matrixSize) }

            // транспонируем исходную матрицу интенсивностей
            // (тк все интенсивности, ведущие в состояние j, находятся в j-ом столбце)
            for (i in 0 until matrixSize) {
                for (j in 0 until matrixSize) {
                    coefficientsArray[i][j] = intensityMatrix[j][i]
                }
            }

            // вычисляем диагональные элементы: λ_ii = -∑(λ_ij) для оттока из каждого состояния
            val rowSums = intensityMatrix.map { row -> row.sum() }
            for (i in 0 until matrixSize) {
                coefficientsArray[i][i] = -rowSums[i]
            }

            // СЛАУ линейно зависима - бесконечное мн-во решений
            // заменяем первую строку на уравнение нормировки: ∑p_i = 1
            for (j in 0 until matrixSize) {
                coefficientsArray[0][j] = 1.0
            }
            // 1 для уравнения нормировки
            val constants = DoubleArray(matrixSize) { 0.0 }
            constants[0] = 1.0

            // решаем систему уравнений coefficientsArray * вектор вероятностей = constants
            val coefficientsMatrix = Array2DRowRealMatrix(coefficientsArray, false)
            val solver = LUDecomposition(coefficientsMatrix).solver
            val solution = solver.solve(org.apache.commons.math3.linear.ArrayRealVector(constants))

            val probabilities = solution.toArray().toList()

            // sumInputIntensity_j = ∑(λ_ij) - суммарная интенсивность, с которой система переходит в состояние j
            // sumOutputIntensity_j = ∑(λ_ji) - суммарная интенсивность, с которой система выходит из состояния j
            // в стационарном режиме частота, с которой система входит в состояние j = частоте, с которой система выходит из состояния j
            // frequencyInput_j = frequencyOutput_j - уравнение баланса
            // frequencyInput_j = Σ(probability_i * λ_ij) - частота, с которой система переходит в состояние j
            // frequencyOutput_j = probability_j * sumOutputIntensity_j - частота, с которой система выходит из состояния j
            // t_j = 1 / sumOutputIntensity_j - по определению из лекции (только по сути как период в физике нужно делить на частоту)
            // из уравнения баланса sumOutputIntensity_j = Σ(probability_i * λ_ij) / probability_j, следовательно
            // t_j = probability_j / Σ(probability_i * λ_ij)

            // взвешенная сумма интенсивностей, где вес - вероятность нахождения в исходном состоянии i
            // frequencyInput_j = Σ(probability_i * λ_ij)
            val frequencyInput = DoubleArray(matrixSize)
            for (j in 0 until matrixSize) { // для каждого целевого состояния j
                var currentFrequencySum = 0.0
                for (i in 0 until matrixSize) {
                    if (i == j) continue
                    // поток из i в j = (вероятность быть в i) * (интенсивность перехода из i в j)
                    currentFrequencySum += probabilities[i] * intensityMatrix[i][j]
                }
                frequencyInput[j] = currentFrequencySum
            }

            // t_j = probability_j / frequencyInput_j
            val times = probabilities.mapIndexed { index, p ->
                // frequencyInput = 0 - возможна, если в состояние нет входящих потоков,
                // но невозможно тк проверка на сильную связность графа исключат
                p / frequencyInput[index]
            }

            return CalculationResult(probabilities, times)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}