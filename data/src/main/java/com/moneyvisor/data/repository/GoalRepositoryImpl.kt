package com.moneyvisor.data.repository

import com.moneyvisor.core.database.dao.GoalDao
import com.moneyvisor.core.database.entity.toDomain
import com.moneyvisor.core.database.entity.toEntity
import com.moneyvisor.domain.model.Goal
import com.moneyvisor.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GoalRepositoryImpl @Inject constructor(
    private val dao: GoalDao
) : GoalRepository {
    override fun getGoals(): Flow<List<Goal>> {
        return dao.getGoals().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertGoal(goal: Goal) {
        dao.insertGoal(goal.toEntity())
    }

    override suspend fun deleteGoal(goal: Goal) {
        dao.deleteGoal(goal.toEntity())
    }

    override suspend fun getGoalById(id: String): Goal? {
        return dao.getGoalById(id)?.toDomain()
    }
}
