package com.moneyvisor.domain.repository

import com.moneyvisor.domain.model.Budget
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun getBudgets(): Flow<List<Budget>>
    suspend fun insertBudget(budget: Budget)
    suspend fun deleteBudget(budget: Budget)
    suspend fun getBudgetByCategory(category: String): Budget?
}
