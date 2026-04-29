package com.moneyvisor.domain.model

data class Budget(
    val id: String,
    val category: String,
    val limitAmount: Double,
    val currentSpent: Double = 0.0
) {
    val progress: Float
        get() = if (limitAmount > 0) (currentSpent / limitAmount).toFloat() else 0f
    
    val isOverBudget: Boolean
        get() = currentSpent > limitAmount
}
