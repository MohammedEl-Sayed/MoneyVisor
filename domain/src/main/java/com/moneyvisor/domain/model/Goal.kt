package com.moneyvisor.domain.model

data class Goal(
    val id: String,
    val name: String,
    val targetAmount: Double,
    val currentSaved: Double = 0.0
) {
    val progress: Float
        get() = if (targetAmount > 0) (currentSaved / targetAmount).toFloat() else 0f
    
    val isCompleted: Boolean
        get() = currentSaved >= targetAmount
}
