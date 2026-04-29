package com.moneyvisor.domain.model

data class Transaction(
    val id: String,
    val amount: Double,
    val category: String,
    val date: Long,
    val type: TransactionType,
    val tag: String = "",
    val isDraft: Boolean = false,
    val receiptUri: String? = null,
    val isRepeated: Boolean = false,
    val repeatInterval: RepeatInterval = RepeatInterval.NONE,
    val repeatValue: Int = 1 // Days or Day of Month
)

enum class TransactionType {
    INCOME, EXPENSE
}

enum class RepeatInterval {
    NONE, DAYS, MONTHLY
}
