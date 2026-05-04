package com.moneyvisor.feature.dashboard.util

import org.junit.Test
import java.util.Currency
import java.util.Locale
import java.text.NumberFormat
import java.util.concurrent.ConcurrentHashMap

class CurrencyUtilsTest {
    @Test
    fun testCurrencyFormatting() {
        val amount = 1234.56
        val result = CurrencyUtils.formatAmount(amount, "USD")
        assert(result.contains("1,234.56") || result.contains("1.234,56"))
        assert(result.contains("$") || result.contains("US$") || result.contains("USD"))
    }
}
