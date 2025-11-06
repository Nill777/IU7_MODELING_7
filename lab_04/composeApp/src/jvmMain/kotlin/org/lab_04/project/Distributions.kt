package org.lab_04.project

import kotlin.math.ln
import kotlin.random.Random

sealed class Distribution {
    data class Uniform(val a: Double, val b: Double) : Distribution()
    data class Exponential(val lambda: Double) : Distribution()
    data class Normal(val mu: Double, val sigma: Double, val n: Int = 12) : Distribution()
    data class Erlang(val k: Int, val lambda: Double) : Distribution()
}

fun generate(dist: Distribution): Double {
    return when (dist) {
        is Distribution.Uniform -> {
            // a + (b - a) * R
            dist.a + (dist.b - dist.a) * Random.nextDouble()
        }
        is Distribution.Exponential -> {
            // -(1/λ) * ln(1 - R)
            -(1.0 / dist.lambda) * ln(1.0 - Random.nextDouble())
        }
        is Distribution.Normal -> {
            // σt * sqrt(12/n) * (ΣRi - n/2) + Mx
            val sumR = (1..dist.n).sumOf { Random.nextDouble() }
            dist.sigma * (sumR - dist.n / 2.0) + dist.mu
        }
        is Distribution.Erlang -> {
            // -(1/kλ) * Σln(1 - R)
            var sum = 0.0
            for (i in 1..dist.k) {
                sum += -(1.0 / dist.lambda / dist.k) * ln(1.0 - Random.nextDouble())
            }
            sum
        }
    }
}