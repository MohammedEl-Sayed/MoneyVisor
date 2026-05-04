package com.moneyvisor.data.repository

import com.moneyvisor.core.database.dao.GoalDao
import com.moneyvisor.core.database.entity.GoalEntity
import com.moneyvisor.domain.model.Goal
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GoalRepositoryImplTest {

    private val dao: GoalDao = mockk()
    private val repository = GoalRepositoryImpl(dao)

    @Test
    fun `getGoals returns mapped domain models`() = runTest {
        val entities = listOf(
            GoalEntity(id = "1", name = "Vacation", targetAmount = 1000.0, currentSaved = 200.0),
            GoalEntity(id = "2", name = "Car", targetAmount = 5000.0, currentSaved = 1500.0)
        )
        every { dao.getGoals() } returns flowOf(entities)

        repository.getGoals().collect { goals ->
            assertEquals(2, goals.size)
            assertEquals("Vacation", goals[0].name)
            assertEquals(1000.0, goals[0].targetAmount, 0.0)
            assertEquals(200.0, goals[0].currentSaved, 0.0)
            assertEquals("Car", goals[1].name)
            assertEquals(5000.0, goals[1].targetAmount, 0.0)
            assertEquals(1500.0, goals[1].currentSaved, 0.0)
        }
    }

    @Test
    fun `insertGoal calls dao insert with mapped entity`() = runTest {
        val goal = Goal(id = "1", name = "Vacation", targetAmount = 1000.0, currentSaved = 200.0)
        coEvery { dao.insertGoal(any()) } returns Unit

        repository.insertGoal(goal)

        coVerify { dao.insertGoal(match { it.id == "1" && it.name == "Vacation" && it.targetAmount == 1000.0 && it.currentSaved == 200.0 }) }
    }

    @Test
    fun `deleteGoal calls dao delete with mapped entity`() = runTest {
        val goal = Goal(id = "1", name = "Vacation", targetAmount = 1000.0, currentSaved = 200.0)
        coEvery { dao.deleteGoal(any()) } returns Unit

        repository.deleteGoal(goal)

        coVerify { dao.deleteGoal(match { it.id == "1" && it.name == "Vacation" && it.targetAmount == 1000.0 && it.currentSaved == 200.0 }) }
    }

    @Test
    fun `getGoalById returns mapped domain model`() = runTest {
        val entity = GoalEntity(id = "1", name = "Vacation", targetAmount = 1000.0, currentSaved = 200.0)
        coEvery { dao.getGoalById("1") } returns entity

        val result = repository.getGoalById("1")

        assertEquals("Vacation", result?.name)
        // Ensure result is not null before checking its properties to avoid nullable ambiguity in assertEquals
        assertEquals(1000.0, result!!.targetAmount, 0.0)
        assertEquals(200.0, result.currentSaved, 0.0)
    }
}
