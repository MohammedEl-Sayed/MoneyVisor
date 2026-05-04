package com.moneyvisor.feature.dashboard.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class CurrencyUtilsTest {

    @Test
    fun testGetAllCurrencies() {
        val currencies = CurrencyUtils.getAllCurrencies()
        assertTrue(currencies.isNotEmpty())

        // Check if common currencies exist
        val hasUsd = currencies.any { it.code == "USD" }
        val hasEur = currencies.any { it.code == "EUR" }
        assertTrue(hasUsd)
        assertTrue(hasEur)

        // Check sorting
        val isSorted = currencies.zipWithNext { a, b -> a.code <= b.code }.all { it }
        assertTrue(isSorted)
    }

    @Test
    fun testFormatPrivacy() {
        assertEquals("****", CurrencyUtils.formatPrivacy("100.00", true))
        assertEquals("100.00", CurrencyUtils.formatPrivacy("100.00", false))
    }

    @Test
    fun testFormatAmount() {
        // Set default locale to ensure deterministic formatting
        val originalLocale = Locale.getDefault()
        Locale.setDefault(Locale.US)

        try {
            // Test valid currency code
            val formattedUsd = CurrencyUtils.formatAmount(100.50, "USD")
            assertEquals("$100.50", formattedUsd)

            val formattedEur = CurrencyUtils.formatAmount(100.50, "EUR")
            assertEquals("€100.50", formattedEur)

            val formattedGbp = CurrencyUtils.formatAmount(100.50, "GBP")
            assertEquals("£100.50", formattedGbp)

            // Test invalid currency code
            val formattedInvalid = CurrencyUtils.formatAmount(100.50, "INVALID")
            assertEquals("INVALID 100.50", formattedInvalid)
        } finally {
            // Restore original locale
            Locale.setDefault(originalLocale)
        }
    }
}
