package com.moneyvisor.feature.dashboard.util

import org.junit.Test
import java.util.Currency
import java.util.Locale
import java.text.NumberFormat

class CurrencyUtilsBenchmarkTest {
    @Test
    fun benchmarkFormatAmount() {
        val amount = 1234.56
        val currencyCode = "USD"
        val iterations = 50000

        // Warmup
        for (i in 1..10000) {
            formatAmountOriginal(amount, currencyCode)
            CurrencyUtils.formatAmount(amount, currencyCode)
        }

        val timeOriginal = measureTime {
            for (i in 1..iterations) {
                formatAmountOriginal(amount, currencyCode)
            }
        }

        val timeNew = measureTime {
            for (i in 1..iterations) {
                CurrencyUtils.formatAmount(amount, currencyCode)
            }
        }

        println("BENCHMARK_RESULT: Original: $timeOriginal ms")
        println("BENCHMARK_RESULT: New: $timeNew ms")
    }

    private fun measureTime(block: () -> Unit): Long {
        val start = System.currentTimeMillis()
        block()
        return System.currentTimeMillis() - start
    }

    private fun formatAmountOriginal(amount: Double, currencyCode: String): String {
        return try {
            val currency = Currency.getInstance(currencyCode)
            val format = NumberFormat.getCurrencyInstance(Locale.getDefault())
            format.currency = currency
            format.format(amount)
        } catch (e: Exception) {
            "$currencyCode ${String.format("%.2f", amount)}"
        }
    }
}
