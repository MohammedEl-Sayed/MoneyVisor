package com.moneyvisor.feature.dashboard.components

import com.moneyvisor.feature.dashboard.CategoryData
import org.junit.Test
import kotlin.system.measureNanoTime

class CategoryPieChartBenchmarkTest {

    @Test
    fun benchmarkAnglesCalculation() {
        val data = List(100) { CategoryData("Cat$it", 100.0, 0.01f) }

        // Warmup
        for (i in 1..10000) {
            oldMethod(data)
            newMethod(data)
        }

        val iterations = 1000000
        var oldTotal = 0L
        var newTotal = 0L

        System.gc()
        Thread.sleep(100)

        for (i in 1..5) {
            val timeOld = measureNanoTime {
                for (j in 1..iterations) {
                    oldMethod(data)
                }
            }

            val timeNew = measureNanoTime {
                for (j in 1..iterations) {
                    newMethod(data)
                }
            }

            println("Run $i - Old: ${timeOld / 1_000_000} ms, New: ${timeNew / 1_000_000} ms")
            if (i > 1) { // Skip first run for averaging
                oldTotal += timeOld
                newTotal += timeNew
            }
        }

        println("Average (runs 2-5) - Old: ${oldTotal / 4 / 1_000_000} ms, New: ${newTotal / 4 / 1_000_000} ms")
    }

    private fun oldMethod(data: List<CategoryData>): List<Float> {
        var current = -90f
        return data.take(6).map { item ->
            val start = current
            current += item.percentage * 360f
            start
        }
    }

    private fun newMethod(data: List<CategoryData>): FloatArray {
        val size = minOf(data.size, 6)
        val angles = FloatArray(size)
        var current = -90f
        for (i in 0 until size) {
            angles[i] = current
            current += data[i].percentage * 360f
        }
        return angles
    }
}
