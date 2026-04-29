package com.moneyvisor.data.repository

import com.moneyvisor.core.database.dao.BudgetDao
import com.moneyvisor.core.database.entity.toDomain
import com.moneyvisor.core.database.entity.toEntity
import com.moneyvisor.domain.model.Budget
import com.moneyvisor.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BudgetRepositoryImpl @Inject constructor(
    private val dao: BudgetDao
) : BudgetRepository {
    override fun getBudgets(): Flow<List<Budget>> {
        return dao.getBudgets().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertBudget(budget: Budget) {
        dao.insertBudget(budget.toEntity())
    }

    override suspend fun deleteBudget(budget: Budget) {
        dao.deleteBudget(budget.toEntity())
    }

    override suspend fun getBudgetByCategory(category: String): Budget? {
        return dao.getBudgetByCategory(category)?.toDomain()
    }
}
