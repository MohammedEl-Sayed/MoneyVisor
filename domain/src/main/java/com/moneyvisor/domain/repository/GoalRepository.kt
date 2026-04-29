package com.moneyvisor.domain.repository

import com.moneyvisor.domain.model.Goal
import kotlinx.coroutines.flow.Flow

interface GoalRepository {
    fun getGoals(): Flow<List<Goal>>
    suspend fun insertGoal(goal: Goal)
    suspend fun deleteGoal(goal: Goal)
    suspend fun getGoalById(id: String): Goal?
}
