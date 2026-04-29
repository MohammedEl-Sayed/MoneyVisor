package com.moneyvisor.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.moneyvisor.domain.model.Budget

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey
    val id: String,
    val category: String,
    val limitAmount: Double
)

fun BudgetEntity.toDomain(currentSpent: Double = 0.0) = Budget(
    id = id,
    category = category,
    limitAmount = limitAmount,
    currentSpent = currentSpent
)

fun Budget.toEntity() = BudgetEntity(
    id = id,
    category = category,
    limitAmount = limitAmount
)
