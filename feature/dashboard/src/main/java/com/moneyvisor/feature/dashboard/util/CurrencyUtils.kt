package com.moneyvisor.feature.dashboard.util

import java.util.Currency
import java.util.Locale
import java.text.NumberFormat

data class CurrencyInfo(
    val code: String,
    val name: String,
    val symbol: String,
    val country: String
)

object CurrencyUtils {
    private val currencies: List<CurrencyInfo> by lazy {
        val localeList = Locale.getAvailableLocales()
        val result = mutableMapOf<String, CurrencyInfo>()
        
        localeList.forEach { locale ->
            try {
                val currency = Currency.getInstance(locale)
                if (currency != null) {
                    val code = currency.currencyCode
                    if (!result.containsKey(code)) {
                        result[code] = CurrencyInfo(
                            code = code,
                            name = currency.getDisplayName(Locale.ENGLISH),
                            symbol = currency.getSymbol(locale),
                            country = locale.getDisplayCountry(Locale.ENGLISH)
                        )
                    }
                }
            } catch (e: Exception) {
                // Ignore locales without currencies
            }
        }
        
        result.values.toList().sortedBy { it.code }
    }

    fun getAllCurrencies(): List<CurrencyInfo> = currencies

    fun formatPrivacy(amount: String, isPrivacy: Boolean): String {
        return if (isPrivacy) "****" else amount
    }

    // Cache NumberFormat instances to avoid expensive initialization
    // Note: NumberFormat is not thread-safe, so we use ThreadLocal
    private val formatters = object : ThreadLocal<MutableMap<String, NumberFormat>>() {
        override fun initialValue() = mutableMapOf<String, NumberFormat>()
    }

    fun formatAmount(amount: Double, currencyCode: String): String {
        return try {
            val map = formatters.get()!!
            val format = map.getOrPut(currencyCode) {
                val currency = Currency.getInstance(currencyCode)
                val newFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
                newFormat.currency = currency
                newFormat
            }
            format.format(amount)
        } catch (e: Exception) {
            // Fallback to simple format
            "$currencyCode ${String.format("%.2f", amount)}"
        }
    }
}
