package org.lab_03.project

import org.apache.commons.math3.util.CombinatoricsUtils.factorial
import kotlin.math.*

object Distributions {
    // Равномерное распределение
    fun uniformPDF(x: Double, a: Double, b: Double): Double {
        return if (x >= a && x <= b) 1.0 / (b - a) else 0.0
    }
    fun uniformCDF(x: Double, a: Double, b: Double): Double {
        return when {
            x < a -> 0.0
            x > b -> 1.0
            else -> (x - a) / (b - a)
        }
    }
    fun uniformMean(a: Double, b: Double): Double = (a + b) / 2.0
    fun uniformVariance(a: Double, b: Double): Double = (b - a).pow(2) / 12.0

    // Пуассоновское распределение (PMF - Probability Mass Function)
    fun poissonPMF(k: Int, lambda: Double): Double {
        if (k < 0 || lambda <= 0) return 0.0
        return (lambda.pow(k) * exp(-lambda)) / factorial(k)
    }
    fun poissonCDF(k: Int, lambda: Double): Double {
        if (k < 0 || lambda <= 0) return 0.0
        return (0..k).sumOf { i -> poissonPMF(i, lambda) }
    }
    fun poissonMean(lambda: Double): Double = lambda
    fun poissonVariance(lambda: Double): Double = lambda

    // Экспоненциальное распределение
    fun exponentialPDF(x: Double, lambda: Double): Double {
        return if (x >= 0) lambda * exp(-lambda * x) else 0.0
    }
    fun exponentialCDF(x: Double, lambda: Double): Double {
        return if (x >= 0) 1.0 - exp(-lambda * x) else 0.0
    }
    fun exponentialMean(lambda: Double): Double = 1.0 / lambda
    fun exponentialVariance(lambda: Double): Double = 1.0 / lambda.pow(2)

    // Нормальное распределение
    fun normalPDF(x: Double, mu: Double, sigma: Double): Double {
        if (sigma <= 0) return 0.0
        val exponent = -0.5 * ((x - mu) / sigma).pow(2)
        val coefficient = 1.0 / (sigma * sqrt(2 * PI))
        return coefficient * exp(exponent)
    }
    fun normalCDF(x: Double, mu: Double, sigma: Double): Double {
        if (sigma <= 0) return 0.0
        val z = (x - mu) / (sigma * sqrt(2.0))
        val t = 1.0 / (1.0 + 0.3275911 * abs(z))
        val a1 = 0.254829592
        val a2 = -0.284496736
        val a3 = 1.421413741
        val a4 = -1.453152027
        val a5 = 1.061405429
        val erf = 1.0 - (((((a5 * t + a4) * t) + a3) * t + a2) * t + a1) * t * exp(-z * z)
        return 0.5 * (1.0 + sign(z) * erf)
    }
    fun normalMean(mu: Double, sigma: Double): Double = mu
    fun normalVariance(mu: Double, sigma: Double): Double = sigma.pow(2)

    // Распределение Эрланга
    fun erlangPDF(x: Double, k: Int, lambda: Double): Double {
        if (x < 0 || k <= 0 || lambda <= 0) return 0.0
        return (lambda.pow(k) * x.pow(k - 1) * exp(-lambda * x)) / factorial(k - 1)
    }
    fun erlangCDF(x: Double, k: Int, lambda: Double): Double {
        if (x < 0 || k <= 0 || lambda <= 0) return 0.0
        val sum = (0 until k).sumOf { i ->
            (exp(-lambda * x) * (lambda * x).pow(i)) / factorial(i)
        }
        return 1.0 - sum
    }
    fun erlangMean(k: Int, lambda: Double): Double = k / lambda
    fun erlangVariance(k: Int, lambda: Double): Double = k / lambda.pow(2)
}
